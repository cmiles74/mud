
(ns cmiles74.mud.server.mud
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
   [compojure.core :as compojure :refer [GET]]))

(defonce timbre-config
  (timbre/merge-config!
   {:appenders {:spit (appenders/spit-appender {:fname "mud-server.log"})}}))

;; our server instance
(def server (ref nil))

;; map of client names to their streams
(def anonymous-clients (ref {}))

;; our event bus for broadcasts
(def event-bus (bus/event-bus))

(defn register-client [stream]
  (dosync (let [client-name (count @anonymous-clients)]
            (alter anonymous-clients assoc (count @anonymous-clients) stream)
            ()
            client-name)))

(defn welcome [stream]
  (let [client-name  (register-client stream)]
    (stream/put! stream (str "Welcome to the Mud Server, Client #" client-name "!"))
    client-name))

(defn websocket-handler
  [request]
  (info "Handling incoming request...")
  (info request)
  (->
   (deferred/let-flow [stream (http/websocket-connection request)]
     (let [client-name (welcome stream)]

       ;; subscribe the new stream to our broadcast topic
       (stream/connect
        (bus/subscribe event-bus ::broadcast) stream)

       ;; consume all messages and post to broadcast stream
       (stream/consume
        (fn [message]
          (bus/publish! event-bus ::broadcast (str client-name ": " message)))
        stream)))
   (deferred/catch
       (fn [_] {:status 400
                :headers {"content-type" "application/text"}
                :body "Expected a websocket connection!"}))))

(def api
  ["/api"
   (yada/swaggered
    {:info {:title "Hello World!"
            :version "1.0"
            :description "Demonstrating yada + swagger"}
     :basePath "/api"}
    ["/hello" (yada/yada "Hello, Miles!")])])

(def handler
  (make-handler ["/" {"connect" websocket-handler}]))

(def app
  (-> handler
      params/wrap-params))

(defn start-server
  [configuration]
  (if (not @server)
    (let [server-port (:port (:server configuration))]

      (dosync (info "Starting the Mud server, listening on port" server-port)
              (ref-set server (http/start-server app {:port server-port}))))))

(defn stop-server
  []
  (if @server
    (dosync (info "Stopping the Mud server")
            (.close @server)
            (ref-set server nil))))
