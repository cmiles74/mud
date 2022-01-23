(ns com.nervestaple.config.interface-test
  (:require [clojure.test :as test :refer [deftest testing is]]
            [com.nervestaple.config.interface :as config]))

(deftest test-read-from-resource
  (testing "Ensure a map is always returned when reading a resource")
  (is (config/read-from-resource "config.edn")))

(deftest test-read-from-file
  (testing "Ensure a map is always returned when reading a file")
  (is (config/read-from-file "config.edn")))

(deftest test-read-from-file-2
  (testing "Ensure we can read a file")
  (let [config-this (config/read-from-file "deps.edn")]
    (is (and config-this
             (< 0 (count (keys config-this)))))))

(deftest test-read-from-environment
  (testing "Ensure a map is always returned when reading from the environment")
  (is (config/read-from-environment ["VAR_1" "VAR_2" "VAR_3"])))

(deftest test-read-from-environment-2
  (testing "Ensure we can read from the environment")
  (let [config-this (config/read-from-environment ["PATH"])]
    (is (and config-this
             (< 0 (count (keys config-this)))))))

(deftest test-read-config
  (testing "Ensure a map is returned when reading/ merging configuration data")
  (is (config/read-config nil "deps.edn" ["PATH"])))
