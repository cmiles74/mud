(ns com.nervestaple.mud.game.interface-test
  (:require [clojure.test :as test :refer :all]
            [com.nervestaple.mud.game.interface :as game]))

(deftest test-add-client
  (testing "Adds a new client and socket"
    (let [client (game/add-client "Bob" "bob733t" {})]
      (is (some #(= (:id client) %) (game/get-client-ids))))))

(deftest test-add-anonymous-client
  (testing "Adds a new anonymous client"
    (let [client (game/add-anonymous-client {})]
      (is (some #(= (:id client) %) (game/get-client-ids))))))

(deftest test-get-client-room-id
  (testing "Verifies we can get the room for a client"
    (let [client (game/add-anonymous-client {})]
      (game/move-client-room (:id client) 2)
      (is (game/get-client-room-id (:id client))))))

(deftest test-move-client-room
  (testing "Verifies we can move a client to another room"
    (let [client (game/add-anonymous-client {})]
      (game/move-client-room (:id client) 2)
      (is (= 2 (game/get-client-room-id (:id client)))))))

(deftest test-get-client-room
  (testing "Verifies we can fetch the client's room"
    (let [client (game/add-anonymous-client {})]
      (game/move-client-room (:id client) 2)
      (is (map? (game/get-client-room (:id client)))))))

(deftest test-get-room-client-ids
  (testing "Fetches the IDs for the clients in a room"
    (let [client (game/add-anonymous-client {})]
      (game/move-client-room (:id client) 2)
      (< 0 (count (game/get-room-client-ids 2))))))

(deftest test-get-room-clients
  (testing "Fetches the clients in a room"
    (let [client (game/add-anonymous-client {})]
      (game/move-client-room (:id client) 2)
      (and (< 0 (count (game/get-room-clients 2)))
           (map? (first (game/get-room-clients 2)))))))

(deftest test-remove-client
  (testing "Removes a client and socket"
    (let [client (game/add-client "Jim" "dad2008" {})]
      (game/remove-client (:id client))
      (is (not (some #(= (:id client) %) (game/get-client-ids)))))))
