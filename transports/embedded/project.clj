(defproject clj-factum/embedded-transport "0.1.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.442"]
                 #_[com.stuartsierra/component "0.3.2"]]

  :min-lein-version "2.6.1"

  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :test-paths ["test/clj"]

  :target-path "target/%s"

  ;;;:aot :all
  ;;;:omit-source true

  ;;;:profiles {:uberjar {:aot :all
  ;;;                     :omit-source true}}

  )
