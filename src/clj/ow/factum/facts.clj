(ns ow.factum.facts
  (:require [clojure.edn :as edn]
            [korma.core :as db]
            [ow.factum.db :as fdb]))

(db/defentity es_events
  (db/prepare (fn [v] (reduce #(update %1 %2 pr-str)
                              v #{:action :attribute :value})))
  (db/transform (fn [v] (reduce #(update %1 %2 edn/read-string)
                                v #{:action :attribute :value}))))

(defn get-facts [& {:keys [where-criteria]}]
  "Retrieves all raw facts, potentially narrowed by specified criteria."
  (let [query (-> (db/select* es_events)
                  (#(if where-criteria
                      (db/where % where-criteria)
                      %))
                  (db/order :tx :desc))
        ;;;facts (volatile! {}) ;; need to define it outside xf, as select-lazy transduces multiple times
        xf (map (fn [{:keys [eid attribute value tx action]}]
                  [eid attribute value tx action]))
        #_(comp (fn [xf]
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
    (fdb/select-lazy query xf)))

(defn project-facts [facts]
  "Projects given facts to a given timestamp."
  (let [;;;facts* (volatile! {})
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
                    :retract (do (vswap! facts* #(assoc-in % [e a] ::retracted))
                                 result))))))]
    (into [] xf facts)))

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

(defn- save-fact [[e a v t action :as fact]]
  (let [data (db/insert es_events
                        (db/values {:eid (or e (new-eid))
                                    :attribute a
                                    :value v
                                    :tx (or t (new-txid))
                                    :action action}))]
    #_(->Fact (:eid data) (:attribute data)
              (:value data) (:tx data))
    [(:eid data) (:attribute data) (:value data) (:tx data)]))

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
