(ns sample.impl.kafka
  (:require [clj-karabiner.tree :as t]
            [clj-karabiner.tree.bplustree :as bt]

            [sample.factstore :as fs]
            [sample.pubsub :as ps]))


(defrecord KafkaFactStore []

  fs/FactStoreWriter

  (assert* [this fs]
    )

  fs/FactStoreReader

  (get-tail [this start]
    )

  ps/Publisher

  (subscribe [this subscriber]
    )

  (unsubscribe [this subscriber]
    ))


(defn kafka-factstore []
  (->KafkaFactStore))
