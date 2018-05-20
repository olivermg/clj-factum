(ns sample.btree
  (:require [sample.rangemap :as rm]))


(defprotocol TreeModify
  (add [this v]))

(defprotocol TreeSearch
  (search [this k]))


(defrecord B+Tree [b slots root? leaf?]

  TreeModify

  (add [this v]
    (->B+Tree b (assoc slots (last v) v) root? leaf?))

  TreeSearch

  (search [this k]
    (when-let [ref (get slots k)]
      (if (satisfies? TreeSearch ref)
        (search ref k)
        ref))))


(defn b+tree [b & {:keys [root? leaf?]
                   :or {root? true
                        leaf? true}}]
  (->B+Tree b (rm/range-map :find-ceiling) root? leaf?))
