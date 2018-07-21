(defproject cmiles74/mud "0.1-SNAPSHOT"
  :description "A Simple Multi User Dungeon"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/tools.cli "0.3.7"]
                 [clj-yaml "0.4.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [slingshot "0.12.2"]
                 [javax.servlet/servlet-api "2.5"]
                 [ring "1.6.3"]
                 [compojure "1.6.1"]
                 [manifold "0.1.8"]
                 [org.slf4j/slf4j-simple "1.7.25"]
                 [aleph "0.4.6"]
                 [com.googlecode.lanterna/lanterna "3.0.1"]
                 [bidi "2.1.3"]
                 [yada "1.2.13"]
                 [cheshire "5.8.0"]]
  :main cmiles74.mud.client.clileinq

  :profiles {:uberjar {:aot :all}
             :dev {:source-paths ["dev"]}})
