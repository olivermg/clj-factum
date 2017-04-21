(ns ow.factum.backend)

(defprotocol Backend
  (get-items [this since-tx])
  (new-eid [this])
  (new-txid [this])
  (save [this facts]))

(defn get-items-all [this]
  (get-items this 0))
