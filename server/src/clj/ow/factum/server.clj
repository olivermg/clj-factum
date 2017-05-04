(ns ow.factum.server
  (:require [clojure.edn :as edn]
            [clojure.core.async :refer [go-loop <!]]
            [com.stuartsierra.component :as c]
            [taoensso.sente :as sente]
            #_[manifold.stream :as s]
            #_[manifold.deferred :as d]
            #_[aleph.tcp :as tcp]
            #_[aleph.netty :as netty]
            #_[bidi.bidi :refer [match-route path-for] :as bb]
            #_[liberator.core :refer [defresource resource] :as l]
            #_[buddy.sign.jwt :as jwt]
            #_[buddy.auth :refer [authenticated? throw-unauthorized]]
            [ow.chatterbox.core :refer [webapp]])
  (:import [java.io FileNotFoundException])
  (:gen-class))

#_(defresource login
  :allowed-methods [:get]
  :available-media-types ["application/json"]
  :handle-ok (fn [{:keys [request] :as ctx}]
               {:foo "bar"}))

#_(def api-routes ["/api/login" login])

(def ^:private +default-config+
  {:webserver (let [jsecret-api "foofoo"
                    jsecret-ws "barbar"

                    #_api-handler #_(fn [req]
                                  (println "api")
                                  #_(case (:uri req)
                                    "/api/login" {:status 200
                                                  :body (str "{ \"token\": \""
                                                             (jwt/sign {:user 123} jsecret-api)
                                                             "\" }")
                                                  :headers {"Content-Type" "application/json"}}
                                    "/api/authed" (do (when (not (authenticated? req))
                                                        (throw-unauthorized {:message "not authorized for api"}))
                                                      {:status 200
                                                       :body "private api content"
                                                       :headers {"Content-Type" "application/json"}})
                                    {:status 200
                                     :body "public api content"
                                     :headers {"Content-Type" "application/json"}})
                                  (when-let [{:keys [handler]} (match-route api-routes (:uri req))]
                                    (handler req)))

                    ws-handler-setup (fn [ch-recv send-fn connected-uids]
                                       (println "ws")
                                       (go-loop [{:keys [event ?reply-fn] :as msg} (<! ch-recv)]
                                         (println "got ws event:" event)
                                         (when ?reply-fn
                                           (?reply-fn {:foo "bar"}))
                                         (recur (<! ch-recv))))]

                {:server {:port 8899}
                 :all {:dev? true}
                 #_:api #_{:handler api-handler
                       :prefix "/api/"
                       :jws-secret jsecret-api}
                 :ws {:handler-setup ws-handler-setup
                      :prefix "/ws"}})})

#_(defn- echo-server [s info]
  (println s info)
  (s/connect s s))

(defn- config []
  (try
    (-> (slurp "config.edn")
        edn/read-string)
    (catch FileNotFoundException e
      (println "could not find config.edn, falling back to default config")
      +default-config+)))

(defn -main [& args]
  (print "starting... ")
  (let [cfg (config)
        webapp (webapp cfg) #_(tcp/start-server echo-server {:port port})]
    (println "done.")
    (c/start webapp)
    ;;;(netty/wait-for-close s)
    (println "quit.")))
