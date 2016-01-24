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
   [dire.core :refer [with-handler!]]
   [aleph.http :as http]
   [manifold.stream :as stream]
   [manifold.deferred :as deferred]
   [ring.middleware.params :as params]
   [compojure.route :as route]
   [compojure.core :as compojure :refer [GET]]))

(defonce timbre-config
  (timbre/merge-config!
   {:appenders {:spit (appenders/spit-appender {:fname "mud-server.log"})}}))

(def server (ref nil))
(def clients (ref {}))

(defn register-client [socket]
  (dosync (let [client-name (count @clients)]
            (alter clients assoc (count @clients) socket)
            client-name)))

(defn welcome [socket]
  (let [client-name  (register-client socket)]
    (stream/put! socket (str "Welcome to the Mud Server, Client #" client-name "!"))
    client-name))

(defn websocket-handler
  [request]
  (info "Handling incoming request...")
  (info request)
  (->
   (deferred/let-flow [socket (http/websocket-connection request)]
     (let [client-name (welcome socket)]
       (stream/consume
        (fn [message]
          ;; (info message)
          (doall (doseq [[client-name-this client-socket] @clients]
                   (info "Posting" client-name-this "-" message)
                   (stream/put! client-socket (str client-name ": " message)))))
        socket)))
   (deferred/catch
       (fn [_] {:status 400
               :headers {"content-type" "application/text"}
               :body "Expected a websocket connection!"}))))

(def handler
  (params/wrap-params
    (compojure/routes
      (GET "/connect" [] websocket-handler)
      (route/not-found "No such page."))))

(defn start-server
  [configuration]
  (if (not @server)
    (let [server-port (:port (:server configuration))]
      (dosync (info "Starting the Mud server, listening on port" server-port)
              (ref-set server (http/start-server handler {:port server-port}))))))

(defn stop-server
  []
  (if @server
    (dosync (info "Stopping the Mud server")
            (.close @server)
            (ref-set server nil))))
