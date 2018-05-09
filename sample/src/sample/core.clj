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
  (cldb/db-rel fact e a v t)

  (letfn [(entities [db]
            (let [lres (cldb/with-db db
                         (cl/run* [q]
                           (cl/fresh [e a v t]
                             (fact e a v t)
                             (cl/== q [e a v t]))))]
              (when (not-empty lres)
                (reduce (fn [s [e a v t]]
                          (update-in s [e a]
                                     (fn [tmap]
                                       (if tmap
                                         (assoc tmap t v)
                                         (sorted-map-by #(compare %2 %1) t v)))))
                        {}
                        lres))))]

    (let [facts1 (cldb/db
                  [fact 200 :type     :address      1]
                  [fact 200 :street   "street 111"  1]
                  [fact 200 :number   111           1]

                  [fact 100 :type     :person       2]
                  [fact 100 :name     "foo"         2]
                  [fact 100 :email    "foo@bar.com" 2]
                  [fact 100 :address  200           2]

                  [fact 101 :type     :person       2]
                  [fact 101 :name     "bar1"        2]
                  [fact 101 :email    "bar@foo.com" 2]
                  [fact 101 :address  200           2]
                  [fact 101 :name     "bar2"        4]
                  [fact 101 :name     "bar3"        3])]

      #_(cldb/with-db facts1
        (cl/run* [q]
          (cl/fresh [pid a v]
            (fact pid :type :person (cl/lvar))
            (fact pid :address 200 (cl/lvar))
            (fact pid a v (cl/lvar))
            (cl/== q [pid a v]))))

      (entities facts1))))

;;; (cltest2)



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
