(ns api.core
    (:gen-class)
    (:require [bidi.ring :as ring]
      [clojure.data.json :as json]
      [ring.adapter.jetty :as jetty]))

(def ^:private todos [{:id 1 :title "牛乳を買う"} {:id 2 :title "牛乳を飲む"}])

(defn- todo-handler [request]
       {:status 200
        :headers {
                  "Content-Type" "application/json; charset=utf-8";; TODO: middlewareで設定する
                  "Access-Control-Allow-Origin" "http://localhost:8080" ;; TODO: middlewareで設定する
                  }
        :body (json/write-str todos)})

(defn- not-found-handler [request]
       {:status 404
        :headers {
                  "Content-Type" "application/json; charset=utf-8" ;; TODO: middlewareで設定する
                  "Access-Control-Allow-Origin" "http://localhost:8080" ;; TODO: middlewareで設定する
                  }
        :body (json/write-str {:message "Not Found"})})

(def ^:private route
  ["/" {"todo" {:get todo-handler}
        true not-found-handler}])

(def ^:private handler
  (-> (ring/make-handler route)
      ))

(defn -main
      [& args]
      (jetty/run-jetty handler {:host "localhost"
                                :port 8081
                                :join? false}))
