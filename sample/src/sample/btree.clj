(ns sample.btree
  (:require [sample.rangemap :as rm]))


(defprotocol TreeModify
  (add [this v]))

(defprotocol TreeSearch
  (search [this k]))


(defrecord B+Tree [b key-fn
                   slots root? leaf?])

(extend-type B+Tree

  TreeModify

  (add [{:keys [key-fn slots] :as this} v]
    (assoc slots (key-fn v) v))

  TreeSearch

  (search [this k]
    ))


(defn b+tree [b key-fn & {:keys [root? leaf?]
                          :or {root? true
                               leaf? true}}]
  (->B+Tree b key-fn
            (rm/range-map :find-ceiling) root? leaf?))
