(ns ow.factum.core
  (:require [clojure.core.logic.pldb :refer [with-db]]
            [ow.factum.db :as db]
            [ow.factum.facts :as f]
            [ow.factum.logic :as l]
            [ow.factum.entities :as e]))

(def fact l/fact)

(defrecord FactEngine [eventstore])

(defmacro query [this & body]
  `(let [ldb# (l/get-logic-db (:eventstore ~this))]
     (with-db ldb#
       ~@body)))

(defmacro query1 [this & body]
  `(-> (query ~this ~@body)
       first))

(defn entity [this eid]
  (e/entity (l/get-logic-db (:eventstore this)) eid))
