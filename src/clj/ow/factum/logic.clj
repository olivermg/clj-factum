(ns ow.factum.logic
  (:require [clojure.core.logic :as l]
            [clojure.core.logic.pldb :as lp]
            [clojure.core.logic.fd :as lfd]
            [clojure.string :as str]
            [ow.factum.facts :as f]
            [ow.factum.memdb :as m]))

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

(defn get-logic-db [memdb & {:keys [timestamp]}]
  (->> (f/project-facts (m/get-data memdb) :timestamp timestamp)
       (into [] (map #(vec (cons fact %))))
       (apply lp/db)))

#_(defmacro query [ldb & body]
  `(lp/with-db ~ldb
     ~@body))

#_(defmacro query1 [ldb & body]
  `(-> (query ~ldb ~@body)
       first))
