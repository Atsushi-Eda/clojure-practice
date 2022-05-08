(ns api.core
  (:gen-class)
  (:require
   [bidi.ring :as ring]
   [clojure.java.jdbc :as jdbc]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.cors :as ring-cors]
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

(defn update-todo-completed [id completed]
  (jdbc/update! mysql-db
                :todo {:completed completed} ["id = ?" id]))

(defn delete-todo [id]
  (jdbc/delete! mysql-db
                :todo ["id = ?" id]))

(defn- get-todos-handler [request]
  {:status 200
   :body (select-todos)})

(defn- post-todo-handler [request]
  (let [id ((first (insert-todo ((request :body) :title))) :generated_key)]
    {:status 200
     :body {:id id}}))

(defn- put-todo-handler [request]
  (update-todo-completed ((request :params) :id) ((request :body) :completed))
  {:status 200})

(defn- delete-todo-handler [request]
  (delete-todo ((request :params) :id))
  {:status 200})

(defn- not-found-handler [request]
  {:status 404
   :body {:message "Not Found"}})

(def ^:private route
  ["/" {"todo" {:get get-todos-handler :post post-todo-handler}
        ["todo/" :id] {:delete delete-todo-handler :put put-todo-handler}
        true not-found-handler}])

(def ^:private handler
  (-> (ring/make-handler route)
      (ring-cors/wrap-cors :access-control-allow-origin [#"http://localhost:8080"]
                           :access-control-allow-headers #{:content-type}
                           :access-control-allow-methods #{:get :post :put :delete})
      (ring-json/wrap-json-body {:key-fn keyword})
      (ring-json/wrap-json-response)))

(defn -main
  [& args]
  (jetty/run-jetty handler {:host "localhost"
                            :port 8081
                            :join? false}))
