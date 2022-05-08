(ns web.core
  (:require
   [react-query :as rq :refer (QueryClientProvider QueryClient)]
   [reagent.dom :as reagent]))

(def queryClient (QueryClient.))

(defn useTodos []
  (rq/useQuery "todos" (fn [] (-> (js/fetch "http://localhost:8081/todo")
                                  (.then (fn [response] (.json response)))))))

(defn usePostTodo []
  (rq/useMutation (fn [title] (js/fetch "http://localhost:8081/todo" (clj->js {:method "POST" :headers {:Content-Type "application/json"} :body (js/JSON.stringify (clj->js {:title title}))})))
                  #js {:onSuccess (fn [response title] (-> (.json response) (.then (fn [json] (.setQueryData queryClient "todos" (fn [old] (clj->js (conj (js->clj old) {:id (.-id json) :title title}))))))))}))

(defn useDeleteTodo []
  (rq/useMutation (fn [id] (js/fetch (str "http://localhost:8081/todo/" id) (clj->js {:method "DELETE"})))
                  #js {:onSuccess (fn [_ id] (.setQueryData queryClient "todos" (fn [old] (clj->js (filter (fn [todo] (not= id (.-id todo))) old)))))}))

(defn delete-todo-button [id]
  (let [mutation (useDeleteTodo)]
    [:button {:onClick (fn [] (mutation.mutate id))} "x"]))

(defn todo-item [todo]
  [:div [:span (todo :title)] [:f> delete-todo-button (todo :id)]])

(defn todo-list []
  (let [response (useTodos)]
    [:div (map (fn [todo] ^{:key (todo :id)} [:f> todo-item todo]) ((js->clj response :keywordize-keys true) :data))]))

(defn todo-form []
  (let [mutation (usePostTodo)]
    [:input {:type "text" :onKeyDown (fn [event] (if (= "Enter" (.-code event)) (mutation.mutate (.-value (.-target event)))))}]))

(defn app []
  [:> QueryClientProvider {:client queryClient}
   [:f> todo-list]
   [:f> todo-form]])

(defn ^:dev/after-load render
  []
  (reagent/render [app] (.getElementById js/document "app")))

(defn ^:export init []
  (render))
