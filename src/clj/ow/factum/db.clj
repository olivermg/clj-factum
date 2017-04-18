(ns ow.factum.db)

(defprotocol Eventstore
  (get-all [this])
  (new-eid [this])
  (new-txid [this])
  (save [this fact]))

(defn projected-facts [this timestamp]
  "Projects raw facts to a given timestamp.
This effectively filters those facts that are relevant for the given timestamp,
i.e. it removes obsolete old facts that are overriden by newer ones or have been
retracted later on (but before timestamp)."
  (let [rawfacts (get-all this)
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

(defn add-facts [this facts]
  "Adds one or more facts within one single transaction."
  (let [txid (new-txid this)]
    (sequence (comp (map #(->> (concat % (repeat nil))
                               (take 5)
                               vec))
                    (map #(assoc % 3 txid 4 :add))
                    (map #(save this %)))
              facts)))

(defn retract-facts [this facts]
  "Retracts one or more facts within one single transaction."
  (let [txid (new-txid this)]
    (sequence (comp (map #(->> (concat % (repeat nil))
                               (take 5)
                               vec))
                    (map #(assoc % 3 txid 4 :retract))
                    (map #(save this %)))
              facts)))
