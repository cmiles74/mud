(ns com.nervestaple.mud.cli.core
  (:require
   [clojure.string :as string]
   [clojure.tools.cli :as cli]))

;; exit codes
(def SUCCESS 0)
(def WARN 1)

(defn error-msg
  [errors]
  (str "I'm sorry, your request could not be parsed.\n\n"
       (string/join \newline errors)))

(defn exit
  [status message]
  (println message)
  (System/exit status))

(defn parse-cli-args
  [usage-text options validate-options-fn handle-args-fn args]
  (let [{:keys [options arguments summary errors]} (cli/parse-opts args options)]

    ;; display help usage
    (when (:help options)
      (exit 0 (->> [usage-text "" summary ""]
                   (string/join \newline))))

    ;;handle custom errors from validation
    (let [errors (validate-options-fn options arguments)]
      (when errors
        (exit WARN (error-msg errors))))

    (if errors
      ;; handle errors from the parser
      (exit 1 (error-msg errors))

      ;; start the application
      (handle-args-fn options arguments))))
