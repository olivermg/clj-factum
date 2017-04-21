(ns ow.factum.db)

(defprotocol Eventstore
  (get-events [this since-tx])
  (new-eid [this])
  (new-txid [this])
  (save [this fact]))

(defn get-events-all [this]
  (get-events this 0))
