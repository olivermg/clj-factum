(ns ow.factum.logic
  (:require [clojure.core.logic :as l]
            [clojure.core.logic.pldb :as lp]
            [clojure.core.logic.fd :as lfd]
            [clojure.string :as str]
            ;;;[korma.core :as db]
            ;;;[ow.factum.db :as fdb]
            [ow.factum.db :as db]
            #_[ow.factum.facts :as facts]
            ))


;;;
;;; LOGIC STUFF
;;;

(lp/db-rel fact e a v t)

#_(extend-type Fact
  clojure.core.logic.protocols/IUnifyTerms
  (unify-terms [u v s]
    ;;;(println "U:" u ", V:" v ", S:" s)
    (when (and (instance? clojure.lang.PersistentVector v)
               (> (count v) 1))
      (loop [i 0 v v s s]
        ;;;(println "I:" i ", V:" v ", S:" s)
        (if (empty? v)
          s
          (when-let [s (l/unify s (first v) (get u (nth [:e :a :v :t] i)))]
            (recur (inc i) (next v) s)))))))

#_(defn fact-rel [q]
  (fn [a]
    (l/to-stream
     (map #(l/unify a % q)
          #_(sort-by :t > db)
          (get-facts)
          ))))

(defn get-logic-db [eventstore]
  (->> (db/projected-facts eventstore #inst"1980-01-01")
       (into [] (map #(vec (cons fact %))))
       (apply lp/db)))
