(ns ow.factum.logicdb
  (:require [clojure.core.logic.pldb :as lp]
            [clojure.string :as str]
            [ow.factum.clientstorage :as cs]))

(lp/db-rel fact e a v t)

(defn new-logicdb
  ([clientstorage timestamp]
   {:clientstorage clientstorage
    :timestamp timestamp})
  ([clientstorage] (new-logicdb clientstorage nil)))

(defn get-core-logic-db [this]
  ;;; TODO: if timestamp is set and not in the future (minus last update),
  ;;;       we don't need to always recalculate the resulting data, but can
  ;;;       instead cache it:
  (->> (cs/project (:clientstorage this))
       (into [] (map #(vec (cons fact %))))
       (apply lp/db)))


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


#_(defmacro query [ldb & body]
  `(lp/with-db ~ldb
     ~@body))

#_(defmacro query1 [ldb & body]
  `(-> (query ~ldb ~@body)
       first))
