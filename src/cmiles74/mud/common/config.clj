(ns cmiles74.mud.common.config
  "Provides commonly used configuration management functions."
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

(defn search-config-file
  "Returns a list of paths for a configuration file with the provided name.
  Currently this list includes the working directory for the VM as well as the
  active user's home directory."
  [file-name]
  [(str (System/getProperty "user.dir") "/" file-name)
   (str (System/getProperty "user.home") "/" file-name)])

(defn find-config-file
  "Searchs for a configuration file with the provided name and returns the first
  file found or nil if no configuration file was detected."
  [file-name]
  (some #(if (.exists (io/as-file %)) %) (search-config-file file-name)))

(defn read-config-file
  "Reads and parses the YAML configuration file at the provided path."
  [path-in]
  (yaml/parse-string (slurp path-in)))

(defn- do-load-config-file
  "Accepts a default configuration map, the name of a configuration file and,
  optionally, the path to an existing configuration file and attempts the
  following in the order listed below:

  - Load the configuration file specified by 'path-in'
  - Search for and load a configuration file named 'file-in'
  - If the above fail, returns the default configuration"
  [default-config file-name path-in]
  (cond
    path-in (read-config-file path-in)
    (find-config-file file-name) (read-config-file (find-config-file file-name))
    :else default-config))

(defn load-config-file
  "Accepts a default configuration map, the name of a configuration file and,
  optionally, the path to an existing configuration file and attempts the
  following in the order listed below:

  - Load the configuration file specified by 'path-in'
  - Search for and load a configuration file named 'file-in'
  - If the above fail, returns the default configuration

  Any errors are logged and then rethrown for further handling."
  [default-config file-name path-in]
  (try+
   (do-load-config-file default-config file-name path-in)
   (catch java.io.IOException exception
     (error "Could not open the configuration file:" (.getMessage exception))
     (throw+ ))
   (catch Exception exception
     (error "Could not read the configuration file: " (.getMessage exception))
     (throw+))))

