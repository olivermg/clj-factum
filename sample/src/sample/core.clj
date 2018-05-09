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

  (defn facts->entities [facts & {:keys [at-t]}]
    (let [;;;ldb (apply cldb/db facts)
          #_lres #_(cldb/with-db ldb
                 (cl/run* [q]
                   (cl/fresh [e a v t]
                     (fact e a v t)
                     (cl/== q [e a v t]))))]
      (when (not-empty facts #_lres)
        (->> facts #_lres
             (reduce (fn [s [e a v t]]
                       (if (or (not at-t) (<= t at-t))
                         (update-in s [e a]
                                    (fn [[t2 v2 :as oldval]]
                                      (if (and t2 (> t2 t))
                                        oldval
                                        [t v])))
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
             (into {})))))

  (defn entities+facts->entities [entities facts & {:keys [at-t]}]
    (letfn [(merge-entities [es1 es2]
              (merge-with #(merge %1 %2)
                          es1 es2))]
      (merge-entities entities (facts->entities facts :at-t at-t))))

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

    (entities+facts->entities (facts->entities facts1) facts2)))

;;; (cltest2)



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
