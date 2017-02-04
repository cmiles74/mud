(ns cmiles74.mud.server.cli
  "Command line handling for the MUD server"
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
   [cmiles74.mud.server.mud :as server])
  (:use
   [slingshot.slingshot :only [throw+ try+]]))

;; default configuration
(def DEFAULT-CONFIG
  {:logging {:level "debug"}
   :server {:host "localhost"
            :port 18080}
   :database {:host "mud-rethinkdb"
              :port 28015}})

;; default name of the configuration file
(def DEFAULT-CONFIG-FILE ".mud-server-config.yml")

(defn load-config-file
  "Loads the configuration file from the provided path, returns a map with the
  configuration values."
  [path-in]
  (try+
   (config/load-config-file DEFAULT-CONFIG DEFAULT-CONFIG-FILE path-in)
   (catch Object exception
     (do (debug (:throwable &throw-context))
         DEFAULT-CONFIG))))

(def cli-options
  [["-c" "--config FILE" "Path to the configuration file" :default nil]
   ["-h" "--help"]])

(defn usage
  "Generates the 'usage' help output using the sequence of text strings provided
  in the options-summary parameter."
  [options-summary]
  (->> ["Starts up the mud server."
        ""
        options-summary]
       (string/join \newline)))

(defn main
  "Bootstraps the application and handles the command line arguments."
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (cli/exit 0 (usage summary))
      errors (cli/exit 1 (cli/error-msg errors)))
    (let [configuration (load-config-file (:config options))]
      (if (:level (:logging configuration))
        (timbre/set-level! (keyword (:level (:logging configuration)))))
      (server/start-server configuration))))

(defn -main
  "The bootstrapping function used to start the application."
  [& args]
  (apply main args))

