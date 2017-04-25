(ns ow.factum.server
  (:require [manifold.stream :as s]
            [manifold.deferred :as d]
            [aleph.tcp :as tcp]
            [aleph.netty :as netty])
  (:gen-class))

(defn- echo-server [s info]
  (println s info)
  (s/connect s s))

(defn -main [& args]
  (print "starting... ")
  (let [s (tcp/start-server echo-server {:port 10001})]
    (println "done.")
    (netty/wait-for-close s)
    (println "quit.")))
