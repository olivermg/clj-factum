(ns ow.factum.db.memory
  (:require [ow.factum.db :as d]))

(defrecord Eventstore [facts cureid curtxid]

  d/Eventstore

  (get-all [this]
    @facts)

  (new-eid [this]
    (swap! cureid inc))

  (new-txid [this]
    (swap! curtxid inc))

  (save [this [e a v t action :as fact]]
    (let [ifact [(or e (d/new-eid this))
                 a v
                 (or t (d/new-txid this))]]
      (swap! facts #(conj % (conj ifact (or action :add))))
      ifact)))

(defn new-eventstore [facts]
  (->Eventstore (atom facts) (atom -1) (atom -1)))
