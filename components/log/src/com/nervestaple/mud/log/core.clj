(ns com.nervestaple.mud.log.core
  (:require
   [taoensso.timbre :as timbre]
   [taoensso.timbre.appenders.core :as appenders]))

(defn add-file
  [file-name]
  (timbre/merge-config!
   {:appenders {:spit (appenders/spit-appender {:fname file-name})}}))

(defn set-ns-log-level
  ([namespace log-level]
   (set-ns-log-level [[namespace log-level]]))
  ([namespace-to-log-levels]
   (timbre/merge-config!
    (assoc timbre/*config*
           :min-level
           (if (keyword? (:min-level timbre/*config*))
             (into namespace-to-log-levels
                   [["*" (:min-level timbre/*config*)]])
             (vec (distinct (into namespace-to-log-levels
                                  (:min-level timbre/*config*)))))))))

(defn set-min-level
  [level]
  (timbre/set-level! level))

(defmacro with-level
  [level & args]
  `(timbre/with-level ~level ~@args))

(defmacro trace
  [& args]
  `(timbre/trace ~@args))

(defmacro debug
  [& args]
  `(timbre/debug ~@args))

(defmacro info
  [& args]
  `(timbre/info ~@args))

(defmacro warn
  [& args]
  `(timbre/warn ~@args))

(defmacro error
  [& args]
  `(timbre/error ~@args))

(defmacro fatal
  [& args]
  `(timbre/fatal ~@args))

(defmacro report
  [& args]
  `(timbre/report ~@args))
