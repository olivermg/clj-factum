(ns sample.core
  (:gen-class)
  (:require [clojure.core.logic :as cl]
            [clojure.core.logic.pldb :as cldb]

            #_[ow.factum.transport.embedded.client :as tc]
            #_[ow.factum.transport.embedded.server :as ts]
            #_[ow.factum.backend.memory :as bm]
            #_[ow.factum.dbpoller :as dbp]
            #_[ow.factum.clientstorage :as cs]
            #_[ow.factum.entities :as e]
            #_[ow.factum.logicdb :as l]))


#_(defn cltest1 []
  (cldb/db-rel man p)
  (cldb/db-rel woman p)
  (cldb/db-rel likes ^:index p1 ^:index p2)

  (let [facts0 (cldb/db
                [man 'Bob]
                [man 'John]

                [woman 'Mary]
                [woman 'Martha]

                [likes 'Bob 'Mary]
                [likes 'Mary 'Bob]
                [likes 'John 'Martha])]

    (cldb/with-db facts0
      (cl/run* [q]
        (cl/fresh [x y]
          (likes x y)
          (man x)
          (woman y)
          (cl/== q [x y]))))))

;;; (cltest1)



(defn cltest2 []
  #_(cldb/db-rel fact e a v t)

  (defrecord Snapshot [entities t])

  (defn facts->snapshot [facts & {:keys [at-t]}]
    (let [;;;ldb (apply cldb/db facts)
          #_lres #_(cldb/with-db ldb
                 (cl/run* [q]
                   (cl/fresh [e a v t]
                     (fact e a v t)
                     (cl/== q [e a v t]))))
          maxt (volatile! 0)]
      (when (not-empty facts #_lres)
        (->Snapshot (->> facts #_lres
                         (reduce (fn [s [e a v t]]
                                   (if (or (not at-t) (<= t at-t))
                                     (do (when (> t @maxt)
                                           (vreset! maxt t))
                                         (update-in s [e a]
                                                    (fn [[t2 v2 :as oldval]]
                                                      (if (and t2 (> t2 t))
                                                        oldval
                                                        [t v]))))
                                     s))
                                 {})
             ;;; TODO: this can probably be optimized in terms of memory usage & performance.
             ;;;   instead of building a new map, can we somehow operate on the existing one in
             ;;;   the appropriate nested places?
                         (map (fn [[e am]]
                                [e (reduce (fn [am [k [t v]]]
                                             (assoc am k v))
                                           {}
                                           am)]))
                         (into {}))
                    (do (println "MAXT2" @maxt)@maxt)))))

  (defn snapshot+facts->snapshot [snapshot facts & {:keys [at-t]}]
    {:pre [(or (nil? at-t) (> at-t (:t snapshot)))]}
    (letfn [(merge-entities [es1 es2]
              (merge-with #(merge %1 %2)
                          es1 es2))
            (merge-snapshots [s1 s2]
              (->Snapshot (merge-entities (:entities s1) (:entities s2))
                          (:t s2)))]
      (merge-snapshots snapshot (facts->snapshot facts :at-t at-t))))

  (let [facts1 [[#_fact 200 :type     :address      1]
                [#_fact 200 :street   "street 111"  1]
                [#_fact 200 :number   111           1]

                [#_fact 100 :type     :person       2]
                [#_fact 100 :name     "foo"         2]
                [#_fact 100 :email    "foo@bar.com" 2]
                [#_fact 100 :address  200           2]

                [#_fact 101 :type     :person       2]
                [#_fact 101 :name     "bar1"        2]
                [#_fact 101 :email    "bar@foo.com" 2]
                [#_fact 101 :address  200           2]
                [#_fact 101 :name     "bar3"        4]
                [#_fact 101 :name     "bar5"        6]
                [#_fact 101 :name     "bar2"        3]
                [#_fact 101 :name     "bar4"        5]]

        facts2 [[#_fact 101 :name     "bar7"        8]
                [#_fact 101 :name     "bar6"        7]
                [#_fact 101 :name     "bar9"        10]
                [#_fact 101 :name     "bar8"        9]]]

    (snapshot+facts->snapshot (facts->snapshot facts1) facts2)))

;;; (cltest2)



(defn etest []
  (cldb/db-rel fact ^:index e ^:index a ^:index v ^:index t)

  #_(cldb/db-rel entity x)
  (cldb/db-rel person ^:index id ^:index name data)
  (cldb/db-rel address ^:index id ^:index street data)
  (cldb/db-rel person<->address ^:index id ^:index personid ^:index addressid data)

  (letfn [(entity->ldb [{:keys [:db/id :db/type] :as entity} & {:keys [index-defs]}]
            (let [index-def (get index-defs type)
                  index-vals (map #(get entity %) index-def)
                  rel (some-> (ns-resolve *ns* (-> type name symbol)) deref)]
              (vec (concat [rel id]
                           index-vals
                           [entity]))))

          (entities->ldb [entities & args]
            (map #(apply entity->ldb % args) entities))]

    (let [facts [[100 :db/type    :address          1]
                 [100 :street     "street 1"        1]
                 [101 :db/type    :address          2]
                 [101 :street     "street 2"        2]
                 [200 :db/type    :person           3]
                 [200 :name       "hans 1"          3]
                 [201 :db/type    :person           4]
                 [201 :name       "hans 2"          4]
                 [300 :db/type    :person<->address 5]
                 [300 :person_id  200               5]
                 [300 :address_id 100               5]
                 [301 :db/type    :person<->address 6]
                 [301 :person_id  200               6]
                 [301 :address_id 101               6]
                 [302 :db/type    :person<->address 7]
                 [302 :person_id  201               7]
                 [302 :address_id 101               7]]

          index-defs {:person           [:name]
                      :address          [:street]
                      :person<->address [:person_id :address_id]}
          #_index-defs #_(->> index-defs
                          (map (fn [[type v]]
                                 [type (->> (map (fn [e i]
                                                   [e i])
                                                 v
                                                 (range))
                                            (into {}))]))
                          (into {}))

          entities [{:db/id 100
                     :db/type :address
                     :street "street 1"}
                    {:db/id 101
                     :db/type :address
                     :street "street 2"}
                    {:db/id 200
                     :db/type :person
                     :name "hans 1"}
                    {:db/id 201
                     :db/type :person
                     :name "hans 2"}
                    {:db/id 300
                     :db/type :person<->address
                     :person_id 200
                     :address_id 100}
                    {:db/id 301
                     :db/type :person<->address
                     :person_id 200
                     :address_id 101}
                    {:db/id 302
                     :db/type :person<->address
                     :person_id 201
                     :address_id 101}]

          entities-ldb (entities->ldb entities :index-defs index-defs)]

      #_(println entities-ldb)

      (cldb/with-db (apply cldb/db entities-ldb)
        (cl/run* [q]
          #_(foo q 22)
          (cl/fresh [p pid pn aid]
            (person pid "hans 2" p)
            (person<->address (cl/lvar) pid aid (cl/lvar))
            (address aid (cl/lvar) q)

            #_(instance q)
            #_(cl/featurec q {:type :foo})

            #_(person p)
            #_(cl/featurec p {:type :person})
            #_(cl/featurec p {:name "hans 1"})
            #_(cl/featurec p {:id q})

            #_(cl/== q p)
            #_(foo q b)
            #_(cl/== q [a b])
            #_(cl/matcha [] ([(person {:id 200 :name q})]))
            #_(cl/== p {:a 1 :b 2 :c 3})
            #_(cl/matcha [p]
                         ([{:a 1 :b 2 :c q}])
                         #_([[_ . o]] (cl/== q ["second" o])))))))))

;;; (etest)



(defn idxtest []
  (letfn [(order-datom [datom order]
            (mapv #(get datom %) order))

          (build-index [datoms order]
            (let [idx* (volatile! {})]
              (doseq [d datoms]
                (vswap! idx* #(assoc-in % (order-datom d order) d)))
              {:order order
               :index @idx*}))

          (leafs [index]
            (let [leafs* (transient [])]
              (loop [[node & nodes] (list index)]
                (cond
                  (map? node)       (recur (concat (vals node) nodes))
                  (not (nil? node)) (do (conj! leafs* node)
                                        (recur nodes))
                  true              (persistent! leafs*)))))

          (lookup-index [{:keys [order index] :as idx} ks]
            (loop [node index
                   [k & ks] ks]
              (cond
                (nil? k)          node
                (not (nil? node)) (recur (get node k) ks))))]

    (let [datoms [[100 :db/type    :address          1]
                  [100 :street     "street 1"        1]
                  [101 :db/type    :address          2]
                  [101 :street     "street 2"        2]
                  [200 :db/type    :person           3]
                  [200 :name       "hans 1"          3]
                  [201 :db/type    :person           4]
                  [201 :name       "hans 2"          4]
                  [302 :db/type    :person<->address 7]
                  [302 :person_id  201               7]
                  [302 :address_id 101               7]
                  [301 :db/type    :person<->address 6]
                  [301 :person_id  200               6]
                  [301 :address_id 101               6]
                  [300 :db/type    :person<->address 5]
                  [300 :person_id  200               5]
                  [300 :address_id 100               5]]]

      #_(build-index datoms [0 1 2 3])
      #_(build-index datoms [1 0 2 3])
      (-> (build-index datoms [1 2 0 3])
          (lookup-index [:name "hans 2"])
          (leafs)))))

;;; (idxtest)



(defn xftest []
  (let [r (range 50000000)
        f1 #(+ % %)
        f2 dec
        f3 #(* % 10)
        ;;;s1 (doall (->> r (map f1) (map f2) (map f3)))
        s2 (doall (sequence (comp (map f1) (map f2) (map f3)) r))
        ]
    (count s2)))

;;; (xftest)



(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  #_(let [b (bm/new-memorybackend)
        s (-> (ts/embedded-server (fn [& args] (println "Connect" args))) ts/start)
        c (-> (tc/embedded-client s) tc/start)
        d (dbp/dbpoller b :poll-interval 100)
        cs1 (cs/clientstorage c)
        l1 (l/new-logicdb cs1)]
    (cs/add-facts cs1 [[:e1 :name "foo"]
                       [:e1 :email "foo@bar.com"]])
    (cs/add-facts cs1 [[:e1 :name "foobar"]])
    #_(l/get-core-logic-db l1)))

;;; (-main)
