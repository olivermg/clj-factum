(defproject sampleclient "0.1.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :min-lein-version "2.0.0"

  :dependencies [#_[org.clojure/clojure "1.9.0-alpha16"]
                 [org.clojure/clojure "1.8.0"]
                 [duct/core "0.3.2"]
                 [duct/module.logging "0.2.0"]
                 ;;;[duct/module.web "0.3.0"]

                 [clj-factum/websocket-transport "0.1.1-SNAPSHOT"]
                 [clj-factum/clientlib "0.1.1-SNAPSHOT"]]

  :plugins [[duct/lein-duct "0.9.0-alpha5"]]

  :main ^:skip-aot sampleclient.main

  :duct {:config-paths ["resources/sampleclient/config.edn"]}

  :resource-paths ["resources" "target/resources"]

  :prep-tasks     ["javac" "compile" ["duct" "compile"]]

  :profiles {:dev     [:project/dev :profiles/dev]

             :repl    {:prep-tasks   ^:replace ["javac" "compile"]
                       :repl-options {:init-ns user}}

             :uberjar {:aot :all}

             :profiles/dev {}

             :project/dev  {:source-paths   ["dev/src"]
                            :resource-paths ["dev/resources"]
                            :dependencies   [[integrant/repl "0.2.0"]
                                             [eftest "0.3.0"]
                                             [kerodon "0.8.0"]]}})
