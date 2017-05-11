(ns ow.factum.server
  (:require [clojure.edn :as edn]
            [clojure.core.async :refer [go-loop <!]]
            [com.stuartsierra.component :as c]
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
  (:import [java.io FileNotFoundException])
  (:gen-class))

(def ^:private +default-config+
  {:port 8899})

(defn- config []
  (let [cfg (try
              (-> (slurp "config.edn")
                  edn/read-string)
              (catch FileNotFoundException e
                (println "could not find config.edn")))]
    (merge +default-config+ cfg)))

(defn -main [& args]
  (print "starting... ")
  (let [{:keys [jdbc port] :as cfg} (config)
        _ (println cfg)
        backend (-> (bp/new-postgresbackend jdbc) c/start)
        dbpoller (dbp/dbpoller (bp/new-postgresbackend jdbc))
        transport-server (ts/websocket-server (:on-connect dbpoller)
                                              :port port)
        #_webapp #_(webapp cfg) #_(tcp/start-server echo-server {:port port})]
    (println "done.")
    ;;;(c/start webapp)
    ;;;(netty/wait-for-close s)
    (c/start transport-server)
    ;;;(println "quit.")
    ))
