(ns ow.factum.logicdb
  (:require [clojure.core.logic.pldb :as lp]
            [clojure.string :as str]
            [ow.factum.memdb :as m]))

(lp/db-rel fact e a v t)

(defn new-logicdb
  ([memdb timestamp]
   {:memdb memdb
    :timestamp timestamp})
  ([memdb] (new-logicdb memdb nil)))

(defn- project-facts [this]
  "Projects rawfacts to the timestamp specified in this logicdb.
This effectively filters those facts that are relevant for the given timestamp,
i.e. it removes obsolete old facts that are overriden by newer ones or have been
retracted later on (but before timestamp). If no timestamp is given, current time
is being assumed."
  (let [rawfacts (m/get-data (:memdb this))
        xf (fn [xf]
             (let [facts* (volatile! {})]
               (fn
                 ([] (xf))
                 ([result] (xf result))
                 ([result [e a v t action :as input]]
                  (case action
                    :add (case (get-in @facts* [e a])
                           true result
                           ::retracted (do (vswap! facts* #(update-in % [e] dissoc a))
                                           result)
                           nil (do (vswap! facts* #(assoc-in % [e a] true))
                                   (xf result (take 4 input))))
                    :retract (do (when (nil? (get-in @facts* [e a]))
                                   (vswap! facts* #(assoc-in % [e a] ::retracted)))
                                 result))))))]
    (into [] xf rawfacts)))

(defn get-core-logic-db [this]
  ;;; TODO: if timestamp is set and not in the future (minus last update),
  ;;;       we don't need to always recalculate the resulting data:
  (->> (project-facts this)
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
