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
  ;;;:transform #(assoc %2 :comments (lazy-seq (get-comments %1 :comment/author (:db/eid %2))))
  :transform (fn [ldb user]
               (assoc user
                      :comments (lazy-seq
                                 (->> (lp/with-db ldb
                                        (ll/run* [q]
                                          (fl/fact q :comment/author (:db/eid user) (ll/lvar))))
                                      (into [] (map #(get-comment ldb %))))))))

(fe/defentity comment [date text author]
  ;;;:transform #(assoc %2 :users (lazy-seq [(get-user %1 (:comment/author %2))]))
  :transform (fn [ldb comment]
               (assoc comment
                      :users (lazy-seq
                              [(get-user ldb (:comment/author comment))]))))


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
  (let [fs (ff/get-facts )
        pfs (ff/project-facts fs)]
    (is (sequential? pfs))
    (is (not-empty pfs))
    (is (empty? (find-duplicate-facts pfs)))
    (is (< (count pfs) (count fs)))))


(deftest entity-1
  (let [e (fe/entity *ldb* 1)]
    (is (= e {:db/eid 1
              :user/name "foo1"
              :user/gender :f
              :user/birthday #inst "1999-09-09"}))))

#_(deftest get-entities-1
  (let [es (fe/get-entities *ldb* :comment/author 1)
        [e1 e2] (sort-by :db/eid es)]
    (is (= (count es) 2))
    (is (= e1 {:db/eid 3
               :comment/author 1
               :comment/text "comment1_1"
               :comment/date #inst "2017-01-01"}))
    (is (= e2 {:db/eid 4
               :comment/author 1
               :comment/text "comment1_2"
               :comment/date #inst "2017-02-01"}))))


(deftest get-user-1
  (let [u (get-user *ldb* 1)
        u-cs (:comments u)
        u-cs0-us (:users (first u-cs))]
    (is (not-empty u))
    (is (#(instance? User %) u)) ;;; wrapped in function call, because otherwise test framework
                                 ;;; tends to take wrong instance of User after recompiling test ns
    (is (= (:db/eid u) 1))
    (is (every? #(instance? Comment %) u-cs))
    (is (= (count u-cs) 2))
    (is (every? #(instance? User %) u-cs0-us))
    (is (= (count u-cs0-us) 1))
    (is (every? #(= (:db/eid %) (:db/eid u)) u-cs0-us))))


(deftest get-custom-1
  (let [facts (lp/with-db *ldb*
                (ll/run* [q]
                  (ll/fresh [e a v]
                    (fl/fact e a v (ll/lvar))
                    (ll/== e 1)
                    (ll/== q a))))]
    (println facts)
    (is (set facts) #{:user/name :user/birthday :user/gender})))

(deftest get-custom-2
  (let [facts (->> (lp/with-db *ldb*
                     (ll/run* [q]
                       (ll/fresh [e a v]
                         (fl/fact e a v (ll/lvar))
                         (ll/== e 1)
                         (ll/== q [a v]))))
                   (into {}))]
    (is (= facts {:user/name "foo1"
                  :user/birthday #inst "1999-09-09"
                  :user/gender :f}))))


#_(fe/entity ldb 1)
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
