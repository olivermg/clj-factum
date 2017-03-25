(ns eventsourcing.custom
  (:require [clojure.edn :as edn]
            [clojure.core.logic :as l]
            [korma.core :as db]
            [eventsourcing.db :as evdb]))

(defrecord Fact [e a v t])

(db/defentity es_events
  (db/prepare (fn [v] (reduce #(update %1 %2 pr-str)
                              v #{:action :attribute :value})))
  (db/transform (fn [v] (reduce #(update %1 %2 edn/read-string)
                                v #{:action :attribute :value}))))

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
        xf (comp (fn [xf]
                   (fn
                     ([] (xf))
                     ([result] (xf result))
                     ([result {:keys [action eid attribute] :as input}]
                      (case action
                        :add (case (get-in @facts [eid attribute])
                               true result
                               ::retracted (do (vswap! facts #(update-in % [eid] dissoc attribute))
                                               result)
                               nil (do (vswap! facts #(assoc-in % [eid attribute] true))
                                       (xf result input)))
                        :retract (do (vswap! facts #(assoc-in % [eid attribute] ::retracted))
                                     result)))))
                 (map #(->Fact (:eid %)
                               (:attribute %)
                               (:value %)
                               (:tx %))))]
    (evdb/select-lazy query xf)))

(defn get-entity [eid]
  (let [facts (get-facts :where-criteria {:eid eid})]
    (reduce (fn [s {:keys [a v]}]
              (if-not (contains? s a)
                (assoc s a v)
                s))
            {}
            facts)))

(defn- new-eid []
  #_(str (java.util.UUID/randomUUID))
  #_(long (rand java.lang.Long/MAX_VALUE))
  (-> (db/select (db/sqlfn nextval "es_events_eid"))
      first
      :nextval))

(defn- new-txid []
  (-> (db/select (db/sqlfn nextval "es_events_txid"))
      first
      :nextval))

(defn- save-fact [{:keys [e a v t] :as fact} & {:keys [action]
                                                :or {action :add}}]
  (let [data (db/insert es_events
                        (db/values {:eid (or e (new-eid))
                                    :attribute a
                                    :value v
                                    :tx (or t (new-txid))
                                    :action action}))]
    (->Fact (:eid data) (:attribute data)
            (:value data) (:tx data))))

(defn add-facts [facts]
  (let [txid (new-txid)]
    #_(->> (map #(assoc % :t txid) facts)
           (map save-fact)
         doall)
    (sequence (comp (map #(assoc % :t txid))
                    (map #(save-fact % :action :add)))
              facts)))

(defn retract-facts [facts]
  (let [txid (new-txid)]
    (sequence (comp (map #(assoc % :t txid))
                    (map #(save-fact % :action :retract)))
              facts)))


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
#_(l/run* [q]
    (l/fresh [e a v t]
      (l/== a :user/name)
      ;;;(l/== e 1)
      (fact-rel [e a v t])
      (l/== q [e a v t])))
#_(get-entity 1)
