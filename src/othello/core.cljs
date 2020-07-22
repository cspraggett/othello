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
(defonce current-turn (r/atom black-tile))

(defn get-current-turn-icon
  []
  @current-turn)

(defn get-other-icon
  []
  (if (= (get-current-turn-icon) black-tile)
    white-tile
    black-tile))

(defn change-current-turn!
  []
  (reset! current-turn (get-other-icon)))

(def change-directions {:diagonal-up-left [-1 -1]
                        :up [-1 0]
                        :diagonal-up-right [-1 1]
                        :left [0 -1]
                        :right [0 1]
                        :diagonal-down-left [1 -1]
                        :down [1 0]
                        :diagonal-down-right [1 1]})

(defn get-neighbouring-squares
  [current-square]
  (mapv (fn [[k v]]
          (->> (mapv + current-square v)
               (assoc {} k))) change-directions))

(defn find-adjacent-opponent-squares
  [neighbours]
  (filter (fn [k]
            (= (@board-state (first (vals k))) (get-other-icon))) neighbours))

(defn check-next-square
  [square]
  (let [next-square-value (mapv + (change-directions (first (keys square)))
                                (first (vals square)))]
    (cond
      (= (@board-state next-square-value) blank-tile)
        next-square-value
      (= (@board-state next-square-value) (get-other-icon))
         (check-next-square {(first (keys square)) next-square-value})
         :else false)))

(defn is-empty?
  [squares]
  (->> squares
       (mapv check-next-square)))

(defn find-valid-moves
  [square]
  (-> square
      (get-neighbouring-squares)
      (find-adjacent-opponent-squares)
      (is-empty?)))

(defn valid-moves
  []
  (->> (filter (fn [current]
                 (= (last current) (get-current-turn-icon))) @board-state)
       (keys)
       (map find-valid-moves)
       (apply concat)
       (set)))

(defn square
  [coordinate valid?]
  [:button
       {:on-click (fn []
                   (swap! board-state assoc coordinate (get-current-turn-icon)))
       :class (when valid? "active")}
   [:span  [@board-state coordinate]]])

(defn board-component
  []
  (let [valid (valid-moves)]
  (->> (for [row (range 8)
             column (range 8)]
         [row column])
       (reduce (fn [board [_row column :as coordinate]]
                 (conj board [:span {:key coordinate} [square coordinate (contains? valid coordinate)] (when (= column 7) [:br])]))
               [:div]))))

(defn app-view []
  [:div
  [:style styles/css]
   [board-component]])

(defn render! []
  (rdom/render
    [app-view]
    (js/document.getElementById "app")))
