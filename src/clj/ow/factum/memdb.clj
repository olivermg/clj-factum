(ns ow.factum.memdb
  (:require [clojure.core.async :refer [go go-loop alts! close! timeout chan]]
            [ow.factum.backend :as b]))

(defrecord MemDb [backend data ctrlch])

(defn new-memdb [backend]
  (->MemDb backend (atom []) (chan)))

(defn get-data [this]
  @(:data this))

(defn start-polling [{:keys [backend data ctrlch] :as this}
                     & {:keys [interval]}]
  (go-loop [[_ ch] []]
    (when (not= ch ctrlch)
      (swap! data #(let [[_ _ _ last-tx _] (first %)
                         next-tx (inc (or last-tx -1))]
                     (println "will query for tx >=" next-tx)
                     (let [newdata (b/get-items backend next-tx)]
                       (concat newdata %))))
      (recur (alts! [(timeout (or interval 3000)) ctrlch])))))

(defn stop-polling [this]
  (close! (:ctrlch this)))
