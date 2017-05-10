(ns ow.factum.dbpoller
  (:require [clojure.core.async :refer [go go-loop alts! close! timeout chan put! >! <! take!]]
            [ow.factum.backend :as b]))

(defn dbpoller [backend & {:keys [poll-interval]}]
  (letfn [(make-on-connect [{:keys [backend ctrlch last-tid poll-interval] :as this}]
            (fn [recv-ch send-ch]
              (let [process-row (fn [[_ _ _ t _ :as row]]
                                  (println "sending event")
                                  (when (put! send-ch row)
                                    (when (> t @last-tid)
                                      (println "increasing to tid" t)
                                      (reset! last-tid t))
                                    true))

                    process-rows (fn [[row & restrows]]
                                   (if row
                                     (when (process-row row)
                                       (recur restrows))
                                     true))]

                (go-loop [_ (<! (timeout 100))]

                  (let [next-tid (inc @last-tid)
                        newrows (do (println "will query for tid >=" next-tid)
                                    (b/get-items backend next-tid))]

                    (if (process-rows (reverse newrows))
                      (recur (<! (timeout poll-interval)))
                      (println "stopped polling")))))))]

    (let [this {:backend backend
                :ctrlch (chan)
                :last-tid (atom -1)
                :poll-interval (or poll-interval 3000)}]

      (assoc this :on-connect (make-on-connect this)))))

#_(defn start-polling [{:keys [backend transport ctrlch last-tid] :as this}
                     & {:keys [interval]}]
  (let [sch (:send-ch transport)]
    (go-loop [[_ ch] []]
      (if (not= ch ctrlch)
        (let [next-tid (inc @last-tid)
              newrows (do (println "will query for tid >=" next-tid)
                          (b/get-items backend next-tid))]
          (doseq [[_ _ _ t _ :as row] newrows]
            (println "sending event")
            (put! sch row)
            (when (> t @last-tid)
              (println "increasing to tid" t)
              (reset! last-tid t)))
          (recur (alts! [(timeout (or interval 3000)) ctrlch])))
        (println "stopped polling")))))

#_(defn stop-polling [this]
  (put! (:ctrlch this) ::stop))
