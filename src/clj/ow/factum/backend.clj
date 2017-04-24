(ns ow.factum.backend)

(defprotocol Backend
  (get-items [this since-tid])
  (new-eid [this])
  (new-tid [this])
  (save [this facts]))

(defn get-items-all [this]
  (get-items this 0))
