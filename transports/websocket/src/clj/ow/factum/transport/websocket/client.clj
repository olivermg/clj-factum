(ns ow.factum.transport.websocket.client
  (:require [clojure.core.async :refer [chan]]
            #_[com.stuartsierra.component :as c]
            [ow.rasync.core :refer [websocket-channel-client] :as rc]))

;;; TODO: client must somehow tell server what tid to start with after server was away

(defrecord WebsocketClient [recv-ch send-ch client])

(defn start [{:keys [client] :as this}]
  (assoc this :client (rc/start client)))

(defn stop [{:keys [client] :as this}]
  (assoc this :client (rc/stop client)))

(defn websocket-client [url]
  (let [recv-ch (chan)
        send-ch (chan)]
    (map->WebsocketClient {:recv-ch recv-ch
                           :send-ch send-ch
                           :client (websocket-channel-client recv-ch send-ch url)})))
