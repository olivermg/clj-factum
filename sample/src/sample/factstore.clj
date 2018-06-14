(ns sample.factstore
  (:refer-clojure :rename {assert assert-clj}))


(defprotocol FactStoreWriter
  (assert* [this fs]))

(defprotocol FactStoreReader
  (get-tail [this start]))


(defn assert [this f & fs]
  (assert* this (cons f fs)))
