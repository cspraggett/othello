(ns othello.core
 (:require
    [reagent.dom :as rdom]
    [reagent.core :as r]
    [othello.styles :as styles]))

(def black-tile "⚫️")
(def white-tile "⚪️")
(def blank-tile "⬜️")

(def initial-state {[3 3] white-tile
                    [3 4] black-tile
                    [4 3] black-tile
                    [4 4] white-tile})

(defn make-board
  []
  (->> (for [row (range 8)
             column (range 8)]
         [row column])
       (reduce (fn [board coordinate]
                 (if (contains? initial-state coordinate)
                   (assoc board coordinate (initial-state coordinate))
                   (assoc board coordinate blank-tile))){})))

(defonce board-state (r/atom (make-board)))
(defonce turn (r/atom black-tile))

(println @board-state)

(defn square
  [coordinate curse]
  [:button
      {:on-click (fn []
                   (swap! board-state assoc coordinate "🥦"))}
   [:span  @curse]])

(defn board-component
  []
  (->> (for [row (range 8)
             column (range 8)]
         [row column])
       (map (fn [[row column :as coordinate]]
              (if (= column 7)
                [:span {:key coordinate} (square coordinate (r/cursor board-state [coordinate])) [:div]]
                [:span {:key coordinate} (square coordinate (r/cursor board-state [coordinate]))])))))

(def t (r/cursor board-state [[0 0]]))

(defn app-view []
  [:div
  [:style styles/css]
   (doall(board-component))])
(defn render! []

  (rdom/render
    [app-view]
    (js/document.getElementById "app")))
