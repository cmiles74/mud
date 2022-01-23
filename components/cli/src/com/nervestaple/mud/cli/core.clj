(ns com.nervestaple.mud.cli.core
  (:require
   [clojure.string :as string]
   [clojure.tools.cli :as cli]))

(defn error-msg
  "Displays an error message on the console."
  [errors]
  (str "I'm sorry, your request could not be parsed.\n\n"
       (string/join \newline errors)))

(defn exit
  "Displays the provided message and then exits the runtime with the given
  status."
  [status message]
  (println message)
  (System/exit status))

(defn parse-cli-args
  [usage-text options validate-options-fn handle-args-fn args]
  (let [{:keys [options arguments summary errors]} (cli/parse-opts args options)]

    (when (:help options)
      (exit 0 (->> [usage-text "" summary ""]
                   (string/join \newline))))

    (let [errors (validate-options-fn options arguments)]
      (when errors
        (exit 0 (->> [errors ""]
                     (string/join \newline)))))

    (cond
      errors
      (exit 1 (error-msg errors))

      (not errors)
      (handle-args-fn options arguments)

      :else
      (exit 0 (usage-text summary)))))
