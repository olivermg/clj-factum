(ns sample.memoryfactstore)


(defprotocol FactStoreWriter
  (append* [this fs]))

(defprotocol FactStoreReader
  (get-tail [this start]))


(defn append [this f & fs]
  (append* this (cons f fs)))



(defrecord MemoryFactStore [facts]

  FactStoreWriter

  (append* [this fs]
    (->MemoryFactStore (apply conj facts fs)))

  FactStoreReader

  (get-tail [this start]
    (->> facts
         (drop start)
         vec)))


(defn memory-factstore []
  (->MemoryFactStore []))
