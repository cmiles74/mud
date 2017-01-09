(defproject cmiles74/mud "0.1-SNAPSHOT"
  :description "A Simple Multi User Dungeon"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.374"]
                 [org.clojure/tools.cli "0.3.3"]
                 [clj-yaml "0.4.0"]
                 [com.taoensso/timbre "4.2.1"]
                 [slingshot "0.12.2"]
                 [ring "1.4.0"]
                 [compojure "1.3.4"]
                 [manifold "0.1.1"]
                 [aleph "0.4.1-beta2"]
                 [com.googlecode.lanterna/lanterna "3.0.0-beta1"]
                 [bidi "1.25.0"]
                 [yada "1.1.0-20160125.190302-12"]
                 [com.apa512/rethinkdb "0.11.0"]]
  :main cmiles74.mud.client.cli

  :profiles {:uberjar {:aot :all}
             :dev {:source-paths ["dev"]}})
