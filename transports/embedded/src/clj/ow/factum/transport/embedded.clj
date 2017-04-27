(ns ow.factum.transport.embedded
  (:require [clojure.core.async :refer [chan]]))

(defn new-embeddedtransport []
  (let [ch (chan)]
    {:clientch ch
     :serverch ch}))
