(ns ow.factum.transport.websocket.client
  (:require [clojure.core.async :refer [chan]]
            [ow.rasync.core :refer [websocket-channel-client]]))

(defn websocket-client [url]
  (let [recv-ch (chan)
        send-ch (chan)]
    (websocket-channel-client recv-ch send-ch url)
    {:recv-ch recv-ch
     :send-ch send-ch
     :url url}))
