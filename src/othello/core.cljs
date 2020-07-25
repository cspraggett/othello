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
#_(defonce current-turn (r/atom black-tile))

(defn current-turn
  []
  (let [square-count (->> @board-state
       (filter (fn [[_ value]]
                 (not= value blank-tile)))
       count)]
    (if (zero? (mod square-count 2))
      black-tile
      white-tile)))

(defn get-current-turn-icon
  []
  (current-turn))

(defn get-other-icon
  []
  (if (= (get-current-turn-icon) black-tile)
    white-tile
    black-tile))
(get-other-icon)
;; change-directions matches the direction of change on the board and
;; the x y values in a vector, to add to a coordinate.

(def change-directions {:diagonal-up-left [-1 -1]
                        :up [-1 0]
                        :diagonal-up-right [-1 1]
                        :left [0 -1]
                        :right [0 1]
                        :diagonal-down-left [1 -1]
                        :down [1 0]
                        :diagonal-down-right [1 1]})


(defn get-neighbouring-coordinates
  "Takes a set of coordinates and maps the direction and coordinates of neighbouring squares."
  [current-coordinates]
  (->> change-directions
       (mapv (fn [[direction change-values]]
               (->> (mapv + current-coordinates change-values)
                    (assoc {} direction))))))

(defn find-neighbouring-stones
  [board opponents-stone neighbours]
  (->> neighbours
       (filter (fn [neighbour]
                 (let [[_ coordinate] (first neighbour)]
                 (= (board coordinate) opponents-stone))))))

(defn check-next-square
  [board opponents-stone target square]
  (let [[direction coordinate] (first square)
         next-square-value (mapv + (change-directions direction) coordinate)]
    (condp = (board next-square-value)
      target next-square-value
      opponents-stone (check-next-square board opponents-stone target {direction next-square-value})
      nil)))

(defn find-valid-moves
  [coordinate]
  (->> coordinate
      (get-neighbouring-coordinates)
      (find-neighbouring-stones @board-state (get-other-icon))
      (mapv (fn [current]
              (check-next-square @board-state (get-other-icon) blank-tile current)))))

(defn valid-moves
  []
  (->> (filter (fn [current]
                 (= (last current) (get-current-turn-icon))) @board-state)
       (keys)
       (mapcat find-valid-moves)
       (set)))

(defn generate-moves
  [origin values]
  (reduce (fn [coll value]
            (let [direction  (first (first value))]
              (->> (loop [current-coordinate origin
                     change-coordinates []]
                (if (= (@board-state current-coordinate) (get-current-turn-icon))
                  change-coordinates
                  (recur (mapv + (change-directions direction) current-coordinate) (conj change-coordinates current-coordinate))))
                   (conj coll)))) [] values))

(defn change-tiles!
  [coordinates]
  (println "in change-tiles! "coordinates)
  (doseq [current coordinates]
    (swap! board-state assoc current (get-current-turn-icon))))

(defn make-move
  [coordinate]
  (->> coordinate
       (get-neighbouring-coordinates)
       (find-neighbouring-stones @board-state (get-other-icon))
       (filterv (fn [current]
               (check-next-square @board-state (get-other-icon) (get-current-turn-icon) current)))
       (generate-moves coordinate)
       (first)
       (change-tiles!)))


#_(make-move [1 4])

(defn square
  [coordinate valid?]
  [:button
       {:on-click (fn []
                    (println "clicked: " coordinate)
                   (make-move coordinate))
       :class (when valid? "active")}
   [:span  [@board-state coordinate]]])

(defn board-component
  []
  (let [valid (valid-moves)]
  (->> (for [row (range 8)
             column (range 8)]
         [row column])
       (reduce (fn [board [_row column :as coordinate]]
                 (conj board [:span {:key coordinate} [square coordinate (valid coordinate)] (when (= column 7) [:br])]))
               [:div]))))

(defn app-view []
  [:div
  [:style styles/css]
   [board-component]])

(defn render! []
  (rdom/render
    [app-view]
    (js/document.getElementById "app")))
