(ns ow.factum.transport.websocket.server
  (:require [com.stuartsierra.component :as c]
            [ow.rasync.core :refer [websocket-channel-server]]))

(defrecord WebsocketServer [server]

  c/Lifecycle

  (start [this]
    (assoc this :server (c/start server)))

  (stop [this]
    (assoc this :server (c/stop server))))

(defn websocket-server [on-connect]
  (map->WebsocketServer {:server (websocket-channel-server on-connect)}))
