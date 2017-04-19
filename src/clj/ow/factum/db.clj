(ns ow.factum.db)

(defprotocol Eventstore
  (get-all [this])
  (new-eid [this])
  (new-txid [this])
  (save [this fact]))
