(ns ow.factum.transport.embedded.client
  (:require [clojure.core.async :refer [chan pipe] :as a]))

(defn embedded-client [{:keys [on-connect] :as server}]
  (let [recv-ch (chan)
        send-ch (chan)]
    (on-connect send-ch recv-ch) ;; NOTE: swap send & recv bc. client<->server
    {:recv-ch recv-ch
     :send-ch send-ch
     :server server}))
