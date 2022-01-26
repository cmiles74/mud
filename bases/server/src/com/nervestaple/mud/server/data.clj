(ns com.nervestaple.mud.server.data)

;; map of the dungeon rooms
(def rooms
  {1 {:name "Room 1"
      :description "A red room with a large number \"1\" on the floor."
      :exits {"east" 2}}
   2 {:name "Room 2"
      :description "A green room with a large number \"2\" on the floor."
      :exits {"west" 1}}})

;;
;; map of clients
;;
;; The key is the id of the client (an integer) and a map of the client's data.
;;
(def clients (ref {}))

;;
;; map of rooms to clients
;;
;; the room id is the key, the value is a list of the clients in that room.
(def room-clients (ref {}))

;;
;; map of clients to rooms
;;
;; the client id is the key, the value is the room the client is currently
;; associated with.
(def client-room (ref {}))

(defn add-client
  "Adds a new client and their connection; this function is used to keep track
  of connected clients.

  - anonymous?, bool            Indicates if the client is anonymous
  - friendly-name, string       The client's friendly name
  - handle, string              The client's account name
  - connection, deferred (map)  Manifold deferred representing the websocket"
  [anonymous? friendly-name handle connection]
  (dosync
   (let [id (count @clients)
         client {:id id
                 :anonymous? anonymous?
                 :friendly-name friendly-name
                 :handle handle
                 :connection connection}]
     (alter clients assoc id client))))

(defn add-anonymous-client
  "Adds a new client and their connection; this function is used to keep track of
  connected clients. The 'connection; should be a Manifold deferred representing
  the websocket"
  [connection]
  (dosync
   (let [id (count @clients)
         client {:id id
                 :anonymous? true
                 :friendly-name (str "Anonymous #" id)
                 :handle (str "anon-" id)
                 :connection connection}]
     (alter clients assoc id client)
     client)))

(defn get-client
  "Returns the client with the given unique identifier."
  [id]
  (@clients id))

(defn get-room
  "Returns the room with the given unique identifier."
  [id]
  (rooms id))

(defn assoc-client-room
  "Associated the provided client identifier with the provided room identifier."
  [client-id room-id]
  (dosync
   (let [clients-in (@room-clients room-id)
         clients-out (if (nil? clients-in) [] clients-in)]
     (alter room-clients assoc room-id (conj clients-out client-id))
     (alter client-room assoc client-id room-id))))

(defn dissoc-client-room
  "Disassociate (remove) the provided client identifier with the provided room
  identifier."
  [client-id room-id]
  (dosync
   (let [clients-out (@room-clients room-id)]
     (if (not (nil? clients-out))
       (alter room-clients assoc room-id (remove #(= %1 client-id) clients-out)))
     (alter client-room assoc client-id nil))))

(defn get-client-room-id
  "Returns the room identifier that is currently associated with the provided
  client identifier."
  [client-id]
  (@client-room client-id))

(defn move-client-room
  "Removes the client from the room they are currently associated with and
  associates them with the room that matches the provided room identifier."
  [client-id room-id]
  (dosync
   (dissoc-client-room client-id (get-client-room-id client-id))
   (assoc-client-room client-id room-id)))

(defn get-client-room
  "Returns the room that is currently associated with the provided client
  identifier."
  [client-id]
  (get-room (get-client-room-id client-id)))

(defn get-room-client-ids
  "Returns a list of the clients identifiers associated with the provided room
  identifier."
  [room-id]
  (@room-clients room-id))

(defn get-room-clients
  "Returns a list of the clients associated with the provided room identifier."
  [room-id]
  (map get-client (get-room-client-ids room-id)))

(defn remove-client
  "Removes the client with the given unique identifier."
  [id]
  (dosync (dissoc-client-room id (get-client-room-id id))
          (alter clients dissoc id)
          (alter client-room dissoc id)))
