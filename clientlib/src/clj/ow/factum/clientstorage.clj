(ns ow.factum.clientstorage
  (:require [clojure.core.async :refer [go go-loop alts! close! timeout chan put! >! take! <!]]
            [ow.factum.backend :as b] ;; TODO: get rid of this dependency, as it may be remote
            ))

(defn clientstorage [client-transport]
  (let [rch (:recv-ch client-transport)
        data (atom '())]
    (go-loop [msg (<! rch)]
      (swap! data #(conj % msg))
      (recur (<! rch)))
    {:transport client-transport
     :data data}))

(defn project [this timestamp]
  ;;; TODO: honor timestamp
  "Projects rawfacts to the given timestamp.
This effectively filters those facts that are relevant for the given timestamp,
i.e. it removes obsolete old facts that are overriden by newer ones or have been
retracted later on (but before timestamp). If no timestamp is given, current time
is being assumed."
  (let [xf (fn [xf]
             (let [facts* (volatile! {})]
               (fn
                 ([] (xf))
                 ([result] (xf result))
                 ([result [e a v t action :as input]]
                  (case action
                    :add (case (get-in @facts* [e a])
                           true result
                           ::retracted (do (vswap! facts* #(update-in % [e] dissoc a))
                                           result)
                           nil (do (vswap! facts* #(assoc-in % [e a] true))
                                   (xf result (take 4 input))))
                    :retract (do (when (nil? (get-in @facts* [e a])) ;; TODO: add support for multiple successive retracts
                                   (vswap! facts* #(assoc-in % [e a] ::retracted)))
                                 result))))))]
    (into [] xf @(:data this))))

(defn add-facts [this facts]
  "Adds one or more facts within one single transaction."
  ;;; TODO: inject facts into clientstorage
  (let [tid (b/new-tid this)]
    (->> (sequence (comp (map #(->> (concat % (repeat nil))
                                    (take 5)
                                    vec))
                         (map #(assoc % 3 tid 4 :add)))
                   facts)
         (b/save this))))

(defn retract-facts [this facts]
  "Retracts one or more facts within one single transaction."
  ;;; TODO: inject facts into clientstorage
  (let [tid (b/new-tid this)]
    (->> (sequence (comp (map #(->> (concat % (repeat nil))
                                    (take 5)
                                    vec))
                         (map #(assoc % 3 tid 4 :retract)))
                   facts)
         (b/save this))))
