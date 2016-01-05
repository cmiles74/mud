(ns cmiles74.mud.client.cli
  (:gen-class)
  (:require
   [taoensso.timbre :as timbre
    :refer (log  trace  debug  info  warn  error  fatal  report
                 logf tracef debugf infof warnf errorf fatalf reportf
                 spy get-env log-env)]
   [taoensso.timbre.appenders.core :as appenders]
   [taoensso.timbre.profiling :as profiling
    :refer (pspy pspy* profile defnp p p*)]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.string :as string]
   [cmiles74.mud.common.config :as config]
   [cmiles74.mud.common.cli :as cli]
   [cmiles74.mud.client.mud :as client])
  (:use
   [slingshot.slingshot :only [throw+ try+]]))

(def DEFAULT-CONFIG
  {:logging {:level "debug"}
   :server {:host "localhost"
            :port 18080}})

(def DEFAULT-CONFIG-FILE ".mud-client-config.yml")

(defn load-config-file [path-in]
  (try+
   (config/load-config-file DEFAULT-CONFIG DEFAULT-CONFIG-FILE path-in)
   (catch Object exception
     (do (debug (:throwable &throw-context))
         DEFAULT-CONFIG))))

(def cli-options
  [["-c" "--config FILE" "Path to the configuration file" :default nil]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Starts up the mud client."
        ""
        options-summary]
       (string/join \newline)))

(defn main
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (cli/exit 0 (usage summary))
      errors (cli/exit 1 (cli/error-msg errors)))
    (let [configuration (load-config-file (:config options))]
      (if (:level (:logging configuration))
        (timbre/set-level! (keyword (:level (:logging configuration)))))
      (client/create-client configuration))))

(defn -main
  [& args]
  (apply main args))
