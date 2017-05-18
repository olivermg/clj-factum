(ns ow.factum.transport.websocket.server
  (:require #_[com.stuartsierra.component :as c]
            [ow.rasync.core :refer [websocket-channel-server] :as rc]))

(defrecord WebsocketServer [server])

(defn start [{:keys [server] :as this}]
  (assoc this :server (rc/start server)))

(defn stop [{:keys [server] :as this}]
  (assoc this :server (rc/stop server)))

(defn websocket-server [on-connect & {:keys [port]}]
  (map->WebsocketServer {:server (websocket-channel-server on-connect
                                                           :port port)}))
