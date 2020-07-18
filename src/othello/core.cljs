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
  (->> (for [x (range 8)
                      y (range 8)]
                  [x y])
       (reduce (fn [board coordinate]
                 (if (contains? initial-state coordinate)
                   (assoc board coordinate (initial-state coordinate))
                   (assoc board coordinate blank-tile))){})))

(defonce board-state (r/atom (make-board)))
(defonce turn (r/atom black-tile))

(println @turn)

(defn find-current-stones
  []
  (filter (fn [[_ v]]
            (= v @turn)) @board-state))
(find-current-stones)

(defn is-valid-coord?
  [num]
  (and (num >= 0) (num < 8)))

(defn get-neighbours
  [[x y]]
  )

(println @board-state)
(defn square
  [[x y]]
  [:button
      {:on-click (fn []
                   (swap! board-state assoc [x y] "🥦"))}
   [:span  (@board-state [x y])]])
(println @board-state)
(defn board-component
  []
  (->> (for [x (range 8)
             y (range 8)]
         [x y])
       (map (fn [[x y :as coordinate]]
              (if (= y 7)
                [:span {:key coordinate} (square coordinate) [:div]]
                [:span {:key coordinate} (square coordinate)])))))

(map (fn [[k v]]
       (println k)) @board-state)


(defn app-view []
  [:div
  [:style styles/css]
   (doall(board-component))])
(defn render! []

  (rdom/render
    [app-view]
    (js/document.getElementById "app")))
