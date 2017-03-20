(ns eventsourcing.db
  (:refer-clojure :rename {update update-clj
                           == ==-clj})
  (:require [korma.db :as db]
            [korma.core :refer :all]
            [heroku-database-url-to-jdbc.core :as h]
            [environ.core :as env]
            [clojure.core.logic :refer :all]
            [clojure.core.logic.pldb :refer :all]))

(defn open []
  (let [url (env/env :database-url)
        kmap (h/korma-connection-map url)
        db (db/create-db (db/postgres kmap))]
    (db/default-connection db)
    db))

(defn select-lazy
  "q is a korma select object, produced via select*"
  ([q l o] (when-let [res (seq (select (-> q (limit l) (offset o))))]
             (println "did another select from" o "to" (+ o l))
             (cons res (lazy-seq (select-lazy q l (+ o l))))))
  ([q] (select-lazy q 1 0)))



(defentity es_events)

(defn get-events []
  (select-lazy (-> (select* es_events)
                   (order :tx :desc))))


#_(open)
#_(get-events)
