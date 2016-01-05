(ns cmiles74.mud.common.cli
  (:require
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.string :as string]))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))
