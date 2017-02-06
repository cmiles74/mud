(ns cmiles74.mud.server.mud
  "The main MUD server"
  (:require
   [taoensso.timbre :as timbre
    :refer (log  trace  debug  info  warn  error  fatal  report
                 logf tracef debugf infof warnf errorf fatalf reportf
                 spy get-env log-env)]
   [taoensso.timbre.appenders.core :as appenders]
   [taoensso.timbre.profiling :as profiling
    :refer (pspy pspy* profile defnp p p*)]
   [slingshot.slingshot :only [throw+ try+]]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.string :as string]
   [aleph.http :as http]
   [manifold.stream :as stream]
   [manifold.deferred :as deferred]
   [manifold.bus :as bus]
   [ring.middleware.params :as params]
   [bidi.bidi :as bidi]
   [bidi.ring :refer [make-handler]]
   [yada.yada :as yada]
   [compojure.route :as route]
   [compojure.core :as compojure :refer [GET]]
   [cheshire.core :as json]
   [cmiles74.mud.server.game :as game]))

(defonce timbre-config
  (timbre/merge-config!
   {:appenders {:spit (appenders/spit-appender {:fname "mud-server.log"})}}))

;; maximum messages any client can send per second
(def client-max-message-rate 5)

;; maximum backlog allowed per client
(def client-max-message-backlog 10)

;; our server instance
(def server (ref nil))

;; map of anonymous client names to their websocket streams
(def anonymous-clients (ref {}))

;; our event bus for broadcasts
(def event-bus (bus/event-bus))

(defn register-client
  "Registers a new client with the server by assigning them a name and adding
  their name and websocket stream to the anonymous-clients map. Returns a map of
  client data."
  [stream]
  (dosync (let [client-name (count @anonymous-clients)
                client-map {:websocket stream
                            :name (str "Anonymous #" client-name)
                            :room 1}]
            (alter anonymous-clients assoc client-name client-map)
            client-map)))

(defn welcome
  "Writes a welcome message for the provided client-name to the provided websocket stream."
  [client]
  (stream/put! (:websocket client)
               (json/generate-smile {:type "welcome"
                                     :content (str "Welcome to the Mud Server, " (:name client) "!")})))

(defn client-handler
  "Returns a function that will handle incoming messages from the client and
  publish them over the broadcast channel."
  [client]
  (fn [message-in]

    ;; parse the incoming message
    (let [message (json/parse-smile message-in true)]

      (case (message :type)
        "message" (bus/publish! event-bus ::broadcast
                                (json/generate-smile {:type "message"
                                                      :from (client :name)
                                                      :content (message :content)}))))))

(defn websocket-handler
  "Handles an incoming client web request by creating a websocket stream for the
  request and welcoming the new client."
  [request]
  (info "Handling incoming request...")
  (info request)
  (->
   (deferred/let-flow [stream (http/websocket-connection request)]

     ;; register the new client
     (let [client (register-client stream)
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

       ;; consume and handle all client messages
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
    {:info {:title "Hello World!"
            :version "1.0"
            :description "Demonstrating yada + swagger"}
     :basePath "/api"}
    ["/hello" (yada/yada "Hello from the MUD server!")])])


;; handler function for incoming web requests
(def handler
  (make-handler ["/" {"connect" websocket-handler}]))

;; the web application
(def app
  (-> handler
      params/wrap-params))

(defn start-server
  "Starts the serveer with the provided map of configuration options."
  [configuration]
  (if (not @server)
    (let [server-port (:port (:server configuration))]

      (dosync (info "Starting the Mud server, listening on port" server-port)
              (ref-set server (http/start-server app {:port server-port}))))))

(defn stop-server
  "Stops the currently running server."
  []
  (if @server
    (dosync (info "Stopping the Mud server")
            (.close @server)
            (ref-set server nil))))
