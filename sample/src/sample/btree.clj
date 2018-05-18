(ns sample.btree)


(defprotocol TreeModify
  (add [this v]))

(defprotocol TreeSearch
  (search [this k]))


(deftype RangeMap [kind ^clojure.lang.IPersistentMap m]

  clojure.lang.IPersistentMap

  (assoc [this k v]
    (->RangeMap kind (.assoc m k v)))

  (assocEx [this k v]
    (.assocEx m k v)
    this)


  clojure.lang.ILookup

  (valAt [this k]
    (.valAt this k nil))

  (valAt [this k not-found]
    (let [idx (loop [ks     (keys m)
                     lastki nil]
                (if (not-empty ks)
                  (let [n   (count ks)
                        i   (int (/ n 2))
                        ki  (get (vec ks) i)
                        cmp (compare ki k)]
                    (cond
                      (> cmp 0) (recur (take i ks)       (if (= kind :find-floor)   lastki ki))  ;; traverse left
                      (< cmp 0) (recur (drop (inc i) ks) (if (= kind :find-ceiling) lastki ki))  ;; traverse right
                      (= cmp 0) ki))
                  lastki))]
      (get m idx))))

(defn range-map [kind & kvs]
  {:pre [(or (= kind :find-ceiling)
             (= kind :find-floor))]}
  (->RangeMap kind (apply sorted-map kvs)))


(defrecord B+Tree [b slots])

(extend-type B+Tree

  TreeModify

  (add [{:keys [slots] :as this} v]
    )

  TreeSearch

  (search [this k]
    ))
