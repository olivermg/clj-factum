(ns ow.factum.db.postgres
  (:refer-clojure :rename {update update-clj})
  (:require [korma.db :as db]
            [korma.core :refer :all]
            [clojure.edn :as edn]
            [heroku-database-url-to-jdbc.core :as h]
            [ow.factum.db :as d]))

(defn- select-lazy
  "q is a korma select object, produced via select*"
  ([q l o xf] (when-let [res (-> (select (-> q (limit l) (offset o)))
                                 (#(if xf
                                     (into [] xf %)
                                     #_(sequence xf %)
                                     #_(transduce xf (or rf conj) %)
                                     %))
                                 seq)]
                (println "did another select from" o "to" (+ o l))
                (concat res (lazy-seq (select-lazy q l (+ o l) xf)))))
  ([q xf] (select-lazy q 10 0 xf))
  ([q] (select-lazy q 10 0 nil)))

(defentity es_events
  (prepare (fn [v] (reduce #(clojure.core/update %1 %2 pr-str)
                           v #{:action :attribute :value})))
  (transform (fn [v] (reduce #(clojure.core/update %1 %2 edn/read-string)
                             v #{:action :attribute :value}))))

(defrecord Eventstore [db]

  d/Eventstore

  (get-all [this]
    (select-lazy (-> (select* es_events)
                     (order :tx :desc)
                     (order :id :desc))
                 (map (fn [{:keys [eid attribute value tx action]}]
                        [eid attribute value tx action]))))

  (new-eid [this]
    (-> (select (sqlfn nextval "es_events_eid"))
        first
        :nextval))

  (new-txid [this]
    (-> (select (sqlfn nextval "es_events_txid"))
        first
        :nextval))

  (save [this [e a v t action :as fact]]
    (let [dbfact {:eid (or e (d/new-eid this))
                  :attribute a
                  :value v
                  :tx (or t (d/new-txid this))
                  :action (or action :add)}
          data (insert es_events
                       (values dbfact))]
      [(:eid data) (:attribute data) (:value data) (:tx data)])))

(defn new-eventstore [db]
  (->Eventstore db))

(defn get-db [eventstore]
  (:db eventstore))

(defn open [url]
  (let [kmap (h/korma-connection-map url)
        db (db/create-db (db/postgres kmap))]
    (db/default-connection db)
    (new-eventstore db)))


#_(open)
#_(entity 1)
