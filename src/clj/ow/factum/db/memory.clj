(ns ow.factum.db.memory
  (:require [ow.factum.db :as d]))

(defrecord Eventstore [facts cureid curtxid]

  d/Eventstore

  (get-all [this]
    (->> @facts
         (sort-by #(nth % 3))
         reverse))

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

(defn new-eventstore []
  (->Eventstore (atom '()) (atom -1) (atom -1)))



(comment (def es (new-eventstore))

         (d/add-facts es [[1 :name "walter1"] [1 :birthday #inst"1980-01-01"]])

         (d/add-facts es [[1 :name "walter2"] [1 :birthday #inst"1981-01-01"]])

         (d/add-facts es [[1 :name "walter3"] [1 :birthday #inst"1982-01-01"]])

         (d/add-facts es [[1 :name "walter4"]])

         (def ldb (ow.factum.logic/get-logic-db es))

         (clojure.core.logic.pldb/with-db ldb
           (clojure.core.logic/run* [q]
             (clojure.core.logic/fresh [e a v]
               (ow.factum.logic/fact e a v (clojure.core.logic/lvar))
               (clojure.core.logic/== q [a v]))))

         )
