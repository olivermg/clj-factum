(ns sample.impl.memory
  (:require [clj-karabiner.tree :as t]
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



(let [eavt-idx (atom (bt/b+tree 3))
      aevt-idx (atom (bt/b+tree 3))
      avet-idx (atom (bt/b+tree 3))
      vaet-idx (atom (bt/b+tree 3))
      mfs (-> (memory-factstore (reify ps/Subscriber
                                  (msg-received [this [e a v t :as fact]]
                                    (println "SUB1" fact)
                                    (swap! eavt-idx #(t/insert % [e a v t] fact))
                                    (swap! aevt-idx #(t/insert % [a e v t] fact))
                                    (swap! avet-idx #(t/insert % [a v e t] fact))
                                    (swap! vaet-idx #(t/insert % [v a e t] fact)))))
              (fs/assert [:person/otto :email "otto@foo.com" 1]
                         [:person/otto :firstname "otto" 1]
                         [:person/otto :lastname "wurst" 1])
              (fs/assert [:person/hans :email "hans@foo.com" 2]
                         [:person/hans :firstname "hans" 2]
                         [:person/hans :lastname "wurst" 2])
              (fs/assert [:person/otto :firstname "otto fritz" 3])
              (fs/assert [:person/hans :firstname "hans hugo" 4]))]
  (t/lookup-range @eavt-idx [:person/otto]))
