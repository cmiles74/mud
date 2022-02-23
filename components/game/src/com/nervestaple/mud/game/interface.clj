(ns com.nervestaple.mud.game.interface
  (:require [com.nervestaple.mud.game.core :as core]))

(defn add-client
  "Adds a new client and their connection; this function is used to keep track
  of connected clients.

  - friendly-name, string       The client's friendly name
  - handle, string              The client's account name
  - connection, deferred (map)  Manifold deferred representing the websocket"
  [friendly-name handle connection]
  (core/add-client friendly-name handle connection))

(defn get-client-ids
  "Returns a list of all unique client identifiers."
  []
  (core/get-client-ids))

(defn add-anonymous-client
  "Adds a new client and their connection; this function is used to keep track of
  connected clients. The 'connection' should be a Manifold deferred representing
  the websocket"
  [connection]
  (core/add-anonymous-client connection))

(defn get-client-room-id
  "Returns the room identifier that is currently associated with the provided
  client identifier."
  [client-id]
  (core/get-client-room-id client-id))

(defn move-client-room
  "Removes the client from the room they are currently associated with and
  associates them with the room that matches the provided room identifier."
  [client-id room-id]
  (core/move-client-room client-id room-id))

(defn get-client-room
  "Returns the room that is currently associated with the provided client
  identifier."
  [client-id]
  (core/get-client-room client-id))

(defn get-room-client-ids
  "Returns a list of the clients identifiers associated with the provided room
  identifier."
  [room-id]
  (core/get-room-client-ids room-id))

(defn get-room-clients
  "Returns a list of the clients associated with the provided room identifier."
  [room-id]
  (core/get-room-clients room-id))

(defn remove-client
  "Removes the client with the given unique identifier."
  [id]
  (core/remove-client id))
