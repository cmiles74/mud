(ns user
  (:require
   [taoensso.timbre :as timbre
    :refer (log  trace  debug  info  warn  error  fatal  report
                 logf tracef debugf infof warnf errorf fatalf reportf
                 spy get-env log-env)]
   [taoensso.timbre.profiling :as profiling
    :refer (pspy pspy* profile defnp p p*)]
   [slingshot.slingshot :only [throw+ try+]]
   [cmiles74.mud.server.cli :as cli]
   [cmiles74.mud.server.mud :as mud]
   [cmiles74.mud.server.mud :as server]))

(defn start-server []
  (server/start-server cli/DEFAULT-CONFIG))
