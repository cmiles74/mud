(ns com.nervestaple.mud.server.core
  (:require
   [com.nervestaple.mud.log.interface :as log])
  (:gen-class))

(defn -main
  "Bootstrapping function for the server"
  [& args]
  (log/info "Mud Server, starting up!"))

(defn main
  [& args]
  (apply args -main))
