(ns ow.factum.memdb
  (:require [clojure.core.async :refer [go go-loop alts! close! timeout chan]]
            [ow.factum.db :as db]))

(defrecord MemDb [eventstore data ctrlch])

(defn new-memdb [eventstore]
  (->MemDb eventstore (atom []) (chan)))

(defn get-eventstore [this]
  (:eventstore this))

(defn get-data [this]
  @(:data this))

(defn start-polling [{:keys [eventstore data ctrlch] :as this}
                     & {:keys [interval]
                        :or {interval 3000}}]
  (go-loop [[_ ch] []]
    (when (not= ch ctrlch)
      (swap! data #(let [[_ _ _ last-tx _] (first %)
                         next-tx (inc (or last-tx -1))]
                     (println "will query for tx >=" next-tx)
                     (let [newdata (db/get-events eventstore next-tx)]
                       (concat newdata %))))
      (recur (alts! [(timeout interval) ctrlch])))))

(defn stop-polling [this]
  (close! (:ctrlch this)))
