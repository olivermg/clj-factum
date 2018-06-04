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



#_(defn- binary-tree-leafnode? [{:keys [l r] :as this}]
  (and (nil? l) (nil? r)))

#_(defrecord Binary+TreeNode [k* v* l r]

  TreeModify

  (add [this k v]
    (let [[l r] (if (<= (compare k k*) 0)
                  [(if-not (nil? l)
                     (add l k v)
                     (Binary+TreeNode. k v nil nil))
                   r]
                  [l
                   (if-not (nil? r)
                     (add r k v)
                     (Binary+TreeNode. k v nil nil))])]
      (BinaryTreeNode. k* )))

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



(defrecord B+Tree [b size children leaf?]

  TreeModify

  (add [this k v]
    (letfn [(new-leafnode []
              (->B+Tree b 0 (rm/range-map :find-ceiling nil) true))
            (new-interiornode []
              (->B+Tree b 0 (rm/range-map :find-ceiling (->B+Tree b 0 (rm/range-map :find-ceiling nil))) false))
            (balance [{:keys [children] :as this}]
              (if (< (:size children) b)
                this
                ))]

      (if leaf?
        (if (< size b)
          [(->B+Tree b (inc size) (assoc children k v) leaf?)]
          (let [children (assoc children k v)
                size (inc size)
                [ks1 ks2] (partition-all (-> size (/ 2) Math/ceil int) (keys children))
                [cs1 cs2] [(select-keys children ks1) (select-keys children ks2)]]
            [(->B+Tree b (count cs1) cs1 leaf?)
             (->B+Tree b (count cs2) cs2 leaf?)]))

        (let [child (get children k)]
          (add child k v)
          #_(->B+Tree b (inc size) (assoc children k v) leaf?)))))

  TreeSearch

  (search [this k]
    (when-let [child (get children k)]
      (if (satisfies? TreeSearch child)
        (search child k)
        child))))


(defn b+tree [b]
  (->B+Tree b 0 (rm/range-map :find-ceiling nil) true))
