(ns com.nervestaple.mud.server.core
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [com.nervestaple.mud.cli.interface :as cli]
   [com.nervestaple.mud.config.interface :as config]
   [com.nervestaple.mud.log.interface :as log])
  (:gen-class))

(def CLI-USAGE
  (->> ["Mud Server"
        ""
        "This application provides the Mud server. If no options or parameters are"
        "provided, the server will be started with the default configuration."
        ""
        "Options:"]
       (string/join \newline)))

(def CLI-OPTIONS
  [["-?" "--help"                     "Display usage information"]
   ["-c" "--config         FILE-PATH" "Path to a configuration file"]
   ["-l" "--mud_log_level  LOG-LEVEL" "Log level threshold"]
   ["-o" "--mud_out_log    FILE-PATH" "Path to write log output"]
   ["-h" "--mud_host       HOSTNAME"  "Host name for the server"]
   ["-p" "--mud_port       PORT"      "Port number to listen on"]])

(defn log-level->key
  [level-in]
  (keyword (string/lower-case level-in)))

(defn validate-log-level
  [level-in]
  (let [level (log-level->key level-in)]
    (when-not (< 0 (count (filter #(= level %) log/LOG-LEVELS)))
      (str "The value \"" level-in "\" is not a valid log level: trace, "
           "debug, info, warn, error, fatal, report."))))

(defn cli-validate
  "Validates the provided command line options and arguments. If the options and
  arguments are not understood, an error is displayed and the application
  exits."
  [options arguments]
  (let [command (first arguments)]
    (seq
     (remove nil?
             [(when-not (nil? command)
                (str "I don't understand the \"" command "\" command."))

              (when (and (options :config)
                         (not (.exists (io/as-file (options :config)))))
                (str "The configuration file \"" (options :config)
                     "\" was not found"))

              (when (and (options :config)
                         (.isDirectory (io/as-file (options :config))))
                (str "The configuration file \"" (options :config)
                     "\" was not a file"))

              (when (and (options :config)
                         (not (.canRead (io/as-file (options :config)))))
                (str "The configuration file \"" (options :config)
                     "\" was not a readable"))

              (when (options :mud_log_level)
                (validate-log-level (options :mud_log_level)))]))))

(defn cli-handle-arguments
  "Parses the provided command line arguments and options, reads the application
  configuration and then starts the server."
  [options arguments]
  (let [config (merge (config/read-config "server/config.edn"
                                          (or (:config options) "config.edn")
                                          ["MUD_HOST"
                                           "MUD_PORT"
                                           "MUD_LOG_LEVEL"])
                      options)]

    ;; handle logging options
    (when (config :mud_out_log)
      (log/add-file (config :mud_out_log)))
    (when (config :mud_log_level)
      (log/set-min-level (log-level->key (config :mud_log_level))))

    (log/info "Welcome to the Mud server!")
    (log/debug "Using the configuration" config)))

(defn -main
  "Bootstrapping function for the server"
  [& args]
  (cli/parse-cli-args CLI-USAGE
                      CLI-OPTIONS
                      cli-validate
                      cli-handle-arguments
                      args))

(defn main
  "Calls the bootstrapping function with the provided arguments"
  [& args]
  (apply args -main))
