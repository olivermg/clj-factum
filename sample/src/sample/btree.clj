(ns sample.btree
  (:require [sample.rangemap :as rm]))



(defprotocol TreeModify
  (add [this k v]))

(defprotocol TreeSearch
  (search [this k]))



(defrecord BinaryTreeNode [k* v* l r]

  TreeModify

  (add [this k v]
    (let [[l r] (if (<= (compare k k*) 0)
                  [(if-not (nil? l)
                     (add l k v)
                     (BinaryTreeNode. k v nil nil))
                   r]
                  [l
                   (if-not (nil? r)
                     (add r k v)
                     (BinaryTreeNode. k v nil nil))])]
      (BinaryTreeNode. k* v* l r)))

  TreeSearch

  (search [this k]
    (let [cres (compare k k*)]
      (cond
        (< cres 0) (when-not (nil? l)
                     (search l k))
        (> cres 0) (when-not (nil? r)
                     (search r k))
        true v*))))



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



#_(defrecord B+Tree [b size key-fn children root? leaf?]

  TreeModify

  (add* [this v]
    (let [k (key-fn v)]
      (if leaf?
        (->B+Tree b (inc size) key-fn (assoc children k v) root? leaf?)
        (if-let [child (get children k)]
          (add child v)
          (->B+Tree b (inc size) key-fn (assoc children k v) root? leaf?)))))

  TreeSearch

  (search* [this k]
    (when-let [child (get children k)]
      (if (satisfies? TreeSearch child)
        (search child k)
        child))))


#_(defn b+tree [b key-fn & {:keys [root? leaf?]
                          :or {root? true
                               leaf? true}}]
  (let [children (if leaf?
                   {}
                   (rm/range-map :find-ceiling))]
    (->B+Tree b 0 key-fn children root? leaf?)))
