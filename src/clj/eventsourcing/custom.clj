(ns eventsourcing.custom
  (:require [clojure.core.logic :as l]))

(defrecord Fact [e a v t])

(def db [(->Fact :user :name "foo" 1)
         (->Fact :user :age 23 2)
         (->Fact :user :name "bar" 3)
         (->Fact :user :age 25 4)])

(extend-type Fact
  clojure.core.logic.protocols/IUnifyTerms
  (unify-terms [u v s]
    (println "U:" u ", V:" v ", S:" s)
    (when (and (instance? clojure.lang.PersistentVector v)
               (> (count v) 1))
      (loop [i 0 v v s s]
        (println "I:" i ", V:" v ", S:" s)
        (if (empty? v)
          s
          (when-let [s (l/unify s (first v) (get u (nth [:e :a :v :t] i)))]
            (recur (inc i) (next v) s)))))))

(defn fact-rel [q]
  (fn [a]
    (l/to-stream
     (map #(l/unify a % q)
          (sort-by :t > db)))))

#_(l/run 1 [q]
    (l/fresh [e a v t]
      (l/== a :name)
      (fact-rel [e a v t])
      (l/== q [e a v t])))
