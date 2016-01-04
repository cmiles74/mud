(ns cmiles74.mud.common.config
  (:require
   [taoensso.timbre :as timbre
    :refer (log  trace  debug  info  warn  error  fatal  report
                 logf tracef debugf infof warnf errorf fatalf reportf
                 spy get-env log-env)]
   [taoensso.timbre.appenders.core :as appenders]
   [taoensso.timbre.profiling :as profiling
    :refer (pspy pspy* profile defnp p p*)]
   [clojure.java.io :as io]
   [clj-yaml.core :as yaml])
  (:use
   [slingshot.slingshot :only [throw+ try+]]))

(defn search-config-file [file-name]
  [(str (System/getProperty "user.dir") "/" file-name)
   (str (System/getProperty "user.home") "/" file-name)])

(defn find-config-file [file-name]
  (some #(if (.exists (io/as-file %)) %) (search-config-file file-name)))

(defn read-config-file [path-in]
  (yaml/parse-string (slurp path-in)))

(defn do-load-config-file [default-config file-name path-in]
  (cond
    path-in (read-config-file path-in)
    (find-config-file file-name) (read-config-file (find-config-file file-name))
    :else default-config))

(defn load-config-file [default-config file-name path-in]
  (try+
   (do-load-config-file default-config file-name path-in)
   (catch java.io.IOException exception
     (error "Could not open the configuration file:" (.getMessage exception))
     (throw+ ))
   (catch Exception exception
     (error "Could not read the configuration file: " (.getMessage exception))
     (throw+))))

