(ns ^{:doc "Provides commonly used command-line oriented utility functions."}
    cmiles74.mud.common.cli
  (:require
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.string :as string]))

(defn error-msg
  "Combines the error messages into a list suitable for display."
  [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit
  "Prints the provided message and then exits the VM with the specified status
  code."
  [status msg]
  (println msg)
  (System/exit status))
