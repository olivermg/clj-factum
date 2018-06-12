(ns sample.btree
  (:refer-clojure :rename {iterate iterate-clj})
  (:require [sample.tree :as t]
            [sample.rangemap :as rm]
            [sample.partiallycomparablekey :as pck]
            [sample.memoryfactstore :as mfs]
            [clojure.tools.logging :as log]))


(defn assoc-if-not-nil [m k v]
  (if-not (nil? k)
    (assoc m k v)
    m))


#_(defrecord BinaryTreeNode [k* v* l r]

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



#_(declare insert*)

#_(defn- internal-node [b ks vs size]
  {:pre [(= (count vs)
            (inc (count ks)))]}
  {:b b
   :type :internal
   :ks ks
   :vs vs
   :size size})

#_(defn- inode-lookup [{:keys [ks vs] :as n} k]
  ;;; TODO: implement binary search
  (loop [[k* & ks*] ks
         [v* & vs*] vs]
    (cond
      (nil? k*)             [::inf v*]
      (<= (compare k k*) 0) [k* v*]
      true                  (recur ks* vs*))))

#_(defn- inode-split [{:keys [b ks vs size] :as n}]
  (let [partition-size  (-> size (/ 2) Math/ceil int)
        [ks1 ks2]       (partition-all partition-size ks)
        [ks1 nk]        [(butlast ks1) (last ks1)]
        [vs1 vs2a vs2b] (partition-all partition-size vs)
        vs2             (concat vs2a vs2b)]
    #_(println "ISPLIT")
    [(internal-node b ks1 vs1 (dec partition-size))
     nk
     (internal-node b ks2 vs2 (- partition-size (rem size 2)))]))

#_(defn- inode-ins [{:keys [b ks vs size] :as n} k v]
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

#_(defn- inode-insert [{:keys [b ks vs] :as n} k v]
  (let [[childk childv] (inode-lookup n k)
        [n1 nk n2]      (insert* childv k v)
        nn              (if (nil? n2)
                          (inode-ins n childk n1)
                          (-> (inode-ins n nk n1)
                              (inode-ins childk n2)))]
    (if (>= (-> nn :ks count) b)
      (inode-split nn)
      [nn])))


#_(defn- leaf-node
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

#_(defn- lnode-lookup [{:keys [m] :as n} k]
  [nil (get m k)])

#_(defn- lnode-split [{:keys [b m size] :as n}]
  (let [partition-size (-> size (/ 2) Math/ceil int)
        [ks1 ks2] (partition-all partition-size (keys m))
        m1 (apply dissoc m ks2)
        m2 (apply dissoc m ks1)]
    #_(println "LSPLIT" m1 m2)
    [(leaf-node b m1 partition-size)
     (last ks1)
     (leaf-node b m2 (- partition-size (rem size 2)))]))

#_(defn- lnode-ins [{:keys [b m size] :as n} k v]
  (let [nm (assoc m k v)]
    #_(println "LINS" nm)
    (leaf-node b nm (inc size))))

#_(defn- lnode-insert [{:keys [b m] :as n} k v]
  (let [nn (lnode-ins n k v)]
    (if (>= (-> nn :m count) b)
      (lnode-split nn)
      [nn])))


#_(defn- node? [{:keys [type] :as n}]
  (or (= type :internal)
      (= type :leaf)))


#_(defn- insert* [{:keys [type] :as n} k v]
  (case type
    :internal (inode-insert n k v)
    :leaf     (lnode-insert n k v)))

#_(defn- lookup* [{:keys [type] :as n} k]
  #_(print "L")
  (case type
    :internal (inode-lookup n k)
    :leaf     (lnode-lookup n k)))

#_(defn insert [{:keys [b] :as n} k v]
  #_(println "=== INSERT ===")
  (let [[n1 k n2] (insert* n k v)]
    (if (nil? n2)
      n1
      (internal-node b [k] [n1 n2] 1))))

#_(defn lookup [n k]
  #_(println "=== LOOKUP ===")
  (loop [[_ v] (lookup* n k)]
    (if (node? v)
      (recur (lookup* v k))
      v)))


(defprotocol B+TreeModifyable
  (insert [this k v leaf-neighbours]))

(defprotocol B+TreeLookupable
  (lookup [this k])
  (lookup-range [this k klen leaf-neighbours]))

(defprotocol B+TreeLeafNodeIterable
  (iterate-leafnodes [this]))


(defn- cmp-keys [k1 k2]
  (if (or (not (sequential? k1))
          (not (sequential? k2)))
    (compare k1 k2)
    (let [ctor (cond (vector? k1) vec
                     (list? k1)   list*)
          kl1 (count k1)
          kl2 (count k2)]
      (cond
        (= kl1 kl2) (compare k1 k2)
        (< kl1 kl2) (compare k1 (->> k2 (take kl1) ctor))
        (> kl1 kl2) (compare (->> k1 (take kl2) ctor) k2)))))


(defrecord B+TreeInternalNode [b size ks vs]

  B+TreeModifyable

  (insert [this k v leaf-neighbours]
    (letfn [(split [{:keys [b ks vs size] :as n}]
              (let [partition-size  (-> size (/ 2) Math/ceil int)
                    [ks1 ks2]       (partition-all partition-size ks)
                    [ks1 nk]        [(butlast ks1) (last ks1)]
                    [vs1 vs2a vs2b] (partition-all partition-size vs)
                    vs2             (concat vs2a vs2b)]
                [(->B+TreeInternalNode b (dec partition-size) ks1 vs1)
                 nk
                 (->B+TreeInternalNode b (- partition-size (rem size 2)) ks2 vs2)]))

            (ins [{:keys [b ks vs size] :as n} k v]
              (if-not (= k ::inf)
                (let [[ks1 ks2] (split-with #(< (cmp-keys % k) 0) ks)
                      [vs1 vs2] (split-at (count ks1) vs)
                      replace?  (= (cmp-keys (first ks2) k) 0)
                      ks2       (if replace? (rest ks2) ks2)
                      vs2       (if replace? (rest vs2) vs2)
                      nsize     (if replace? size (inc size))
                      nks       (concat ks1 [k] ks2)
                      nvs       (concat vs1 [v] vs2)]
                  (->B+TreeInternalNode b nsize nks nvs))
                (let [nvs (-> vs butlast (concat [v]))]
                  (->B+TreeInternalNode b size ks nvs))))]

      (let [[childk childv]  (lookup this k)
            [n1 nk n2 nlnbs] (insert childv k v leaf-neighbours)
            nn               (if (nil? n2)
                               (ins this childk n1)
                               (-> (ins this nk n1)
                                   (ins childk n2)))]
        (if (>= (-> nn :size) b)
          (concat (split nn) [nlnbs])
          [nn nil nil nlnbs]))))

  B+TreeLookupable

  (lookup [this k]
    (loop [[k* & ks*] ks
           [v* & vs*] vs]
      (cond
        (nil? k*)              [::inf v*]
        (<= (cmp-keys k k*) 0) [k* v*]
        true                   (recur ks* vs*))))

  (lookup-range [this k _ _]
    (lookup this k))

  B+TreeLeafNodeIterable

  (iterate-leafnodes [this]
    (lazy-seq (mapcat iterate-leafnodes vs))))


(defrecord B+TreeLeafNode [b size m]

  B+TreeModifyable

  (insert [this k v leaf-neighbours]
    (letfn [(insert-leaf-neighbours [n1]
              (let [{pleaf :prev nleaf :next} (get leaf-neighbours this)
                    {ppleaf :prev}            (when-not (nil? pleaf)
                                                (get leaf-neighbours pleaf))
                    {nnleaf :next}            (when-not (nil? nleaf)
                                                (get leaf-neighbours nleaf))]
                (-> leaf-neighbours
                    (assoc-if-not-nil pleaf {:prev ppleaf :next n1})
                    (dissoc this)
                    (assoc-if-not-nil n1    {:prev pleaf  :next nleaf})
                    (assoc-if-not-nil nleaf {:prev n1     :next nnleaf}))))

            (update-leaf-neighbours [n1 n2]
              (let [{pleaf :prev nleaf :next} (get leaf-neighbours this)
                    {ppleaf :prev}            (when-not (nil? pleaf)
                                                (get leaf-neighbours pleaf))
                    {nnleaf :next}            (when-not (nil? nleaf)
                                                (get leaf-neighbours nleaf))]
                (-> leaf-neighbours
                    (assoc-if-not-nil pleaf {:prev ppleaf :next n1})
                    (dissoc this)
                    (assoc-if-not-nil n1    {:prev pleaf  :next n2})
                    (assoc-if-not-nil n2    {:prev n1     :next nleaf})
                    (assoc-if-not-nil nleaf {:prev n2     :next nnleaf}))))

            (split [{:keys [b m size] :as n}]
              (let [partition-size (-> size (/ 2) Math/ceil int)
                    [ks1 ks2] (partition-all partition-size (keys m))
                    m1 (apply dissoc m ks2)
                    m2 (apply dissoc m ks1)
                    n1size partition-size
                    n2size (- partition-size (rem size 2))
                    n1 (->B+TreeLeafNode b n1size m1)
                    n2 (->B+TreeLeafNode b n2size m2)
                    nleafnbs (update-leaf-neighbours n1 n2)]
                [n1 (last ks1) n2 nleafnbs]))

            (ins [k v]
              (let [nsize (if (contains? m k) size (inc size))
                    nm (assoc m k v)]
                (->B+TreeLeafNode b nsize nm)))]

      (let [nn (ins k v)]
        (if (>= (-> nn :size) b)
          (split nn)
          [nn nil nil (insert-leaf-neighbours nn)]))))

  B+TreeLookupable

  (lookup [this k]
    [k (get m k)])

  (lookup-range [this k klen leaf-neighbours]
    (when (>= (cmp-keys k (-> m keys first)) 0)
      (let [matching-keys (->> (keys m)
                               (filter #(= (cmp-keys % k) 0)))]
        ;;; TODO: we don't really need k in this case, as it is only needed for when we get
        ;;;   invoked by insert. as this never happens for lookup-range, we could get rid of
        ;;;   k (i.e. result in 2-tuple vector form). however, currently lookup* (below) relies
        ;;;   on the format to be a 2-tuple, so we leave it like that for the moment:
        [k (lazy-seq
            (concat (-> (select-keys m matching-keys)
                        vals
                        vec)
                    (when-let [next (-> (get leaf-neighbours this) :next)]
                      (second (lookup-range next k klen leaf-neighbours)))))])))

  B+TreeLeafNodeIterable

  (iterate-leafnodes [this]
    [this]))


(defn lookup* [node lookup-fn]
  (loop [[_ v] (lookup-fn node)]
    (if (satisfies? B+TreeLookupable v)
      (recur (lookup-fn v))
      v)))

(defrecord B+Tree [b root leaf-neighbours]

  t/TreeModifyable

  (insert [this k v]
    (let [[n1 k n2 nlnbs] (insert root k v leaf-neighbours)
          nroot (if (nil? n2)
                  n1
                  (->B+TreeInternalNode b 1 [k] [n1 n2]))]
      (->B+Tree b nroot nlnbs)))

  t/TreeLookupable

  (lookup [this k]
    (lookup* root #(lookup % k)))

  (lookup-range [this k]
    (lookup* root #(lookup-range % k (count k) leaf-neighbours)))

  B+TreeLeafNodeIterable

  (iterate-leafnodes [this]
    (iterate-leafnodes root)))


(defn b+tree [b]
  (->B+Tree b (->B+TreeLeafNode b 0 (sorted-map)) {}))


#_(-> (b+tree 3)
    (t/insert [:a 5] 55)
    (t/insert [:a 9] 99)
    (t/insert [:a 3] 33)
    (t/insert [:a 4] 44)
    (t/insert [:a 2] 22)
    (t/insert [:a 1] 11)
    (t/lookup-range [:a 3]))

#_(let [kvs (take 100000 (repeatedly #(let [k (-> (rand-int 9000000)
                                                (+ 1000000))]
                                      [k (str "v" k)])))
      ts (atom [])
      t (time (reduce (fn [t [k v]]
                        (let [nt (t/insert t k v)]
                          (swap! ts #(take 10 (conj % nt)))
                          #_(reset! ts t)
                          nt))
                      (b+tree 100)
                      kvs))
      kv1 (first kvs)
      k1 (first kv1)]
  #_(Thread/sleep 120000)
  [kv1 (count @ts) (time (t/lookup t k1))])

#_(let [kvs (for [k1 [:a :b :c]
                k2 ["x" "y" "z"]
                k3 [1 3 5 7 9]]
            [[k1 k2 k3] (str (name k1) k2 k3)])
      t (time (reduce (fn [t [k v]]
                        (let [nt (t/insert t k v)]
                          nt))
                      (b+tree 3)
                      kvs))]
  #_(clojure.pprint/pprint t)
  (t/lookup-range t [:b]))



#_(defrecord B+TreeNode [b size children leaf?]

  TreeModify

  (insert [this k v]
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

  (lookup [this k]
    (when-let [child (get children k)]
      (if (satisfies? TreeSearch child)
        (lookup child k)
        child))))


#_(defn b+tree [b]
  (->B+TreeNode b 0 (sorted-map) true))
