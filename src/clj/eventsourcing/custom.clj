(ns eventsourcing.custom
  (:require [clojure.edn :as edn]
            [clojure.core.logic :as l]
            [korma.core :as db]
            [eventsourcing.db :as evdb]))

(defrecord Fact [e a v t])

(db/defentity es_events)

#_(def db [(->Fact :user :name "foo" 1)
         (->Fact :user :age 23 2)
         (->Fact :user :name "bar" 3)
         (->Fact :user :age 25 4)])


(defn get-facts [& {:keys [where-criteria]}]
  (let [query (-> (db/select* es_events)
                  (#(if where-criteria
                      (db/where % where-criteria)
                      %))
                  (db/order :tx :desc))
        facts (volatile! {}) ;; need to define it outside xf, as select-lazy transduces multiple times
        xf (comp (map #(->Fact (:eid %)
                               (-> % :attribute edn/read-string)
                               (-> % :value edn/read-string)
                               (:tx %)))
                 (fn [xf]
                   (fn
                     ([] (xf))
                     ([result] (xf result))
                     ([result {:keys [e a] :as input}]
                      (if-not (get-in @facts [e a])
                        (do (vswap! facts #(assoc-in % [e a] true))
                            (xf result input))
                        result)))))]
    (evdb/select-lazy query xf)))

(defn get-entity [eid]
  (let [facts (get-facts :where-criteria {:eid eid})]
    (reduce (fn [s {:keys [a v]}]
              (if-not (contains? s a)
                (assoc s a v)
                s))
            {}
            facts)))

(defn- get-eid []
  #_(str (java.util.UUID/randomUUID))
  (long (rand java.lang.Long/MAX_VALUE)))

(defn- get-txid []
  (-> (db/select (db/sqlfn nextval "es_events_txid"))
      first
      :nextval))

(defn add-fact [{:keys [e a v t] :as fact}]
  (let [data (db/insert es_events
                        (db/values {:eid (or e (get-eid))
                                    :attribute (pr-str a)
                                    :value (pr-str v)
                                    :tx (or t (get-txid))}))]
    (->Fact (:eid data) (-> data :attribute edn/read-string)
            (-> data :value edn/read-string) (:tx data))))

(defn add-facts [facts]
  (let [txid (get-txid)]
    (->> (map #(assoc % :t txid) facts)
         (map add-fact)
         doall)))


(extend-type Fact
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

(defn fact-rel [q]
  (fn [a]
    (l/to-stream
     (map #(l/unify a % q)
          #_(sort-by :t > db)
          (get-facts)
          ))))


#_(evdb/open)
#_(get-events)
#_(l/run 2 [q]
    (l/fresh [e a v t]
      (l/== a :user/name)
      ;;;(l/== e 1)
      (fact-rel [e a v t])
      (l/== q [e a v t])))
#_(get-entity 1)
