(ns ow.factum.entities
  (:require [clojure.core.logic :as l]
            [clojure.core.logic.pldb :as lp]
            [clojure.core.logic.fd :as lfd]
            [clojure.string :as str]
            [clojure.set :refer [union]]
            [ow.factum.logicdb :as fl]))

(defn entity [ldb eid]
  "Retrieves an entity with all its attributes and values
without altering those."
  (let [efacts (lp/with-db ldb
                 (l/run* [q]
                   (l/fresh [e a v]
                     (fl/fact e a v (l/lvar))
                     (l/== e eid)
                     (l/== q [a v]))))]
    (when (not-empty efacts)
      (into {:db/eid eid} efacts))))

#_(defn entity [ldb eid]
  "Retrieves an entity with all its attributes and values.
Note that the underlying fact's namespaced attributes are
converted to non-namespaced attributes during this process."
  (let [e (entity* ldb eid)
        xf (map (fn [[nkw v]]
                  [(-> nkw name keyword) v]))]
    (into {} xf e)))

#_(defn get-entities [ldb attribute value]
  "Retrieves entire entities."
  (let [efacts (lp/with-db ldb
                 (l/run* [q]
                   (l/fresh [e a v e2 a2 v2]
                     (fl/fact e a v (l/lvar))
                     (l/== a attribute)
                     (l/== v value)
                     (fl/fact e2 a2 v2 (l/lvar))
                     (l/== e2 e)
                     (l/== q [e2 a2 v2 (l/lvar)]))))]
    (->> (reduce (fn [s [e a v t]]
                   (assoc-in s [e a] v))
                 {}
                 efacts)
         (map (fn [[k v]]
                (assoc v :db/eid k))))))

(defmacro defentity [name [& fields] & {:keys [transform]}]
  (let [name (clojure.core/name name)
        namekw (keyword name)
        recsym (-> name str/capitalize symbol)
        mapctorsym (-> (str "map->" (str/capitalize name)) symbol)
        printfields (transduce (comp (map clojure.core/name)
                                     (map (fn [n] #{(keyword n)
                                                    (keyword name n)})))
                               union
                               #{:eid :db/eid
                                 :etype :db/etype}
                               fields)
        getsym (-> (str "get-" name) symbol)
        ;;;getsymm (-> (str "get-" name "s") symbol)
        transform #(if transform
                     `(~transform ~%1 ~%2)
                     %2)]
    `(do (defrecord ~recsym [#_~@fields])

         (defmethod print-method ~recsym [v# ^java.io.Writer w#]
           (print-method (select-keys v# ~printfields) w#))

         (defn ~getsym [~'ldb ~'eid]
           (when-let [~'e (some-> (entity ~'ldb ~'eid)
                                  (assoc :db/etype ~namekw))]
             (~mapctorsym ~(transform `~'ldb `~'e))))

         #_(defn ~getsymm [~'ldb ~'attribute ~'value]
           (let [es# (get-entities ~'ldb ~'attribute ~'value)]
             (sequence (map (fn [~'e]
                              (~mapctorsym ~(transform `~'ldb `~'e))))
                       es#))))))
