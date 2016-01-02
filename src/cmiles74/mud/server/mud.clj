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

(defn echo-handler
  [request]
  (info "Handling incoming request...")
  (->
   (deferred/let-flow [socket (http/websocket-connection request)]
               (stream/connect socket socket))
   (deferred/catch
       (fn [_] {:status 400
               :headers {"content-type" "application/text"}
               :body "Expected a websocket connection!"}))))

(def handler
  (params/wrap-params
    (compojure/routes
      (GET "/echo" [] echo-handler)
      (route/not-found "No such page."))))

(defn start-server
  []
  (if (not @server)
    (dosync (info "Starting the Mud server")
            (ref-set server (http/start-server handler {:port 18080})))))

(defn stop-server
  []
  (if @server
    (do (info "Stopping the Mud server")
        (.close @server))))
