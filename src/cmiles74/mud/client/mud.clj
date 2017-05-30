(ns cmiles74.mud.client.mud
  "The MUD client "
  (:require
   [taoensso.timbre :as timbre
    :refer (log  trace  debug  info  warn  error  fatal  report
                 logf tracef debugf infof warnf errorf fatalf reportf
                 spy get-env log-env)]
   [taoensso.timbre.appenders.core :as appenders]
   [taoensso.timbre.profiling :as profiling
    :refer (pspy pspy* profile defnp p p*)]
   [slingshot.slingshot :only [throw+ try+]]
   [aleph.http :as http]
   [manifold.stream :as stream]
   [manifold.deferred :as deferred]
   [clojure.core.async :as async]
   [cheshire.core :as json]
   [cmiles74.mud.client.console :as console]
   [cmiles74.mud.client.keybinding :as keybinding]))

(defonce timbre-config
  (timbre/merge-config!
   {:appenders {:spit (appenders/spit-appender {:fname "mud-client.log"})}}))

(defn post-message
  "Posts a chat message to the server."
  [content]
  (json/generate-smile {:type "message"
                        :content content}))

(defn handle-input-fn
  "Handles all input from the client, typically the client sends this data by
  pressing the return key."
  [server-stream]
  (fn [console]

    ;; for now, treat everything as a chat message
    (stream/put! server-stream (post-message (apply str @(:input-buffer console))))

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

         "move"    (write-console console (str (message :content))))))
   server-stream))

(defn build-keybindings
  "Returns a map of keybindings and handler functions. The default set of
  bindings are merged with a binding to submit messages to the provided server
  websocket stream and returned. All of the binding functions are contained in a
  mpmap with the following keys: :description, :handler."
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
  (let [server-host (:host (:server configuration))
        server-port (:port (:server configuration))
        server-stream @(http/websocket-client
                        (str "ws://" server-host ":" server-port "/connect"))
        keybindings (build-keybindings server-stream)
        console (console/create-interactive-console keybindings server-stream)]
    (console/break-writeln-console console
                                   (str "Connecting to server " server-host
                                        " on port " server-port "..."))
    (handle-incoming-message server-stream console)
    {:console console
     :server-socket server-stream}))
