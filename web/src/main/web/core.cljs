(ns web.core
  (:require
   [react-query :as rq :refer (QueryClientProvider QueryClient)]
   [reagent.dom :as reagent]))

(def ^:private queryClient (QueryClient.))

(defn- use-todos []
  (rq/useQuery "todos" (fn [] (-> (js/fetch "http://localhost:8081/todo")
                                  (.then (fn [response] (.json response)))))))

(defn- use-post-todo []
  (rq/useMutation (fn [title] (js/fetch "http://localhost:8081/todo" (clj->js {:method "POST" :headers {:Content-Type "application/json"} :body (js/JSON.stringify (clj->js {:title title}))})))
                  #js {:onSuccess (fn [response title] (-> (.json response) (.then (fn [json] (.setQueryData queryClient "todos" (fn [old] (clj->js (conj (js->clj old) {:id (.-id json) :title title}))))))))}))

(defn- use-update-todo-completed []
  (rq/useMutation (fn [{:keys [:id :completed]}] (js/fetch (str "http://localhost:8081/todo/" id) (clj->js {:method "PUT" :headers {:Content-Type "application/json"} :body (js/JSON.stringify (clj->js {:completed completed}))})))
                  #js {:onSuccess (fn [_ {:keys [:id :completed]}] (.setQueryData queryClient "todos" (fn [old] (clj->js (assoc-in (js->clj old :keywordize-keys true) [(.indexOf (clj->js (map (fn [todo] (todo :id)) (js->clj old :keywordize-keys true))) id) :completed] completed)))))}))

(defn- use-delete-todo []
  (rq/useMutation (fn [id] (js/fetch (str "http://localhost:8081/todo/" id) (clj->js {:method "DELETE"})))
                  #js {:onSuccess (fn [_ id] (.setQueryData queryClient "todos" (fn [old] (clj->js (filter (fn [todo] (not= id (.-id todo))) old)))))}))

(defn- todo-checkbox [todo]
  (let [mutation (use-update-todo-completed)]
    [:input {:type "checkbox" :checked (todo :completed) :onChange (fn [] (mutation.mutate {:id (todo :id) :completed (not (todo :completed))}))}]))

(defn- delete-todo-button [id]
  (let [mutation (use-delete-todo)]
    [:button {:onClick (fn [] (mutation.mutate id))} "x"]))

(defn- todo-item [todo]
  [:div [:f> todo-checkbox todo] [:span (todo :title)] [:f> delete-todo-button (todo :id)]])

(defn- todo-list []
  (let [response (use-todos)]
    [:div (map (fn [todo] ^{:key (todo :id)} [:f> todo-item todo]) ((js->clj response :keywordize-keys true) :data))]))

(defn- todo-form []
  (let [mutation (use-post-todo)]
    [:input {:type "text" :onKeyDown (fn [event] (if (= "Enter" (.-code event)) (mutation.mutate (.-value (.-target event)))))}]))

(defn- app []
  [:> QueryClientProvider {:client queryClient}
   [:f> todo-list]
   [:f> todo-form]])

(defn- ^:dev/after-load render
  []
  (reagent/render [app] (.getElementById js/document "app")))

(defn- ^:export init []
  (render))
