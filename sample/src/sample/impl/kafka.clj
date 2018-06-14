(ns sample.impl.kafka
  (:require [clj-karabiner.tree :as t]
            [clj-karabiner.tree.bplustree :as bt]

            [sample.factstore :as fs]
            [sample.pubsub :as ps]))


(defrecord KafkaFactStore [subscribers]

  fs/FactStoreWriter

  (assert* [this fs]
    )

  fs/FactStoreReader

  (get-tail [this start]
    )

  ps/Publisher

  (subscribe [this subscriber]
    (->KafkaFactStore (assoc subscribers subscriber true)))

  (unsubscribe [this subscriber]
    (->KafkaFactStore (dissoc subscribers subscriber))))


(defn kafka-factstore
  ([subscriber & subscribers]
   (->KafkaFactStore (zipmap (cons subscriber subscribers)
                             (repeat true))))
  ([]
   (->KafkaFactStore nil)))
