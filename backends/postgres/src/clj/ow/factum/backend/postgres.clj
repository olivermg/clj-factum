(ns ow.factum.backend.postgres
  (:refer-clojure :rename {update update-clj})
  (:require [com.stuartsierra.component :as c]
            [korma.db :as db]
            #_[clojure.java.jdbc :refer [with-db-transaction]]
            [korma.core :refer :all]
            [clojure.edn :as edn]
            #_[heroku-database-url-to-jdbc.core :as h]
            [ow.factum.backend :as b]))

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
                           v #{:action :a :v})))
  (transform (fn [v] (reduce #(clojure.core/update %1 %2 edn/read-string)
                             v #{:action :a :v}))))

(defn- save-fact [this t [e a v _ action :as fact]]
  (let [dbfact {:e (or e (b/new-eid this))
                :a a
                :v v
                :t (or t (b/new-tid this))
                :action (or action :add)}
        data (insert es_events
                     (values dbfact))]
    [(:e data) (:a data) (:v data) (:t data)]))

(defrecord PostgresBackend [jdbc
                            conn]

  c/Lifecycle

  (start [this]
    (if-not conn
      (let [conn* (db/create-db jdbc)]
        (db/default-connection conn*)
        (assoc this :conn conn*))
      this))

  (stop [this]
    (if conn
      (do (some-> conn :pool deref :datasource .close)
          (assoc this :conn nil))
      this))

  b/Backend

  (get-items [this since-tid]
    (select-lazy (-> (select* es_events)
                     (where (>= :t since-tid))
                     (order :t :desc)
                     (order :id :desc))
                 (map (fn [{:keys [e a v t action]}]
                        [e a v t action]))))

  (new-eid [this]
    (-> (select (sqlfn nextval "es_events_eid"))
        first
        :nextval))

  (new-tid [this]
    (-> (select (sqlfn nextval "es_events_tid"))
        first
        :nextval))

  (save [this facts]
    (db/transaction
     (let [tid (b/new-tid this)]
       (doall (map #(save-fact this tid %) facts))))))

(defn new-postgresbackend [jdbc]
  #_(let [jdbc (merge {:make-pool? true}
                      jdbc)])
  (map->PostgresBackend {:jdbc jdbc}))

#_(defn get-conn [pgbackend]
    (:jdbc pgbackend))


#_(defn open [url]
  (let [kmap (h/korma-connection-map url)
        conn (db/create-db (db/postgres kmap))]
    (db/default-connection conn)
    (new-eventstore conn)))

#_(open)
#_(entity 1)
