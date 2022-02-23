(ns com.nervestaple.mud.client.mud
  (:require
   [aleph.http :as http]
   [cheshire.core :as json]
   [manifold.stream :as stream]
   [com.nervestaple.mud.console.interface :as console]
   [com.nervestaple.mud.client.keybinding :as keybinding]))

(def COMMAND-PATTERN (re-pattern "\\/([^\\s]+)"))

(defn post-message
  "Posts a chat message to the server."
  [content]
  (json/generate-smile {:type "message"
                        :content content}))

(defn post-command
  "Posts a command message to the server."
  [command argument]
  (json/generate-smile {:type command
                        :content (first argument)}))

(defn parse-command [input]
  (let [command (re-find COMMAND-PATTERN input)]
    (when command
      (conj [(second command)]
            (.trim (apply str (drop (count (first command)) input)))))))

(defn handle-input-fn
  "Handles all input from the client, typically the client sends this data by
  pressing the return key."
  [server-stream]
  (fn [console]

    (let [input (apply str @(:input-buffer console))
          command (parse-command input)]
      (if command

        ;; post the command and argument
        (stream/put! server-stream (post-command (first command) (rest command)))

        ;; post a chat message
        (stream/put! server-stream (post-message (apply str @(:input-buffer console))))))

    ;; clear the input buffer and the input area
    (console/clear-input-buffer console)
    (console/clear-input-area console)))

(defn write-console
  "Writes the provided content to the console, lines are broken to fit."
  [console content]
  (console/break-writeln-console console content))

(defn handle-incoming-message
  "Reads incoming messages from the provided server websocket stream and writes
  them to the provided console."
  [server-stream console]
  (stream/consume
   (fn [message-in]

     ;; parse the incoming message
     (let [message (json/parse-smile message-in true)]
       (case (message :type)
         "welcome" (write-console console (message :content))

         "message" (write-console console (str (message :from) ": " (message :content)))

         "move"    (write-console console ((message :content) :description))

         "error"   (write-console console (message :content))

         (write-console console
                        (str "I received a message from the server that I do "
                             "not understand: \"" message "\"")))))
   server-stream))

(defn build-keybindings
  "Returns a map of keybindings and handler functions. The default set of
  bindings are merged with a binding to submit messages to the provided server
  websocket stream and returned. All of the binding functions are contained in a
  map with the following keys: :description, :handler."
  [server-stream]
  (merge keybinding/DEFAULT-KEYBINDINGS
         {(keybinding/vim-keystroke "<Return>")
          {:description "Process the current input buffer"
           :handler (handle-input-fn server-stream)}}))

(defn create-client
  "Creates a new map of client data for the provided map of configuration data.
  A client consists of a server websocket stream, ,keybindings and an
  interactive console. When this function exits, the client will be active and
  connected to the server, the console will be ready for interaction. This
  function will return a map with the follow keys:
  :console, :server-socket."
  [configuration]
  (let [server-host (:mud_host configuration)
        server-port (:mud_port configuration)
        server-stream @(http/websocket-client
                        (str (if (= 443 server-port) "wss://" "ws://")
                             server-host ":" server-port "/connect"))
        keybindings (build-keybindings server-stream)
        console (console/create-interactive-console keybindings server-stream)]
    (console/break-writeln-console console
                                   (str "Connecting to server " server-host
                                        " on port " server-port "..."))
    (handle-incoming-message server-stream console)
    {:console console
     :server-socket server-stream}))
