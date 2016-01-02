(ns cmiles74.mud.server.cli
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
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.string :as string]
   [clojure.java.io :as io]
   [clj-yaml.core :as yaml]
   [cmiles74.mud.server.mud :as server]))

(def DEFAULT-CONFIG
  {:server {:host "localhost"
            :port 18080}
   :database {:host "mud-rethinkdb"
              :port 28015}})

(def DEFAULT-CONFIG-FILE ".mud-server.conf")

(def DEFAULT-CONFIG-FILE-SEARCH
  [(str (System/getProperty "user.dir") "/" DEFAULT-CONFIG-FILE)
   (str (System/getProperty "user.home") "/" DEFAULT-CONFIG-FILE)])

(defn find-config-file []
  (some #(if (.exists (io/as-file %)) %) DEFAULT-CONFIG-FILE-SEARCH))

(defn read-config-file [path-in]
  (yaml/parse-string (slurp path-in)))

(defn load-config-file [path-in]
  (cond
    path-in (read-config-file path-in)
    (find-config-file)(read-config-file (find-config-file))
    :else DEFAULT-CONFIG))

(def cli-options
  [["-c" "--config" "Path to the configuration file"
    :default nil]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Starts up the mud server."
        ""
        options-summary]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn main
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      errors (exit 1 (error-msg errors)))
    (info "Configuration:" (:config options))
    (server/start-server)))

(defn -main
  [& args]
  (apply main args))

