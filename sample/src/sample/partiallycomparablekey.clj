(ns sample.partiallycomparablekey)

(defrecord PartiallyComparableKey [keycoll]

  java.lang.Comparable

  (compareTo [this other]
    (loop [[tk & tks] keycoll
           [ok & oks] (:keycoll other)]
      (if-not (nil? tk)
        (let [curcmp (compare tk ok)]
          (if (= curcmp 0)
            (recur tks oks)
            curcmp))
        0))))

(defn partially-comparable-key [& ks]
  (->PartiallyComparableKey ks))
