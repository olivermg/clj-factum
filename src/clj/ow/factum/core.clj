(ns ow.factum.core
  (:require [clojure.string :as str]
            [clojure.core.logic.pldb :refer [with-db]]
            [ow.factum.db :as db]
            [ow.factum.facts :as f]
            [ow.factum.logic :as l]
            [ow.factum.entities :as e]))

(defrecord FactEngine [eventstore])

(defn ldb [this & {:keys [timestamp]}]
  (l/get-logic-db (:eventstore this)
                  :timestamp timestamp))
