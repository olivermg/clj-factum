(defproject clj-factum/clientlib "0.1.1-SNAPSHOT"
  :description "eventsourcing library"
  :url "https://github.com/olivermg/clj-factum/tree/master/library"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.442"]
                 [org.clojure/core.logic "0.8.11"]

                 [clj-factum/serverlib "0.1.1-SNAPSHOT"] ;; TODO: get rid of this dependency
                 ]

  #_:plugins #_[#_[lein-cljsbuild "1.1.3"]
            [lein-environ "1.0.3"]]

  :min-lein-version "2.6.1"

  :source-paths ["src/clj" #_"src/cljs" #_"src/cljc"]
  :java-source-paths ["src/java"]
  :test-paths ["test/clj" #_"test/cljc"]

  :target-path "target/%s"

  ;;;:aot :all
  ;;;:omit-source true

  ;;;:profiles {:uberjar {:aot :all
  ;;;                     :omit-source true}}

  ;;;:clean-targets ^{:protect false} [:target-path :compile-path #_"resources/public/js"]

  ;;;:uberjar-name "clj-factum.jar"

  ;; Use `lein run` if you just want to start a HTTP server, without figwheel
  ;;;:main ^:skip-aot ow.factum.server

  ;; nREPL by default starts in the :main namespace, we want to start in `user`
  ;; because that's where our development helper functions like (run) and
  ;; (browser-repl) live.
  ;;;:repl-options {:init-ns user}

  #_:cljsbuild #_{:builds
              [{:id "app"
                :source-paths ["src/cljs" "src/cljc"]

                :figwheel true
                ;; Alternatively, you can configure a function to run every time figwheel reloads.
                ;; :figwheel {:on-jsload "eventsourcing.core/on-figwheel-reload"}

                :compiler {:main eventsourcing.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/eventsourcing.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}

               {:id "test"
                :source-paths ["src/cljs" "test/cljs" "src/cljc" "test/cljc"]
                :compiler {:output-to "resources/public/js/compiled/testable.js"
                           :main eventsourcing.test-runner
                           :optimizations :none}}

               {:id "min"
                :source-paths ["src/cljs" "src/cljc"]
                :jar true
                :compiler {:main eventsourcing.core
                           :output-to "resources/public/js/compiled/eventsourcing.js"
                           :output-dir "target"
                           :source-map-timestamp true
                           :optimizations :advanced
                           :pretty-print false}}]}

  ;; When running figwheel from nREPL, figwheel will read this configuration
  ;; stanza, but it will read it without passing through leiningen's profile
  ;; merging. So don't put a :figwheel section under the :dev profile, it will
  ;; not be picked up, instead configure figwheel here on the top level.

  #_:figwheel #_{;; :http-server-root "public"       ;; serve static assets from resources/public/
             ;; :server-port 3449                ;; default
             ;; :server-ip "127.0.0.1"           ;; default
             :css-dirs ["resources/public/css"]  ;; watch and update CSS

             ;; Instead of booting a separate server on its own port, we embed
             ;; the server ring handler inside figwheel's http-kit server, so
             ;; assets and API endpoints can all be accessed on the same host
             ;; and port. If you prefer a separate server process then take this
             ;; out and start the server with `lein run`.
             :ring-handler user/http-handler

             ;; Start an nREPL server into the running figwheel process. We
             ;; don't do this, instead we do the opposite, running figwheel from
             ;; an nREPL process, see
             ;; https://github.com/bhauman/lein-figwheel/wiki/Using-the-Figwheel-REPL-within-NRepl
             ;; :nrepl-port 7888

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             :server-logfile "log/figwheel.log"}

  ;;;:doo {:build "test"}

  #_:profiles #_{:dev
             {:dependencies [#_[figwheel "0.5.4-4"]
                             #_[figwheel-sidecar "0.5.4-4"]
                             #_[com.cemerick/piggieback "0.2.1"]
                             [org.clojure/tools.nrepl "0.2.12"]]

              :plugins [#_[lein-figwheel "0.5.4-4"]
                        #_[lein-doo "0.1.6"]]

              :source-paths ["dev"]
              #_:repl-options #_{:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}

             :uberjar
             {:source-paths ^:replace ["src/clj" #_"src/cljc"]
              :prep-tasks ["compile" #_["cljsbuild" "once" "min"]]
              :hooks []
              :omit-source true
              :aot :all}

             :default [:base :system :user :provided :dev :clj-factum-dev]})
