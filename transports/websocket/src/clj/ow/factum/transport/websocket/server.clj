(ns ow.factum.transport.websocket.server
  (:require [ow.rasync.core :refer [websocket-channel-server]]))

(defn websocket-server [on-connect]
  {:server (websocket-channel-server on-connect)})
