(defproject javaapp "0.1.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]

                 [clj-factum/embedded-transport "0.1.1-SNAPSHOT"]
                 [clj-factum/clientlib "0.1.1-SNAPSHOT"]]

  :min-lein-version "2.6.1"

  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]

  :test-paths ["test/clj"]

  :main javaapp.Main

  ;;;:target-path "target/%s"

  :profiles {:uberjar {:aot :all
                       :omit-source true}})
