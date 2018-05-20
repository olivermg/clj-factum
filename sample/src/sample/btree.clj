(ns sample.btree
  (:require [sample.rangemap :as rm]))


(defprotocol TreeModify
  (add [this v]))

(defprotocol TreeSearch
  (search [this k]))


(defrecord B+Tree [b key-fn
                   slots root? leaf?]

  TreeModify

  (add [this v]
    (->B+Tree b key-fn (assoc slots (key-fn v) v) root? leaf?))

  TreeSearch

  (search [this k]
    (get slots k)))


(defn b+tree [b key-fn & {:keys [root? leaf?]
                          :or {root? true
                               leaf? true}}]
  (->B+Tree b key-fn
            (rm/range-map :find-ceiling) root? leaf?))
