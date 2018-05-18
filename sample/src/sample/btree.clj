(ns sample.btree
  (:require [sample.rangemap :as rm]))


(defprotocol TreeModify
  (add [this v]))

(defprotocol TreeSearch
  (search [this k]))


(defrecord B+Tree [b slots])

(extend-type B+Tree

  TreeModify

  (add [{:keys [slots] :as this} v]
    )

  TreeSearch

  (search [this k]
    ))
