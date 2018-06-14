(defproject sample "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.logic "0.8.11"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/tools.logging "0.4.1"]

                 [clj-karabiner "0.1.1-SNAPSHOT"]

                 #_[clj-factum/serverlib "0.1.1-SNAPSHOT"]
                 #_[clj-factum/embedded-transport "0.1.1-SNAPSHOT"]
                 #_[clj-factum/clientlib "0.1.1-SNAPSHOT"]

                 [spootnik/kinsky "0.1.22"]]

  :main ^:skip-aot sample.core

  :target-path "target/%s"

  :profiles {:uberjar {:aot :all}})
