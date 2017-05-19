(ns sampleclient.websocket
  (:require [integrant.core :as ig]
            [ow.factum.transport.websocket.client :as wc]))

(defmethod ig/init-key ::client [_ opts]
  (-> (wc/websocket-client "ws://localhost:8899/ws") wc/start))

(defmethod ig/halt-key! ::client [_ client]
  (-> client wc/stop))
