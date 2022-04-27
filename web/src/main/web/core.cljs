(ns web.core
      (:require
            [reagent.dom :as reagent]
            [react-query :as rq :refer (QueryClientProvider QueryClient)]))

(def queryClient (QueryClient.))

(defn useTodos []
      (rq/useQuery "todos" (fn [] (-> (js/fetch "http://localhost:8081/todo")
                               (.then (fn [response] (.json response)))))))

(defn todo-list []
        (let [response (useTodos)]
             [:div (map (fn [todo] [:p {:key (todo :id)} (todo :title)]) ((js->clj response :keywordize-keys true) :data))]))


(defn app []
      [:> QueryClientProvider {:client queryClient}
          [:f> todo-list]])

(defn ^:dev/after-load render
      []
      (reagent/render [app] (.getElementById js/document "app")))

(defn ^:export init []
      (render))
