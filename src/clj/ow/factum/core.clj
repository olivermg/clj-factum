(ns ow.factum.core
  (:require [clojure.string :as str]
            [clojure.core.logic.pldb :refer [with-db]]
            [ow.factum.db :as db]
            [ow.factum.facts :as f]
            [ow.factum.logic :as l]
            [ow.factum.entities :as e]))

;;;
;;; FACTS
;;;

(def fact l/fact)

(defmacro query [this & body]
  `(let [ldb# (l/get-logic-db (:eventstore ~this))]
     (with-db ldb#
       ~@body)))

(defmacro query1 [this & body]
  `(-> (query ~this ~@body)
       first))


;;;
;;; ENTITIES
;;;

(defn entity [this eid]
  (e/entity (l/get-logic-db (:eventstore this)) eid))

(defmacro defentity [name [& fields] & {:keys [transform]}]
  (let [name (clojure.core/name name)
        recsym (-> name str/capitalize symbol)
        mapctorsym (-> (str "map->" (str/capitalize name)) symbol)
        printfields (into #{:db/eid}
                          (comp (map clojure.core/name)
                                (map #(keyword name %)))
                          fields)
        getsym (-> (str "get-" name) symbol)
        ;;;getsymm (-> (str "get-" name "s") symbol)
        transform #(if transform
                     `(~transform ~%1 ~%2)
                     %2)]
    `(do (defrecord ~recsym [~@fields])

         (defmethod print-method ~recsym [v# ^java.io.Writer w#]
           (print-method (select-keys v# ~printfields) w#))

         (defn ~getsym [~'this ~'eid]
           (when-let [~'e (entity ~'this ~'eid)]
             (~mapctorsym ~(transform `~'this `~'e))))

         #_(defn ~getsymm [~'ldb ~'attribute ~'value]
           (let [es# (get-entities ~'ldb ~'attribute ~'value)]
             (sequence (map (fn [~'e]
                              (~mapctorsym ~(transform `~'ldb `~'e))))
                       es#))))))


;;;
;;; FACT ENGINE
;;;

(defrecord FactEngine [eventstore])
