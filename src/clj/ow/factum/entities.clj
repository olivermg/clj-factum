(ns ow.factum.entities
  (:require [clojure.core.logic :as l]
            [clojure.core.logic.pldb :as lp]
            [clojure.core.logic.fd :as lfd]
            [clojure.string :as str]
            ;;;[korma.core :as db]
            ;;;[ow.factum.db :as fdb]
            #_[ow.factum.facts :as facts]
            [ow.factum.logic :as logic]))

(defn get-entity [ldb eid]
  "Retrieves entire entity."
  (let [efacts (lp/with-db ldb
                 (l/run* [q]
                   (l/fresh [e a v]
                     (logic/fact e a v (l/lvar))
                     (l/== e eid)
                     (l/== q [e a v (l/lvar)]))))]
    (reduce (fn [s [e a v t]]
              (assoc s a v))
            {:db/eid eid}
            efacts)))

(defn get-entities [ldb attribute value]
  "Retrieves entire entities."
  (let [efacts (lp/with-db ldb
                 (l/run* [q]
                   (l/fresh [e a v e2 a2 v2]
                     (logic/fact e a v (l/lvar))
                     (l/== a attribute)
                     (l/== v value)
                     (logic/fact e2 a2 v2 (l/lvar))
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
        recsym (-> name str/capitalize symbol)
        mapctorsym (-> (str "map->" (str/capitalize name)) symbol)
        sfields (into #{} (comp (map clojure.core/name)
                                (map #(keyword name %)))
                      fields)
        getsym (-> (str "get-" name) symbol)
        getsymm (-> (str "get-" name "s") symbol)
        transform #(if transform
                     `(~transform ~%)
                     %)]
    `(do (defrecord ~recsym [~@fields])

         (defmethod print-method ~recsym [v# ^java.io.Writer w#]
           (print-method (select-keys v# ~sfields) w#))

         (defn ~getsym [~'ldb ~'eid]
           (let [~'e (get-entity ~'ldb ~'eid)]
             (~mapctorsym ~(transform `~'e))))

         (defn ~getsymm [~'ldb ~'attribute ~'value]
           (let [es# (get-entities ~'ldb ~'attribute ~'value)]
             (sequence (map (fn [~'e]
                              (~mapctorsym ~(transform `~'e))))
                       es#))))))
