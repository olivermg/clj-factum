(defproject clj-factum/server "0.1.0-SNAPSHOT"
  :description "server for clj-factum"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]

                 [clj-factum "0.1.0-SNAPSHOT"]

                 ;;; tcp server:
                 [aleph "0.4.3"]
                 ]

  :min-lein-version "2.6.1"

  :source-paths ["src/clj"]

  :test-paths ["test/clj"]

  :main ^:skip-aot ow.factum.server

  ;;;:target-path "target/%s"

  :profiles {:uberjar {:aot :all}}

  )
