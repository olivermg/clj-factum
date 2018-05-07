(ns ow.factum.storage.memory
  (:require [ow.factum.storage :as fs]))


(defrecord MemoryStorage [a]

  fs/Storage

  (append [this fact]
    (swap! a #(conj % fact))
    this)

  (get-all [this]
    @a))


(defn memory-storage []
  (->MemoryStorage (atom [])))
