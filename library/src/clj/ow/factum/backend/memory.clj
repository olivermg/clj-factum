(ns ow.factum.backend.memory
  (:require [ow.factum.backend :as b]))

(defn- save-fact [this t fact]
  (let [fact (assoc fact 3 t)]
    (swap! (:facts this)
           #(conj % fact))
    fact))

(defrecord MemoryBackend [facts cureid curtid]

  b/Backend

  (get-items [this since-tid]
    (->> @facts
         (filter #(>= (nth % 3) since-tid))
         (sort-by #(nth % 3))
         reverse))

  (new-eid [this]
    (swap! cureid inc))

  (new-tid [this]
    (swap! curtid inc))

  (save [this facts]
    (let [tid (b/new-tid this)]
      (doall (map #(save-fact this tid %) facts)))))

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
