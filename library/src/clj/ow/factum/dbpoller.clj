(ns ow.factum.dbpoller
  (:require [clojure.core.async :refer [go go-loop alts! close! timeout chan put! >! take!]]
            [ow.factum.backend :as b]))

(defn new-dbpoller [backend eventch]
  {:backend backend
   :eventch eventch
   :ctrlch (chan)
   :last-tid (atom -1)})

(defn start-polling [{:keys [backend eventch ctrlch last-tid] :as this}
                     & {:keys [interval]}]
  (go-loop [[_ ch] []]
    (if (not= ch ctrlch)
      (let [next-tid (inc @last-tid)
            newrows (do (println "will query for tid >=" next-tid)
                        (b/get-items backend next-tid))]
        (doseq [[_ _ _ t _ :as row] newrows]
          (println "sending event")
          (put! eventch row)
          (when (> t @last-tid)
            (println "increasing to tid" t)
            (reset! last-tid t)))
        (recur (alts! [(timeout (or interval 3000)) ctrlch])))
      (println "stopped polling"))))

(defn stop-polling [this]
  (put! (:ctrlch this) ::stop))
