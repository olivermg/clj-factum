(ns ow.factum.transport.embedded.client
  (:require [clojure.core.async :refer [chan pipe close!] :as a]
            #_[com.stuartsierra.component :as c]))

(defrecord EmbeddedClient [recv-ch send-ch server
                           srv-send-ch srv-recv-ch])

(defn start [{:keys [recv-ch send-ch server srv-send-ch] :as this}]
  (if-not srv-send-ch
    (let [srvsch (chan)
          srvrch (chan)]
      (pipe srvsch recv-ch false)
      (pipe send-ch srvrch true)
      ((:on-connect server) srvrch srvsch)
      (assoc this
             :srv-send-ch srvsch
             :srv-recv-ch srvrch))
    this))

(defn stop [{:keys [srv-send-ch srv-recv-ch] :as this}]
  (if srv-send-ch
    (do (close! srv-send-ch)
        (close! srv-recv-ch)
        (assoc this
               :srv-send-ch nil
               :srv-recv-ch nil))
    this))

(defn embedded-client [{:keys [on-connect] :as server}]
  (map->EmbeddedClient {:recv-ch (chan)
                        :send-ch (chan)
                        :server server}))
