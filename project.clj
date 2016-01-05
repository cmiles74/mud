(defproject cmiles74/mud "0.1-SNAPSHOT"
  :description "A Simple Multi User Dungeon"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/tools.cli "0.3.3"]
                 [clj-yaml "0.4.0"]
                 [com.taoensso/timbre "4.0.1"]
                 [slingshot "0.12.2"]
                 [dire "0.5.3"]
                 [ring "1.4.0-RC1"]
                 [io.netty/netty-all "4.1.0.Beta6"]
                 [compojure "1.3.4"]
                 [manifold "0.1.0"]
                 [aleph "0.4.0"
                  :exclusions [[io.netty/netty-all]]]
                 [com.googlecode.lanterna/lanterna "3.0.0-beta1"]
                 [yada "1.0.0-20150903.093751-9"]]
  :main cmiles74.mud.client.cli
  :repositories [["nervestaple"
                  {:url "http://nexus.nervestaple.com/content/groups/public"}]]

  :profiles {:uberjar {:aot :all}
             :dev {:source-paths ["dev"]}})
