(defproject clj-factum/postgres-backend "0.1.1-SNAPSHOT"
  :description "postgresql backend for clj-factum"
  :url "https://github.com/olivermg/clj-factum/tree/master/server"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]

                 [clj-factum/serverlib "0.1.1-SNAPSHOT"]

                 ;;; rdbms database:
                 ;;;[org.clojure/java.jdbc "0.6.1"]                       ;; jdbc for clojure
                 [org.postgresql/postgresql "9.4.1211"]                ;; postgres driver
                 ;;;[sqlingvo "0.8.14"]                                   ;; sql syntax abstraction layer
                 [korma "0.4.3"]                                       ;; sql & db abstraction layer
                 [heroku-database-url-to-jdbc "0.2.2"]                 ;; convert heroku db url to korma compatible config map
                 ]

  :min-lein-version "2.6.1"

  :source-paths ["src/clj"]

  :test-paths ["test/clj"]

  )
