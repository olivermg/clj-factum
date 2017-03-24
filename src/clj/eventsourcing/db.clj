(ns eventsourcing.db
  (:refer-clojure :rename {update update-clj
                           == ==-clj})
  (:require [korma.db :as db]
            [korma.core :refer :all]
            [heroku-database-url-to-jdbc.core :as h]
            [environ.core :as env]
            #_[clojure.core.logic :refer :all]
            #_[clojure.core.logic.pldb :refer :all]
            #_[clojure.edn :as edn]
            #_[eventsourcing.custom :as es]))



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
                                     (into [] xf %)
                                     #_(sequence xf %)
                                     #_(transduce xf (or rf conj) %)
                                     %))
                                 seq)]
                (println "did another select from" o "to" (+ o l))
                (concat res (lazy-seq (select-lazy q l (+ o l) xf)))))
  ([q xf] (select-lazy q 2 0 xf))
  ([q] (select-lazy q 2 0 nil)))


#_(open)
#_(entity 1)
