(ns ow.factum.backend)

(defprotocol Backend
  (get-items [this since-tx])
  (new-eid [this])
  (new-txid [this])
  (save [this facts]))

(defn get-items-all [this]
  (get-items this 0))

(defn add-facts [this facts]
  "Adds one or more facts within one single transaction."
  (let [txid (new-txid this)]
    (->> (sequence (comp (map #(->> (concat % (repeat nil))
                                    (take 5)
                                    vec))
                         (map #(assoc % 3 txid 4 :add)))
                   facts)
         (save this))))

(defn retract-facts [this facts]
  "Retracts one or more facts within one single transaction."
  (let [txid (new-txid this)]
    (->> (sequence (comp (map #(->> (concat % (repeat nil))
                                    (take 5)
                                    vec))
                         (map #(assoc % 3 txid 4 :retract)))
                   facts)
         (save this))))
