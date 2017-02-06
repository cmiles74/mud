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

  (def rooms
    {1 {:name "Room 1"
        :description "A red room with a large number \"1\" on the floor."
        :exits {"east" 2}}
     2 {:name "Room 2"
        :description "A green room with a large number \"2\" on the floor."
        :exits {"west" 1}}})

  (defn describe-room
    [client]
    (info ((rooms (:room client)) :description))
    (stream/put! (:websocket client)
                 (json/generate-smile {:type "room"
                                       :content (rooms (:room client))})))

  (defn initialize-client
    [client]

    ;; place the client in the starting room
    (dosync (alter rooms-clients assoc (:room client) (:name client)))

    ;; describe the room to the client
    (describe-room client))
