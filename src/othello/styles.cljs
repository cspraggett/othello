(ns othello.styles
  (:require
    [garden.core :as garden]))

  (def style
    [:body
        {:display "flex"
         :align-items "center"
         :justify-content "center"
         :height "100vh"
         :width "100vw"
         :background "darkslategray"}
        [:div.header
         {:display "flex"
          :justify-content "space-between"
          :align-items "center"
          :border "2px solid black"
          :background "beige"}
         ]
        [:span.score
           {:padding "10px"
            :background "lightgrey"}]
         [:button
           {:font-size 50
            :align-self "center"
            :pointer-events "none"}]
         [:.active
          {;; :background-color "rgba(255, 0, 0, 0.2)"
           :pointer-events "auto"}]])



(println style)
(def css
  (garden/css {} style))
