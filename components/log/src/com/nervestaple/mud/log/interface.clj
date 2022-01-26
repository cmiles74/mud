(ns com.nervestaple.mud.log.interface
  (:require
   [com.nervestaple.mud.log.core :as core]))

;; sequence of valid log levels
(def LOG-LEVELS core/LOG-LEVELS)

(defn add-file
  "Adds a new \"spit\" appender to the current log configuration, all log
  messages will be written to this file. Returns the current logging
  configuration.

  You probably want to wrap calls to this function in a `(defonce ...)`."
  [file-name]
  (core/add-file file-name))

(defn set-ns-log-level
  "Accepts either a namespace and a key with a log level or a sequence where each
  item is a sequence with a namespace and a key with a log level. This data is
  used to set the log level for the provided namespaces."
  ([namespace-to-log-levels]
   (core/set-ns-log-level namespace-to-log-levels))
  ([namespace log-level]
   (core/set-ns-log-level namespace log-level)))

(defn set-min-level
  "Sets the minimum logging level and returns the current logging configuration."
  [level]
  (core/set-min-level level))

(defmacro with-level
  [level & args]
  `(core/with-level ~level ~@args))

(defmacro trace
  "Logs a trace message to the log stream"
  [& args]
  `(core/trace ~@args))

(defmacro debug
  "Logs a debug message to the log stream."
  [& args]
  `(core/debug ~@args))

(defmacro info
  "Logs an informative message to the log stream."
  [& args]
  `(core/info ~@args))

(defmacro warn
  "Logs a warning to the log stream."
  [& args]
  `(core/warn ~@args))

(defmacro error
  "Logs an error to the log stream."
  [& args]
  `(core/error ~@args))

(defmacro fatal
  "Logs a fatal message to the log stream."
  [& args]
  `(core/fatal ~@args))

(defmacro report
  "Logs a report message to the log stream."
  [& args]
  `(core/report ~@args))

