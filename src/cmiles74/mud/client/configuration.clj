(ns cmiles74.mud.client.configuration
  (:require
   [clojure.java.io :as io]))

(def DEFAULT-PROPERTIES
  {:server "localhost" :port "18080"})
(def config-properties (atom {}))
(def config-file ".mud-client.properties")
(def user-home (System/getProperty "user.dir"))
(def config-path (str user-home "/" config-file))

(defn load-config []
  DEFAULT-PROPERTIES)

(defn store-config [config-map])
