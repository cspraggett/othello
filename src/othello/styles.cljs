(ns othello.styles
  (:require
    [garden.core :as garden]))

  (def style
    [:body
        {:display "flex"
         :align-items "center"
         :justify-content "center"
         :height "100vh"
         :width "100vw"}
         [:button
           {:font-size 50
            :align-self "center"
            :pointer-events "none"}]
         [:.active
          {:background-color "rgba(255, 0, 0, 0.2)"
           :pointer-events "auto"}]])



(println style)
(def css
  (garden/css {} style))
