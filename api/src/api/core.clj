(ns api.core
    (:gen-class)
    (:require [bidi.ring :as ring]
      [ring.middleware.json :as ring-json]
      [clojure.data.json :as json]
      [ring.adapter.jetty :as jetty]
      [clojure.java.jdbc :as jdbc]))

(def mysql-db {:dbtype "mysql"
               :host "localhost"
               :port 3306
               :dbname "clojure-practice"
               :user "root"
               :password "clojure-practice"})

(defn select-todos[]
      (jdbc/query mysql-db
                  ["select * from todo"]))

(defn insert-todo[title]
      (jdbc/insert! mysql-db
                  :todo {:title title}))

(defn- get-todos-handler [request]
       {:status 200
        :headers {
                  "Content-Type" "application/json; charset=utf-8";; TODO: middlewareで設定する
                  "Access-Control-Allow-Origin" "http://localhost:8080" ;; TODO: middlewareで設定する
                  }
        :body (json/write-str (select-todos))})

(defn- post-todo-handler [request]
       (let [id ((first (insert-todo ((request :body) :title))) :generated_key)]
       {:status 200
        :headers {
                  "Content-Type" "application/json; charset=utf-8";; TODO: middlewareで設定する
                  "Access-Control-Allow-Origin" "http://localhost:8080" ;; TODO: middlewareで設定する
                  }
        :body (json/write-str {:id id})}))

(defn- not-found-handler [request]
       {:status 404
        :headers {
                  "Content-Type" "application/json; charset=utf-8" ;; TODO: middlewareで設定する
                  "Access-Control-Allow-Origin" "http://localhost:8080" ;; TODO: middlewareで設定する
                  }
        :body (json/write-str {:message "Not Found"})})

(def ^:private route
  ["/" {"todo" {:get get-todos-handler :post post-todo-handler}
        true not-found-handler}])

(def ^:private handler
  (-> (ring/make-handler route)
      (ring-json/wrap-json-body {:key-fn keyword})))

(defn -main
      [& args]
      (jetty/run-jetty handler {:host "localhost"
                                :port 8081
                                :join? false}))
