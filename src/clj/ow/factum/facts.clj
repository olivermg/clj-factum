(ns ow.factum.facts
  (:require [ow.factum.db :as db]))

(defn projected-facts [eventstore & {:keys [timestamp]}]
  "Projects raw facts to a given timestamp.
This effectively filters those facts that are relevant for the given timestamp,
i.e. it removes obsolete old facts that are overriden by newer ones or have been
retracted later on (but before timestamp). If no timestamp is given, current time
is being assumed."
  (let [rawfacts (db/get-all eventstore)
        xf (fn [xf]
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
                    :retract (do (vswap! facts* #(assoc-in % [e a] ::retracted))
                                 result))))))]
    (into [] xf rawfacts)))

(defn add-facts [eventstore facts]
  "Adds one or more facts within one single transaction."
  (let [txid (db/new-txid eventstore)]
    (sequence (comp (map #(->> (concat % (repeat nil))
                               (take 5)
                               vec))
                    (map #(assoc % 3 txid 4 :add))
                    (map #(db/save eventstore %)))
              facts)))

(defn retract-facts [eventstore facts]
  "Retracts one or more facts within one single transaction."
  (let [txid (db/new-txid eventstore)]
    (sequence (comp (map #(->> (concat % (repeat nil))
                               (take 5)
                               vec))
                    (map #(assoc % 3 txid 4 :retract))
                    (map #(db/save eventstore %)))
              facts)))
