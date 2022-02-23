(ns com.nervestaple.mud.server.mud
  "The main MUD server"
  (:require
   [aleph.http :as http]
   [bidi.ring :refer [make-handler]]
   [cheshire.core :as json]
   [com.nervestaple.mud.server.game :as game]
   [com.nervestaple.mud.log.interface :as log]
   [manifold.stream :as stream]
   [manifold.deferred :as deferred]
   [manifold.bus :as bus]
   [ring.middleware.params :as params]
   [yada.yada :as yada]))

;; maximum messages any client can send per second
(def client-max-message-rate 5)

;; maximum backlog allowed per client
(def client-max-message-backlog 10)

;; our server instance
(def server (ref nil))

;; our event bus for broadcasts
(def event-bus (bus/event-bus))

(defn welcome
  "Writes a welcome message for the provided client-name to the provided websocket
  stream."
  [client]
  (stream/put! (:connection client)
               (json/generate-smile {:type "welcome"
                                     :content (str "Welcome to the Mud Server, "
                                                   (client :friendly-name) "!")})))

(defn handle-server-command
  "Handles a server command from the client."
  [client message])

(defn client-handler
  "Returns a function that will handle incoming messages from the client and
  publish them over the broadcast channel."
  [client]
  (fn [message-in]

    ;; parse the incoming message
    (let [message (json/parse-smile message-in true)]

      (case (message :type)

        "server-command" (handle-server-command client message)

        ;; let the game handle the message
        (game/handle-incoming-message client message)))))

(defn websocket-handler
  "Handles an incoming client web request by creating a websocket stream for the
  request and welcoming the new client."
  [request]
  (log/debug request)
  (->
   (deferred/let-flow [stream (http/websocket-connection request)]

     ;; register the new client
     (let [client (game/register-client stream)
           handler-fn (client-handler client)]

       ;; welcome the client
       (welcome client)

       ;; setup the game for the client
       (game/initialize-client client)

       ;; subscribe the client's websocket stream to our broadcast topic
       (stream/connect
        (bus/subscribe event-bus ::broadcast)
        stream
        {:timeout 1e4})

       ;; consume client messages and pass to our handler function
       (stream/consume handler-fn
        (stream/throttle
         client-max-message-rate
         client-max-message-backlog
         stream))))
   (deferred/catch
       (fn [_] {:status 400
                :headers {"content-type" "application/text"}
                :body "Expected a websocket connection!"}))))

;; the web API
(def api
  ["/api"
   (yada/swaggered
    ["/hello" (yada/yada "Hello from the MUD server!")]
    {:info {:title "Hello World!"
            :version "1.0"
            :description "Demonstrating yada + swagger"}
     :basePath "/api"})])


;; handler function for incoming web requests
(def handler
  (make-handler ["/" {"connect" websocket-handler}]))

;; the web application
(def app
  (-> handler
      params/wrap-params))

(defn start-server
  "Starts the server with the provided map of configuration options."
  [configuration]
  (when-not @server
    (log/set-ns-log-level "io.netty.*" :info)
    (let [server-port (:mud_port configuration)]
      (dosync (log/info "Starting the Mud server, listening on port" server-port)
              (ref-set server (http/start-server app {:port server-port}))))))

(defn stop-server
  "Stops the currently running server."
  []
  (when @server
    (dosync (log/info "Stopping the Mud server")
            (.close @server)
            (ref-set server nil))))
