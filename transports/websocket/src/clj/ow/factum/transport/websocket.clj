(ns ow.factum.transport.websocket
  (:require [ow.rasync.core :refer [websocket-channel-server websocket-channel-client]]))

(defn websocket-server [on-connect]
  {:server (websocket-channel-server on-connect)})

(defn websocket-client [recv-ch send-ch url]
  {:client (websocket-channel-client recv-ch send-ch url)
   :recv-ch recv-ch
   :send-ch send-ch})
