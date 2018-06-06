(ns sample.btree
  (:require [sample.rangemap :as rm]
            [clojure.tools.logging :as log]))



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


(declare insert*)

(defn- internal-node [b ks vs size]
  {:pre [(= (count vs)
            (inc (count ks)))]}
  {:b b
   :type :internal
   :ks ks
   :vs vs
   :size size})

(defn- inode-lookup [{:keys [ks vs] :as n} k]
  ;;; TODO: implement binary search
  (loop [[k* & ks*] ks
         [v* & vs*] vs]
    (cond
      (nil? k*)             [::inf v*]
      (<= (compare k k*) 0) [k* v*]
      true                  (recur ks* vs*))))

(defn- inode-split [{:keys [b ks vs size] :as n}]
  (let [partition-size  (-> size (/ 2) Math/ceil int)
        [ks1 ks2]       (partition-all partition-size ks)
        [ks1 nk]        [(butlast ks1) (last ks1)]
        [vs1 vs2a vs2b] (partition-all partition-size vs)
        vs2             (concat vs2a vs2b)]
    #_(println "ISPLIT")
    [(internal-node b ks1 vs1 (dec partition-size))
     nk
     (internal-node b ks2 vs2 (- partition-size (rem size 2)))]))

(defn- inode-ins [{:keys [b ks vs size] :as n} k v]
  (if-not (= k ::inf)
    (let [[ks1 ks2] (split-with #(< (compare % k) 0) ks)
          [vs1 vs2] (split-at (count ks1) vs)
          replace?  (= (compare (first ks2) k) 0)
          ks2       (if replace? (rest ks2) ks2)
          vs2       (if replace? (rest vs2) vs2)
          nsize     (if replace? size (inc size))
          nks       (concat ks1 [k] ks2)
          nvs       (concat vs1 [v] vs2)]
      #_(println "IINS" nks nvs)
      (internal-node b nks nvs nsize))
    (let [nvs (-> vs butlast (concat [v]))]
      #_(println "IINS (INF)" ks nvs)
      (internal-node b ks nvs size))))

(defn- inode-insert [{:keys [b ks vs] :as n} k v]
  (let [[childk childv] (inode-lookup n k)
        [n1 nk n2]      (insert* childv k v)
        nn              (if (nil? n2)
                          (inode-ins n childk n1)
                          (-> (inode-ins n nk n1)
                              (inode-ins childk n2)))]
    (if (>= (-> nn :ks count) b)
      (inode-split nn)
      [nn])))


(defn- leaf-node
  ([b ks vs size]
   {:pre [(= (count vs)
             (count ks))]}
   {:b b
    :type :leaf
    :m (->> (interleave ks vs)
            (apply sorted-map))
    :size size})

  ([b]
   (leaf-node b nil nil 0))

  ([b m size]
   {:b b
    :type :leaf
    :m m
    :size size}))

(defn- lnode-lookup [{:keys [m] :as n} k]
  [nil (get m k)])

(defn- lnode-split [{:keys [b m size] :as n}]
  (let [partition-size (-> size (/ 2) Math/ceil int)
        [ks1 ks2] (partition-all partition-size (keys m))
        m1 (apply dissoc m ks2)
        m2 (apply dissoc m ks1)]
    #_(println "LSPLIT" m1 m2)
    [(leaf-node b m1 partition-size)
     (last ks1)
     (leaf-node b m2 (- partition-size (rem size 2)))]))

(defn- lnode-ins [{:keys [b m size] :as n} k v]
  (let [nm (assoc m k v)]
    #_(println "LINS" nm)
    (leaf-node b nm (inc size))))

(defn- lnode-insert [{:keys [b m] :as n} k v]
  (let [nn (lnode-ins n k v)]
    (if (>= (-> nn :m count) b)
      (lnode-split nn)
      [nn])))


(defn- node? [{:keys [type] :as n}]
  (or (= type :internal)
      (= type :leaf)))


(defn- insert* [{:keys [type] :as n} k v]
  (case type
    :internal (inode-insert n k v)
    :leaf     (lnode-insert n k v)))

(defn- lookup* [{:keys [type] :as n} k]
  #_(print "L")
  (case type
    :internal (inode-lookup n k)
    :leaf     (lnode-lookup n k)))

(defn insert [{:keys [b] :as n} k v]
  #_(println "=== INSERT ===")
  (let [[n1 k n2] (insert* n k v)]
    (if (nil? n2)
      n1
      (internal-node b [k] [n1 n2] 1))))

(defn lookup [n k]
  #_(println "=== LOOKUP ===")
  (loop [[_ v] (lookup* n k)]
    (if (node? v)
      (recur (lookup* v k))
      v)))

(defn tree [b]
  (leaf-node b))


#_(-> (tree 3)
    (insert 5 55)
    (insert 9 99)
    (insert 3 33)
    (insert 4 44)
    (insert 2 22)
    (insert 1 11)
    #_(lookup 3))

#_(let [kvs (take 100000 (repeatedly #(let [k (-> (rand-int 9000000)
                                                (+ 1000000))]
                                      [k (str "v" k)])))
      t (time (reduce (fn [t [k v]]
                        (insert t k v))
                      (tree 100)
                      kvs))]
  [(first kvs)
   (time (lookup t (-> kvs first first)))])


(defrecord B+TreeNode [b size children leaf?]

  TreeModify

  (add [this k v]
    (letfn [(split [children size]
              (println "SPLIT1" (keys children) (type children))
              (let [partition-size (-> size (/ 2) Math/ceil int)
                    [ks1 ks2] (partition-all partition-size (keys children))
                    cs1 (apply dissoc children ks2)  ;; cannot use select-keys here, as that removes sorted-map properties
                    cs2 (apply dissoc children ks1)]
                (println "SPLIT2" ks1 ks2 (type cs1) (type cs2))
                (let [res [{:c cs1
                            :s partition-size
                            :k (last ks1)}
                           {:c cs2
                            :s (- partition-size (rem size 2))
                            :k (last ks2)}]]
                  (println "SPLIT3" res)
                  res)))

            (ins [children size k v]
              (println "INS" size k v children)
              (let [children (assoc children k v)
                    size (inc size)]
                (if (< size b)
                  [{:c children :s size :k k}]
                  (split children size))))

            (add* [{:keys [b children size leaf?] :as this} k v root? i]
              #_(println "ADD*" k v root? leaf?)
              (if (<= i 5)
                (cond
                  leaf? (let [_ (println "LEAF1" k v)
                              [m1 m2] (ins children size k v)]
                          (println "LEAF2" m1 m2)
                          [{:n (->B+TreeNode b (:s m1) (:c m1) true)
                            :k (:k m1)}
                           (when-not (nil? m2)
                             {:n (->B+TreeNode b (:s m2) (:c m2) true)
                              :k (:k m2)})])
                  #_root? #_(let [_ (println "ROOT1" k v)
                              child (get children k)
                              [b1 sk b2] (add* child k v false (inc i))]
                          (println "ROOT2" b1 sk b2)
                          [(if (nil? b2)
                             b1
                             (->B+TreeNode b 1 (rm/range-map :find-ceiling b2 sk b1) false))])
                  true (let [_ (println "TRUE1" k v)
                             child (get children k)
                             orig-k (-> child meta :sample.rangemap/key)
                             [b1 b2] (add* child k v false (inc i))]
                         (println "TRUE2" b1 b2 orig-k)
                         (if (nil? b2)
                           [{:n (->B+TreeNode b size (assoc children orig-k (:n b1)) leaf?)
                             :k orig-k}]
                           (let [children   (-> (dissoc children k)
                                                (assoc (:k b1) (:n b1)))
                                 [m1 m2] (ins children size (:k b2) (:n b2))]
                             (println "TRUE3" m1 m2)
                             [{:n (->B+TreeNode b (:s m1) (:c m1) false)
                               :k (:k m1)}
                              (when-not (nil? m2)
                                {:n (->B+TreeNode b (:s m2) (:c m2) false)
                                 :k (:k m2)})]))))
                (do (println "ABORT")
                    [this])))]

      (println "=== ADD* ===" k v)
      (let [[b1 b2] (add* this k v true 0)]
        (println "ADD*2" b1 b2)
        (let [res (if (nil? b2)
                    (:n b1)
                    (->B+TreeNode b 1
                                  (-> (rm/range-map :find-ceiling)
                                      (assoc (:k b1) (:n b1) :sample.rangemap/fallback (:n b2)))
                                  false))]
          (println "ADD*3" res)
          res))

      (split (-> (rm/range-map :find-ceiling)
                 (assoc 1 11 2 22 3 33 4 44 :sample.rangemap/fallback 55))
             5)))

  TreeSearch

  (search [this k]
    (when-let [child (get children k)]
      (if (satisfies? TreeSearch child)
        (search child k)
        child))))


(defn b+tree [b]
  (->B+TreeNode b 0 (sorted-map) true))
