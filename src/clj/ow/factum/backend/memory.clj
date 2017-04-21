(ns ow.factum.backend.memory
  (:require [ow.factum.backend :as b]))

(defrecord MemoryBackend [facts cureid curtxid]

  b/Backend

  (get-items [this since-tx]
    (->> @facts
         (sort-by #(nth % 3))
         reverse))

  (new-eid [this]
    (swap! cureid inc))

  (new-txid [this]
    (swap! curtxid inc))

  (save [this [e a v t action :as fact]]
    (let [ifact [(or e (b/new-eid this))
                 a v
                 (or t (b/new-txid this))]]
      (swap! facts #(conj % (conj ifact (or action :add))))
      ifact)))

(defn new-memorybackend []
  (->MemoryBackend (atom '()) (atom -1) (atom -1)))



(comment (def es (new-memorybackend))

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
