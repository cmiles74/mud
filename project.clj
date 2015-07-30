(defproject cmiles74/mud "0.1-SNAPSHOT"
  :description "A Simple Multi User Dungeon"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.taoensso/timbre "4.0.1"]
                 [slingshot "0.12.2"]
                 [dire "0.5.3"]
                 [ring "1.4.0-RC1"]
                 [compojure "1.3.4"]
                 [manifold "0.1.0"]
                 [aleph "0.4.0"]
                 [com.googlecode.lanterna/lanterna "3.0.0-SNAPSHOT"]]
  :main cmiles74.client.mud
  :repositories [["nervestaple"
                  {:url "http://nexus.nervestaple.com/content/groups/public"}]]

  :profiles {:uberjar {:aot :all}
             :dev {:source-paths ["dev"]}})
