(ns ow.factum.storage)


(defprotocol Storage
  (append [this fact])
  (get-all [this]))
