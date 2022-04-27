(ns web.core
      (:require
            [reagent.dom :as reagent]
            [react-query :as rq :refer (QueryClientProvider QueryClient)]))

(def ^:private todos [{:id 1 :title "牛乳を買う"} {:id 2 :title "牛乳を飲む"}])

(defn app []
      [:div
       (map (fn [todo] [:p {:key (todo :id)} (todo :title)]) todos)])

(defn ^:dev/after-load render
      []
      (reagent/render [app] (.getElementById js/document "app")))

(defn ^:export init []
      (render))
