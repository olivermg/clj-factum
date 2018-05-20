(ns sample.btree
  (:require [sample.rangemap :as rm]))



(defprotocol TreeModify
  (add [this k v root]))

(defprotocol TreeSearch
  (search [this k]))



(defrecord BinaryTree [k* v* l r]

  TreeModify

  (add [this k v root]
    )

  TreeSearch

  (search [this k]
    ))



#_(defrecord B+TreeChildren [children-map largest-child]

  TreeModify

  (add [this v]
    )

  TreeSearch

  (search [this k]
    (if-let [child (get children-map k)]
      child
      largest-child)))


#_(defn- b+tree-children []
  (->B+TreeChildren (rm/range-map :find-ceiling) nil))



(defrecord B+Tree [b size key-fn children root? leaf?]

  TreeModify

  (add [this v]
    (let [k (key-fn v)]
      (if leaf?
        (->B+Tree b (inc size) key-fn (assoc children k v) root? leaf?)
        (if-let [child (get children k)]
          (add child v)
          (->B+Tree b (inc size) key-fn (assoc children k v) root? leaf?)))))

  TreeSearch

  (search [this k]
    (when-let [child (get children k)]
      (if (satisfies? TreeSearch child)
        (search child k)
        child))))


(defn b+tree [b key-fn & {:keys [root? leaf?]
                          :or {root? true
                               leaf? true}}]
  (let [children (if leaf?
                   {}
                   (rm/range-map :find-ceiling))]
    (->B+Tree b 0 key-fn children root? leaf?)))
