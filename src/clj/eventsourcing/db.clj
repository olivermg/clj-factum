(ns eventsourcing.db
  (:refer-clojure :rename {update update-clj
                           == ==-clj})
  (:require [korma.db :as db]
            [korma.core :refer :all]
            [heroku-database-url-to-jdbc.core :as h]
            [environ.core :as env]
            [clojure.core.logic :refer :all]
            [clojure.core.logic.pldb :refer :all]
            [clojure.edn :as edn]
            [eventsourcing.custom :as es]))

(defentity es_events)

(defn open []
  (let [url (env/env :database-url)
        kmap (h/korma-connection-map url)
        db (db/create-db (db/postgres kmap))]
    (db/default-connection db)
    db))

(defn select-lazy
  "q is a korma select object, produced via select*"
  ([q l o xf] (when-let [res (-> (select (-> q (limit l) (offset o)))
                                 (#(if xf
                                     (transduce xf conj %)
                                     %))
                                 seq)]
                (println "did another select from" o "to" (+ o l))
                (concat res (lazy-seq (select-lazy q l (+ o l) xf)))))
  ([q xf] (select-lazy q 1 0 xf))
  ([q] (select-lazy q 1 0 nil)))


(defn entity [eid]
  (select es_events
          (where {:eid eid})))

(defn get-events []
  (select-lazy (-> (select* es_events)
                   (order :tx :desc))
               (map #(es/->Fact (:eid %)
                                (-> % :attribute edn/read-string)
                                (-> % :value edn/read-string)
                                (:tx %)))))


#_(open)
#_(get-events)
#_(entity 1)
