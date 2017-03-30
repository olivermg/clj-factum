(ns app-test
  (:require [clojure.test :refer :all]
            [clojure.core.logic :as ll]
            [clojure.core.logic.pldb :as lp]
            [ow.factum.db :as fd]
            [ow.factum.facts :as ff]
            [ow.factum.logic :as fl]
            [ow.factum.entities :as fe]))

(declare get-user get-users)
(declare get-comment get-comments)

(fe/defentity user [name gender birthday]
  :transform #(assoc % :comments (lazy-seq (get-comments :comment/author (:db/eid %)))))

(fe/defentity comment [date text author]
  :transform #(assoc % :users (lazy-seq [(get-user (:comment/author %))])))


(defonce ^:dynamic *db* (fd/open))
(def ^:dynamic *ldb* (fl/get-logic-db))


(defn- find-duplicate-facts [fs]
  (let [dups (transient #{})]
    (reduce (fn [s [e a v t]]
              (update-in s [e a] #(let [n (or % 0)]
                                    (when (> n 0)
                                      (conj! dups [e a]))
                                    (inc n))))
            {}
            fs)
    (persistent! dups)))


(deftest logic-db-1
  (is (not-empty *ldb*)))

(deftest get-facts-1
  (let [fs (ff/get-facts)]
    (is (sequential? fs))
    (is (not-empty fs))
    (is (not-empty (find-duplicate-facts fs)))))

(deftest project-facts-1
  (let [pfs (-> (ff/get-facts) ff/project-facts)]
    (is (sequential? pfs))
    (is (not-empty pfs))
    (is (empty? (find-duplicate-facts pfs)))))


#_(fe/get-entity ldb 1)
#_(get-user ldb 2)

#_(lp/with-db ldb
    (ll/run* [q]
      (ll/fresh [eu vu
                 ec ac vc]
        (ll/== vu "foo1")
        (fl/fact eu :user/name vu (ll/lvar))
        (fl/fact ec :comment/author eu (ll/lvar))
        (ll/== ac :comment/text)
        (fl/fact ec ac vc (ll/lvar))
        (ll/== q [ec ac vc (ll/lvar)]))))
