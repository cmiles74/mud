(ns com.nervestaple.mud.client.core
  (:require
   [com.nervestaple.mud.log.interface :as log])
  (:gen-class))

(defn -main
  "Bootstrapping function for the server"
  [& args]
  (log/info "Welcome to the Mud client!"))

(defn main
  [& args]
  (apply args -main))

