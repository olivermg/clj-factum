(ns sample.rangemap)


(deftype RangeMap [kind ^clojure.lang.IPersistentMap m leftfn rightfn fallback-val]

  clojure.lang.MapEquivalence

  clojure.lang.IPersistentMap

  (assoc [this k v]
    (if-not (= k ::fallback)
      (RangeMap. kind (.assoc m k v) leftfn rightfn fallback-val)
      (RangeMap. kind m leftfn rightfn v)))

  (assocEx [this k v]
    (if-not (= k ::fallback)
      (RangeMap. kind (.assocEx m k v) leftfn rightfn fallback-val)
      (RangeMap. kind m leftfn rightfn v)))

  (without [this k]
    (if-not (= k ::fallback)
      (RangeMap. kind (.without m k) leftfn rightfn fallback-val)
      (RangeMap. kind m leftfn rightfn nil)))


  clojure.lang.ILookup

  (valAt [this k]
    (.valAt this k nil))

  (valAt [this k not-found]
    (if-not (= k ::fallback)
      (let [idx (loop [ks     (keys m)
                       n      (count ks)
                       lastki nil]
                  #_(println "LOOP1" lastki ks)
                  (if (not-empty ks)
                    (let [i   (int (/ n 2))
                          ki  (get (vec ks) i)
                          cmp (compare ki k)]
                      #_(println "LOOP2" n i ki cmp)
                      (cond
                        (> cmp 0) (recur (take i ks)       i         (leftfn lastki ki))   ;; traverse left
                        (< cmp 0) (recur (drop (inc i) ks) (- n i 1) (rightfn lastki ki))  ;; traverse right
                        (= cmp 0) ki))
                    lastki))]
        (if-not (nil? idx)
          (with-meta (get m idx)
            {::key idx})
          (or not-found
              (with-meta fallback-val
                {::key ::fallback}))))
      fallback-val))


  clojure.lang.IFn

  (invoke [this arg1]
    (.valAt this arg1))


  clojure.lang.Seqable

  (seq [this]
    (concat (.seq m)
            (when-not (nil? fallback-val)
              [(clojure.lang.MapEntry. ::fallback fallback-val)])))


  clojure.lang.Associative

  (containsKey [this k]
    (if-not (= k ::fallback)
      (.containsKey m k)
      true))

  (entryAt [this k]
    (.entryAt m k))


  clojure.lang.IPersistentCollection

  (equiv [this o]
    (.equiv m o))

  (empty [this]
    (RangeMap. kind (.empty m) leftfn rightfn nil))

  (cons [this o]
    (RangeMap. kind (.cons m o) leftfn rightfn fallback-val))

  (count [this]
    (.count m))


  #_java.lang.Iterable

  #_(iterator [this]
    (.iterator m))


  Object

  (toString [this]
    (let [s (->> (pr-str m)
                 rest
                 butlast
                 (apply str))]
      (str "{" s
           (when-not (nil? fallback-val)
             (str " " ::fallback " " (pr-str fallback-val)))
           "}")))


  java.util.Map

  (size [this]
    (.size m))

  (get [this k]
    (.valAt this k))


  clojure.lang.IHashEq

  (hasheq [this]
    (.hasheq m)))


(defmethod print-method RangeMap [o writer]
  (.write writer (.toString o)))


(defn range-map [kind & kvs]
  {:pre [(or (= kind :find-ceiling)
             (= kind :find-floor))]}
  (println "RANGE-MAP" kind kvs)
  (let [[leftfn rightfn] (case kind
                           :find-floor   [(fn [lastki ki] lastki)
                                          (fn [lastki ki] ki)]
                           :find-ceiling [(fn [lastki ki] ki)
                                          (fn [lastki ki] lastki)])]
    (->RangeMap kind (apply sorted-map kvs) leftfn rightfn nil)))
