(ns api.core
  (:gen-class)
  (:require
   [bidi.ring :as ring]
   [clojure.data.json :as json]
   [clojure.java.jdbc :as jdbc]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.json :as ring-json]))

(def mysql-db {:dbtype "mysql"
               :host "localhost"
               :port 3306
               :dbname "clojure-practice"
               :user "root"
               :password "clojure-practice"})

(defn select-todos []
  (jdbc/query mysql-db
              ["select * from todo"]))

(defn insert-todo [title]
  (jdbc/insert! mysql-db
                :todo {:title title}))

(defn delete-todo [id]
  (jdbc/delete! mysql-db
                :todo ["id = ?" id]))

(defn- get-todos-handler [request]
  {:status 200
   :headers {"Content-Type" "application/json; charset=utf-8";; TODO: middlewareで設定する
             "Access-Control-Allow-Origin" "http://localhost:8080" ;; TODO: middlewareで設定する
             }
   :body (json/write-str (select-todos))})

(defn- post-todo-handler [request]
  (let [id ((first (insert-todo ((request :body) :title))) :generated_key)]
    {:status 200
     :headers {"Content-Type" "application/json; charset=utf-8";; TODO: middlewareで設定する
               "Access-Control-Allow-Origin" "http://localhost:8080" ;; TODO: middlewareで設定する
               }
     :body (json/write-str {:id id})}))

(defn- delete-todo-handler [request]
  (delete-todo ((request :params) :todo-id))
  {:status 200
   :headers {"Access-Control-Allow-Origin" "http://localhost:8080" ;; TODO: middlewareで設定する
             }})

(defn- options-todo-handler [request]
  {:status 200
   :headers {"Access-Control-Allow-Origin" "http://localhost:8080"
             "Access-Control-Allow-Headers" "Content-Type" ;; TODO: 各handlerのAccess-Control-Allow-Originと合わせてRing CORSに移行
             }})

(defn- not-found-handler [request]
  {:status 404
   :headers {"Content-Type" "application/json; charset=utf-8" ;; TODO: middlewareで設定する
             "Access-Control-Allow-Origin" "http://localhost:8080" ;; TODO: middlewareで設定する
             }
   :body (json/write-str {:message "Not Found"})})

(def ^:private route
  ["/" {"todo" {:get get-todos-handler :post post-todo-handler :options options-todo-handler}
        ["todo/" :todo-id] {:delete delete-todo-handler}
        true not-found-handler}])

(def ^:private handler
  (-> (ring/make-handler route)
      (ring-json/wrap-json-body {:key-fn keyword})))

(defn -main
  [& args]
  (jetty/run-jetty handler {:host "localhost"
                            :port 8081
                            :join? false}))
