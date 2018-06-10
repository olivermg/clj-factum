(ns sample.tree)


(defprotocol TreeModifyable
  (insert [this k v]))

(defprotocol TreeLookupable
  (lookup [this k])
  (lookup-range [this k]))
