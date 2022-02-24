(ns com.nervestaple.mud.terminal.interface-test
  (:require [clojure.test :as test :refer :all]
            [com.nervestaple.mud.terminal.interface :as terminal]))

(deftest test-create
  (testing "Creates a new terminal"
    (let [ts (terminal/create)]
      (terminal/dispose ts)
      (is ts))))

(deftest test-size
  (testing "Fetch the size of the terminal"
    (let [ts (terminal/create)]
      (try
        (is (and (vector? (terminal/size ts))
                 (= 2 (count (terminal/size ts)))))
        (finally (terminal/dispose ts))))))

(deftest test-move-cursor
  (testing "Moves the cursor and fetches it's position"
    (let [ts (terminal/create)]
      (try
        (terminal/move-cursor ts 10 10)
        (is (= [10 10] (terminal/cursor-position ts)))
        (finally (terminal/dispose ts))))))

(deftest write-char
  (testing "Writes a character onto the terminal"
    (let [ts (terminal/create)]
      (try
        (terminal/write-char ts 10 10 \!)
        (is (= "{80x24}\n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n          !                                                                     \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n"
               (str ts)))
        (finally (terminal/dispose ts))))))

(deftest test-write-string
  (testing "Writes a string to the terminal"
    (let [ts (terminal/create)]
      (try
        (terminal/write-string ts 10 10 "Hello world!")
        (is (= "{80x24}\n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n          Hello world!                                                          \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n"
               (str ts)))
        (finally (terminal/dispose ts))))))

(deftest test-scroll-up
  (testing "Scrolls the terminal up"
    (let [ts (terminal/create)]
      (try
        (terminal/write-string ts 10 10 "Hello world!")
        (terminal/scroll-up ts 1 23)
        (is (= "{80x24}\n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n          Hello world!                                                          \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n"
               (str ts)))
        (finally (terminal/dispose ts))))))

(deftest test-clear
  (testing "Writes a string to the terminal"
    (let [ts (terminal/create)]
      (try
        (terminal/write-string ts 10 10 "Hello world!")
        (terminal/clear ts)
        (is (= "{80x24}\n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n                                                                                \n"
               (str ts)))
        (finally (terminal/dispose ts))))))
