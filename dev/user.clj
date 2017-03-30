(ns user)

;; Let Clojure warn you when it needs to reflect on types, or when it does math
;; on unboxed numbers. In both cases you should add type annotations to prevent
;; degraded performance.
(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
#_(def http-handler
  (wrap-reload #'eventsourcing.server/http-handler))

#_(defn run []
  (figwheel/start-figwheel!))

#_(def browser-repl figwheel/cljs-repl)
