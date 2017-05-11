(ns ow.factum.transport.websocket.client
  (:require [clojure.core.async :refer [chan]]
            [com.stuartsierra.component :as c]
            [ow.rasync.core :refer [websocket-channel-client]]))

;;; TODO: client must somehow tell server what tid to start with after server was away

(defrecord WebsocketClient [recv-ch send-ch client]

  c/Lifecycle

  (start [this]
    (assoc this :client (c/start client)))

  (stop [this]
    (assoc this :client (c/stop client))))

(defn websocket-client [url]
  (let [recv-ch (chan)
        send-ch (chan)]
    (map->WebsocketClient {:recv-ch recv-ch
                           :send-ch send-ch
                           :client (websocket-channel-client recv-ch send-ch url)})))
