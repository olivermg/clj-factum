(ns ow.factum.memdb
  (:require [clojure.core.async :refer [go go-loop alts! close! timeout chan put! >! take! <!]]
            [ow.factum.backend :as b] ;; TODO: get rid of this dependency, as it may be remote
            ))

(defn new-memdb [eventch]
  (let [data (atom '())]
    (go-loop [ev (<! eventch)]
      (swap! data #(conj % ev))
      (recur (<! eventch)))
    {:eventch eventch
     :data data}))

(defn get-data [this]
  @(:data this))

(defn add-facts [this facts]
  "Adds one or more facts within one single transaction."
  ;;; TODO: inject facts into memdb
  (let [tid (b/new-tid this)]
    (->> (sequence (comp (map #(->> (concat % (repeat nil))
                                    (take 5)
                                    vec))
                         (map #(assoc % 3 tid 4 :add)))
                   facts)
         (b/save this))))

(defn retract-facts [this facts]
  "Retracts one or more facts within one single transaction."
  ;;; TODO: inject facts into memdb
  (let [tid (b/new-tid this)]
    (->> (sequence (comp (map #(->> (concat % (repeat nil))
                                    (take 5)
                                    vec))
                         (map #(assoc % 3 tid 4 :retract)))
                   facts)
         (b/save this))))
