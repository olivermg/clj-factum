(ns ow.factum.server
  (:require [clojure.edn :as edn]
            [clojure.core.async :refer [go-loop <!]]
            [com.stuartsierra.component :as c]
            [environ.core :refer [env]]
            #_[heroku-database-url-to-jdbc.core :as hc]
            #_[clojure.java.jdbc :as jdbc]
            [ow.factum.transport.websocket.server :as ts]
            [ow.factum.dbpoller :as dbp]
            #_[ow.factum.backend.memory :as bm]
            [ow.factum.backend.postgres :as bp]
            #_[taoensso.sente :as sente]
            #_[manifold.stream :as s]
            #_[manifold.deferred :as d]
            #_[aleph.tcp :as tcp]
            #_[aleph.netty :as netty]
            #_[bidi.bidi :refer [match-route path-for] :as bb]
            #_[liberator.core :refer [defresource resource] :as l]
            #_[buddy.sign.jwt :as jwt]
            #_[buddy.auth :refer [authenticated? throw-unauthorized]]
            #_[ow.chatterbox.core :refer [webapp]])
  #_(:import [java.io FileNotFoundException])
  (:gen-class))

(defn- get-config []
  {:port (Integer. (or (env :port) "8899"))
   :interval (Integer. (or (env :interval) "1000"))
   :jdbc {:connection-uri (or (env :jdbc-database-url))}})

(defn -main [& args]
  (print "starting... ")
  (let [{:keys [jdbc port interval] :as cfg} (get-config)
        _ (println cfg)
        backend (-> (bp/new-postgresbackend jdbc) c/start)
        dbpoller (dbp/dbpoller backend :poll-interval interval)
        transport-server (ts/websocket-server (:on-connect dbpoller)
                                              :port port)
        #_webapp #_(webapp cfg) #_(tcp/start-server echo-server {:port port})]
    (println "done.")
    ;;;(c/start webapp)
    ;;;(netty/wait-for-close s)
    (c/start transport-server)
    ;;;(println "quit.")
    ))
