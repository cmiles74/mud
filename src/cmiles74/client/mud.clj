(ns cmiles74.client.mud
  (:gen-class)
  (:require
   [taoensso.timbre :as timbre
    :refer (log  trace  debug  info  warn  error  fatal  report
                 logf tracef debugf infof warnf errorf fatalf reportf
                 spy get-env log-env)]
   [taoensso.timbre.appenders.core :as appenders]
   [taoensso.timbre.profiling :as profiling
    :refer (pspy pspy* profile defnp p p*)]
   [slingshot.slingshot :only [throw+ try+]]
   [dire.core :refer [with-handler!]]
   [aleph.http :as http]
   [manifold.stream :as stream]
   [manifold.deferred :as deferred]
   [clojure.core.async :as async]
   [cmiles74.client.console :as console]
   [cmiles74.client.keybinding :as keybinding]))

(defonce timbre-config
  (timbre/merge-config!
   {:appenders {:spit (appenders/spit-appender {:fname "mud-client.log"})}}))

(defn post-to-server-fn
  "Returns a function for posting  a textual message to the server."
  [server-socket]
  (fn [console]

    ;; post our message to the server
    (stream/put! server-socket (apply str @(:input-buffer console)))

    ;; clear the input buffer and the input area
    (console/clear-input-buffer console)
    (console/clear-input-area console)))

(defn handle-server-message
  [server-socket console]
  (stream/consume
   (fn [message]
     (console/break-writeln-console console message))
   server-socket))

(defn build-keybindings
  [server-socket]
  (merge keybinding/DEFAULT-KEYBINDINGS
         {(keybinding/vim-keystroke "<Return>")
          {:description "Process the current input buffer"
           :handler (post-to-server-fn server-socket)}}))

(defn create-client
  []
  (let [server-socket @(http/websocket-client "ws://localhost:18080/echo")
        keybindings (build-keybindings server-socket)
        console (console/create-interactive-console keybindings)]
    (handle-server-message server-socket console)
    {:console console
     :server-socket server-socket}))

(defn main
  [& args]
  (create-client))

(defn -main
  [& args]
  (apply main args))
