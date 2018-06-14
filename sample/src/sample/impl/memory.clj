(ns sample.impl.memory
  (:require [clojure.set :as s]
            [clojure.edn :as edn]

            [clj-karabiner.tree :as t]
            [clj-karabiner.tree.bplustree :as bt]

            [sample.factstore :as fs]
            [sample.pubsub :as ps]))


(defrecord MemoryFactStore [facts subscribers]

  fs/FactStoreWriter

  (assert* [this fs]
    (dorun
     (for [f fs
           s (keys subscribers)]
       (ps/msg-received s f)))
    (->MemoryFactStore (apply conj facts fs) subscribers))

  fs/FactStoreReader

  (get-tail [this start]
    (->> facts
         (drop start)
         vec))

  ps/Publisher

  (subscribe [this subscriber]
    (->MemoryFactStore facts (assoc subscribers subscriber true)))

  (unsubscribe [this subscriber]
    (->MemoryFactStore facts (dissoc subscribers subscriber))))


(defn memory-factstore
  ([subscriber & subscribers]
   (->MemoryFactStore [] (zipmap (cons subscriber subscribers)
                                 (repeat true))))
  ([]
   (memory-factstore nil)))



#_(let [eavt-idx (atom (bt/b+tree 3))
      aevt-idx (atom (bt/b+tree 3))
      avet-idx (atom (bt/b+tree 3))
      vaet-idx (atom (bt/b+tree 3))
      mfs (-> (memory-factstore (reify ps/Subscriber
                                  (msg-received [this [e a v t :as fact]]
                                    (swap! eavt-idx #(t/insert % [e a v t] fact))
                                    (swap! aevt-idx #(t/insert % [a e v t] fact))
                                    (swap! avet-idx #(t/insert % [a v e t] fact))
                                    (swap! vaet-idx #(t/insert % [v a e t] fact)))))
              (fs/assert [:person/otto :person/email     "otto@foo.com"  1]
                         [:person/otto :person/firstname "otto"          1]
                         [:person/otto :person/lastname  "wurst"         1])
              (fs/assert [:person/hans :person/email     "hans@foo.com"  2]
                         [:person/hans :person/firstname "hans"          2]
                         [:person/hans :person/lastname  "wurst"         2])
              (fs/assert [:person/otto :person/firstname "otto fritz"    3])
              (fs/assert [:person/hans :person/firstname "hans hugo"     4])
              (fs/assert [:address/a   :address/street   "street1"       5]
                         [:address/a   :address/number   "11a"           5])
              (fs/assert [:address/b   :address/street   "street1"       6]
                         [:address/b   :address/number   "22a"           6])
              (fs/assert [:person/otto :person/address   (pr-str :address/a)  7]
                         [:person/otto :person/address   (pr-str :address/b)  7])
              (fs/assert [:person/hans :person/address   (pr-str :address/b)  8])
              (fs/assert [:address/a   :address/number   "11b"           9])
              (fs/assert [:address/b   :address/number   "22b"          10]))]
  (let [addresses-by-street (t/lookup-range @avet-idx [:address/street "street1"])
        addresses-by-number (t/lookup-range @avet-idx [:address/number "22b"])
        abs-es (->> addresses-by-street (map first) set)
        abn-es (->> addresses-by-number (map first) set)
        ae (-> (s/intersection abs-es abn-es) first)
        persons-by-address (t/lookup-range @avet-idx [:person/address (pr-str ae)])
        pe (->> persons-by-address (map first))]
    pe))
