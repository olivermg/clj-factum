(ns sample.btree)


(defprotocol TreeModify
  (add [this v]))

(defprotocol TreeSearch
  (search [this k]))


(deftype RangeMap [kind ^clojure.lang.IPersistentMap m leftfn rightfn]

  clojure.lang.IPersistentMap

  (assoc [this k v]
    (->RangeMap kind (.assoc m k v) leftfn rightfn))

  (assocEx [this k v]
    (->RangeMap kind (.assocEx m k v) leftfn rightfn))


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
      (get m idx))))

(defn range-map [kind & kvs]
  {:pre [(or (= kind :find-ceiling)
             (= kind :find-floor))]}
  (let [[leftfn rightfn] (case kind
                           :find-floor   [(fn [lastki ki] lastki)
                                          (fn [lastki ki] ki)]
                           :find-ceiling [(fn [lastki ki] ki)
                                          (fn [lastki ki] lastki)])]
    (->RangeMap kind (apply sorted-map kvs) leftfn rightfn)))


(defrecord B+Tree [b slots])

(extend-type B+Tree

  TreeModify

  (add [{:keys [slots] :as this} v]
    )

  TreeSearch

  (search [this k]
    ))
