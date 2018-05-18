(ns sample.rangemap-test
  (:require [sample.rangemap :refer :all]
            [clojure.test :refer :all]))


;;; (remove-ns 'sample.rangemap-test)


(deftest test-get
  (testing "get"
    (let [m (range-map :find-ceiling :a 11 :b 22 :c 33)]
      (is (= 11 (get m :a)))
      (is (= 22 (get m :b)))
      (is (= 33 (get m :c))))

    (let [m (range-map :find-ceiling)]
      (is (= nil (get m :a))))))


(deftest test-equal
  (testing "equal"
    (let [m (range-map :find-ceiling :a 11 :b 22 :c 33)]
      (is (= {:a 11 :b 22 :c 33} m))
      (is (= m {:b 22 :c 33 :a 11})))

    (let [m (range-map :find-ceiling)]
      (is (= {} m))
      (is (= m {})))))


(deftest test-str
  (testing "str"
    (is (= (str (range-map :find-ceiling :a 11 :b 22 :c 33))
           "{:a 11, :b 22, :c 33}"))
    (is (= (str (range-map :find-ceiling))
           "{}"))))


(deftest test-read
  (testing "read"
    (let [m (range-map :find-ceiling :a 11 :b 22 :c 33)]
      (is (= m (clojure.edn/read-string (str m)))))
    (let [m (range-map :find-ceiling)]
      (is (= m (clojure.edn/read-string (str m)))))))


(deftest test-keys
  (testing "keys"
    (is (= (set (keys (range-map :find-ceiling :a 11 :b 22 :c 33)))
           (set (keys {:a 11 :b 22 :c 33}))))
    (is (= (keys (range-map :find-ceiling))
           (keys {})))))


(deftest test-vals
  (testing "vals"
    (is (= (set (vals (range-map :find-ceiling :a 11 :b 22 :c 33)))
           (set (vals {:a 11 :b 22 :c 33}))))
    (is (= (vals (range-map :find-ceiling))
           (vals {})))))


(deftest test-assoc
  (testing "assoc"
    (let [m (-> (range-map :find-ceiling :a 11 :b 22)
                (assoc :b 23 :c 33 :d 44))]
      (is (= m {:a 11 :b 23 :c 33 :d 44})))
    (let [m (-> (range-map :find-ceiling)
                (assoc :b 22 :a 11))]
      (is (= m {:a 11 :b 22})))))


(deftest test-dissoc
  (testing "dissoc"
    (let [m (-> (range-map :find-ceiling :a 11 :b 22 :c 33)
                (dissoc :b))]
      (is (= m {:a 11 :c 33})))
    (let [m (-> (range-map :find-ceiling)
                (dissoc :a))]
      (is (= m {})))))
