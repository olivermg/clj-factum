(defproject clj-factum/server "0.1.1-SNAPSHOT"
  :description "server for clj-factum"
  :url "https://github.com/olivermg/clj-factum/tree/master/backends/postgres"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.442"]
                 [com.stuartsierra/component "0.3.2"]
                 [environ "1.1.0"]
                 ;;;[org.clojure/java.jdbc "0.7.0-alpha3"]
                 [org.postgresql/postgresql "42.1.1"]
                 #_[heroku-database-url-to-jdbc "0.2.2"]

                 [clj-factum/serverlib "0.1.1-SNAPSHOT"]
                 [clj-factum/websocket-transport "0.1.1-SNAPSHOT"]
                 [clj-factum/postgres-backend "0.1.1-SNAPSHOT"]

                 ;;;[com.taoensso/sente "1.11.0"]
                 ;;;[liberator "0.14.1"]
                 ;;;[bidi "2.0.17"]
                 ;;;[hiccup "1.0.5"]
                 ;;;[cheshire "5.7.1"]
                 ;;;[metosin/compojure-api "1.1.10"]
                 ;;;[buddy/buddy-auth "1.4.1"]

                 ;;; tcp/http server:
                 ;;;[aleph "0.4.3"]
                 ;;;[clj-chatterbox "0.1.0-SNAPSHOT"]
                 ]

  :plugins [[lein-environ "1.1.0"]]

  :min-lein-version "2.6.1"

  :source-paths ["src/clj"]
  ;;;:java-source-paths ["src/java"]
  :test-paths ["test/clj"]

  :target-path "target/%s"

  :main ^:skip-aot ow.factum.server

  :profiles {:uberjar {:aot :all}}

  )
