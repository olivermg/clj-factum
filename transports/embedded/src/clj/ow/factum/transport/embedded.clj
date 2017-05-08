(ns ow.factum.transport.embedded
  (:require [clojure.core.async :refer [chan #_pipe] :as a]))

(defn embedded-server [on-connect]
  {:on-connect on-connect})

(defn embedded-client [recv-ch send-ch {:keys [on-connect] :as server}]
  (on-connect send-ch  ;; recv-ch for server
              recv-ch) ;; send-ch for server
  #_(pipe send-ch (:recv-ch server))
  #_(pipe (:send-ch server) recv-ch)
  {:recv-ch recv-ch
   :send-ch send-ch})
