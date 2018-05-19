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


(deftest test-lookup-ceiling-even
  (testing "lookup (ceiling, even no. of keys)"
    (let [m (range-map :find-ceiling 5 [3 4 5], 2 [1 2], 8 [6 7 8], 12 [9 10 11 12], 15 [13 14 15], 16 [16])]
      (is (= [6 7 8] (get m 8)))      ;; exact match
      (is (= [6 7 8] (get m 7)))      ;; range match
      (is (= [1 2]   (get m 0)))      ;; lower key
      (is (= nil     (get m 100)))    ;; higher key
      )))


(deftest test-lookup-ceiling-odd
  (testing "lookup (ceiling, odd no. of keys)"
    (let [m (range-map :find-ceiling 5 [3 4 5], 2 [1 2], 8 [6 7 8], 12 [9 10 11 12], 15 [13 14 15])]
      (is (= [6 7 8] (get m 8)))      ;; exact match
      (is (= [6 7 8] (get m 7)))      ;; range match
      (is (= [1 2]   (get m 0)))      ;; lower key
      (is (= nil     (get m 100)))    ;; higher key
      )))


(deftest test-lookup-ceiling-empty
  (testing "lookup (ceiling, empty map)"
    (let [m (range-map :find-ceiling)]
      (is (= nil (get m 0)))
      (is (= nil (get m 100))))))


(deftest test-lookup-floor-even
  (testing "lookup (floor, even no. of keys)"
    (let [m (range-map :find-floor 5 [3 4 5], 2 [1 2], 8 [6 7 8], 12 [9 10 11 12], 15 [13 14 15], 16 [16])]
      (is (= [6 7 8] (get m 8)))      ;; exact match
      (is (= [3 4 5] (get m 7)))      ;; range match
      (is (= nil     (get m 0)))      ;; lower key
      (is (= [16]    (get m 100)))    ;; higher key
      )))


(deftest test-lookup-floor-odd
  (testing "lookup (floor, odd no. of keys)"
    (let [m (range-map :find-floor 5 [3 4 5], 2 [1 2], 8 [6 7 8], 12 [9 10 11 12], 15 [13 14 15])]
      (is (= [6 7 8]    (get m 8)))      ;; exact match
      (is (= [3 4 5]    (get m 7)))      ;; range match
      (is (= nil        (get m 0)))      ;; lower key
      (is (= [13 14 15] (get m 100)))    ;; higher key
      )))


(deftest test-lookup-floor-empty
  (testing "lookup (floor, empty map)"
    (let [m (range-map :find-floor)]
      (is (= nil (get m 0)))
      (is (= nil (get m 100))))))


(deftest test-fn
  (testing "function invocation"
    (let [m (range-map :find-ceiling :e [3 4 5], :b [1 2], :h [6 7 8])]
      (is (= [6 7 8] (m :h)))
      (is (= [3 4 5] (m :d)))
      (is (= [6 7 8] (:h m)))
      (is (= [3 4 5] (:d m))))))
