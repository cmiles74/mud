(ns cmiles74.mud.server.game
  "Provides functions for managing the game"
  (:require
   [taoensso.timbre :as timbre
    :refer (log  trace  debug  info  warn  error  fatal  report
                 logf tracef debugf infof warnf errorf fatalf reportf
                 spy get-env log-env)]
   [taoensso.timbre.appenders.core :as appenders]
   [taoensso.timbre.profiling :as profiling
    :refer (pspy pspy* profile defnp p p*)]
   [manifold.stream :as stream]
   [slingshot.slingshot :only [throw+ try+]]
   [cheshire.core :as json]))

;; map of rooms to clients
(def rooms-clients (ref {}))

;; map of clients to rooms
(def clients-rooms (ref {}))

(def rooms
  {1 {:name "Room 1"
      :description "A red room with a large number \"1\" on the floor."
      :exits {"east" 2}}
   2 {:name "Room 2"
      :description "A green room with a large number \"2\" on the floor."
      :exits {"west" 1}}})

(defn post-message-to-client
  "Posts a message from one client to another."
  [client-from message client-to]
  (stream/put! (client-to :websocket)
               (json/generate-smile {:type "message"
                                     :from (client-from :name)
                                     :content (message :content)})))

(defn notify-client-move
  "Notifies the client that they mave moved to another room."
  [client]
  (post-message-to-client {:name "System"}
                          {:type "move"
                           :content (rooms (:room client))}
                          client))

(defn post-message-room
  "Posts a message to all the clients in the room."
  [client message]
  (let [room (@clients-rooms (client :name))
        clients (@rooms-clients room)]
    (dorun (map (partial post-message-to-client client message) clients))))

(defn move-client
  "Moves the provided client to the specified target room and then notifies the
  client that they have moved."
  [client target-room]
  (dosync (alter rooms-clients assoc target-room (conj (rooms-clients target-room) client))
          (alter clients-rooms assoc (:name client) target-room))
  (notify-client-move client))

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
    "message" (post-message-room client message)))
