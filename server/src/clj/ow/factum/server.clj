(ns ow.factum.server
  (:require [clojure.edn :as edn]
            [manifold.stream :as s]
            [manifold.deferred :as d]
            [aleph.tcp :as tcp]
            [aleph.netty :as netty])
  (:import [java.io FileNotFoundException])
  (:gen-class))

(def ^:private +default-config+
  {:port 10001})

(defn- echo-server [s info]
  (println s info)
  (s/connect s s))

(defn- config []
  (try
    (-> (slurp "config.edn")
        edn/read-string)
    (catch FileNotFoundException e
      (println "could not find config.edn, falling back to default config")
      +default-config+)))

(defn -main [& args]
  (print "starting... ")
  (let [{:keys [port] :as cfg} (config)
        s (tcp/start-server echo-server {:port port})]
    (println "done.")
    (netty/wait-for-close s)
    (println "quit.")))
