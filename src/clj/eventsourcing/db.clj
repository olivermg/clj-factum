(ns eventsourcing.db
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
  ([q l o] (when-let [res (seq (select (-> (q) (limit l) (offset o))))]
             (println "did another select from" o "to" (+ o l))
             (cons res (lazy-seq (select-lazy q l (+ o l))))))
  ([q] (select-lazy q 1 0)))
