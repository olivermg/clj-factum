(ns sample.rangemap)


(deftype RangeMap [kind ^clojure.lang.IPersistentMap m leftfn rightfn]

  clojure.lang.MapEquivalence

  clojure.lang.IPersistentMap

  (assoc [this k v]
    (RangeMap. kind (.assoc m k v) leftfn rightfn))

  (assocEx [this k v]
    (RangeMap. kind (.assocEx m k v) leftfn rightfn))

  (without [this k]
    (RangeMap. kind (.without m k) leftfn rightfn))


  clojure.lang.ILookup

  (valAt [this k]
    (.valAt this k nil))

  (valAt [this k not-found]
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
        (get m idx)
        not-found)))


  clojure.lang.IFn

  (invoke [this arg1]
    (.valAt this arg1))


  clojure.lang.Seqable

  (seq [this]
    (.seq m))


  clojure.lang.Associative

  (containsKey [this k]
    (.containsKey m k))


  clojure.lang.IPersistentCollection

  (equiv [this o]
    (.equiv m o))

  (empty [this]
    (RangeMap. kind (.empty m) leftfn rightfn))

  (cons [this o]
    (RangeMap. kind (.cons m o) leftfn rightfn))

  (count [this]
    (.count m))


  #_java.lang.Iterable

  #_(iterator [this]
    (.iterator m))


  Object

  (toString [this]
    (.toString m))


  java.util.Map

  (size [this]
    (.size m))

  (get [this k]
    (.valAt this k))


  clojure.lang.IHashEq

  (hasheq [this]
    (.hasheq m)))


(defn range-map [kind & kvs]
  {:pre [(or (= kind :find-ceiling)
             (= kind :find-floor))]}
  (let [[leftfn rightfn] (case kind
                           :find-floor   [(fn [lastki ki] lastki)
                                          (fn [lastki ki] ki)]
                           :find-ceiling [(fn [lastki ki] ki)
                                          (fn [lastki ki] lastki)])]
    (->RangeMap kind (apply sorted-map kvs) leftfn rightfn)))

(defn range-map-ceiling [& kvs]
  (apply range-map :find-ceiling kvs))

(defn range-map-floor [& kvs]
  (apply range-map :find-floor kvs))
