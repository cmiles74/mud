(ns com.nervestaple.mud.config.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [com.nervestaple.mud.log.interface :as log]))

(defn read-from-resource
  [resource-path]
  (log/trace (str "Reading configuration from resource \"" resource-path "\""))
  (try
    (edn/read-string (slurp (io/resource resource-path)))
    (catch Exception exception
      (log/trace "Could not read configuration from resource:"
                (.getMessage exception))
      {})))

(defn read-from-file
  [file-path]
  (log/debug (str "Reading configuration from file \"" file-path "\""))
  (let [file (when file-path (io/as-file file-path))
        config (cond

                 ;; ensure we have a file path
                 (not file-path)
                 (log/debug "No configuration file path was provided!")

                 ;; make sure the file exists
                 (not (.exists file))
                 (log/info (str "Provided configuration file does not exist, "
                                "using configuration from resource file."))

                 ;; make sure the file is readable
                 (and (.exists file) (not (.canRead file)))
                 (log/warn (str "Provided configuration file path was not readable!"))

                 ;; read the file
                 (and (.exists file) (.canRead file))
                 (try
                   (edn/read-string (slurp file))
                   (catch Exception exception
                     (log/warn "Could not read configuration from the file path:"
                               (.geMessage exception)))))]

    ;; if we don't have any data, return an empty map
    (if config config {})))

(defn read-from-environment
  [variable-list]
  (into {}
        (map #(when (System/getenv %)
                [(keyword (string/lower-case %)) (System/getenv %)])
        variable-list)))

(defn read-config
  [resource-path file-path variable-list]
  (merge (read-from-resource resource-path)
         (read-from-file file-path)
         (read-from-environment variable-list)))

