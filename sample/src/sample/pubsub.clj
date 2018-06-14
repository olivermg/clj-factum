(ns sample.pubsub)


(defprotocol Subscriber
  (msg-received [this msg])
  (eof-received [this]))

(defprotocol Publisher
  (subscribe [this subscriber])
  (unsubscribe [this subscriber]))
