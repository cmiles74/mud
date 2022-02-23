(ns com.nervestaple.mud.cli.interface-test
  (:require [clojure.test :as test :refer [deftest testing is]]
            [com.nervestaple.mud.cli.interface :as cli]))

(deftest test-cli-start
  (testing "Valid options and arguments"
    (is (cli/parse-cli-args "usage" [["-h" "--help" "Help"]]
                            (fn [& args] (count args) nil)
                            (fn [& args] args)
                            []))))
