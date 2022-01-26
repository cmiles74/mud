(ns com.nervestaple.mud.server.game
  "Provides functions for managing the game"
  (:require
   [taoensso.timbre :as timbre
    :refer (info  warn)]
   [manifold.stream :as stream]
   [cheshire.core :as json]
   [clojure.string :as string]
   [com.nervestaple.mud.server.data :as data]))

(defn post-message-to-client
  "Posts a message from one client to another."
  [client-from message client-to]
  (stream/put! (client-to :connection)
               (json/generate-smile {:type "message"
                                     :from (client-from :handle)
                                     :content (message :content)})))

(defn post-move-to-client
  "Posts a message to the client indicating that it has moved."
  [message client-to]
  (stream/put! (client-to :connection)
               (json/generate-smile {:type "move"
                                     :from "System"
                                     :content (message :content)})))

(defn notify-client-move
  "Notifies the client that they mave moved to another room."
  [client]
  (post-move-to-client {:type "move"
                        :content (data/get-client-room (client :id))}
                       client))

(defn post-message-room
  "Posts a message to all the clients in the room."
  [client message]
  (let [room-id (data/get-client-room-id (:id client))
        clients (data/get-room-clients room-id)]
    (dorun (map (partial post-message-to-client client message) clients))))

(defn post-unknown-command
  [client command argument]
  (stream/put! (client :connection)
               (json/generate-smile
                {:type "error"
                 :from "system"
                 :content (str "I don't know how to " command " with \""
                               argument "\".")})))

(defn post-error
  [client message]
  (stream/put! (client :connection)
               (json/generate-smile
                {:type "error"
                 :from "system"
                 :content message})))

(defn move-client
  "Moves the provided client to the specified target room and then notifies the
  client that they have moved."
  [client target-room]
  (data/move-client-room (client :id) target-room)
  (notify-client-move client))

(defn handle-move
  [client argument]
  (let [arguments (string/split argument #"\s")
        room (data/get-client-room (client :id))
        target (if room ((room :exits) (first arguments)) nil)]
    (if target
      (move-client client target)
      (post-error client (str "You cannot move \"" (first arguments) "\" from here.")))))

;; map of commands to functions
(def commands {"move" handle-move})

(defn handle-command
  [client command argument]
  (info (str "Handling command \"" command "\" with argument \"" argument "\" for client " (client :handle)))
  (let [handler-fn (commands command)]
    (if handler-fn
      (try
        (handler-fn client argument)
        (catch Exception exception
          (warn exception)))
      (post-unknown-command client command argument))))

(defn initialize-client
  "Sets up the game for a client."
  [client]
  (try
    (move-client client 1)
    (catch Exception e
      (warn e))))

(defn handle-incoming-message
  "Handles incoming messages from the client. All incoming messages will be maps of data."
  [client message]
  (case (message :type)
    "message" (post-message-room client message)
    (handle-command client (message :type) (message :content))))
