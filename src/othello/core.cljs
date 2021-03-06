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
(defonce no-valid-moves? (r/atom false))

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

(defn get-stone-count
  [stone]
  (->> (filter (fn [[_ current-stone]]
                 (= stone current-stone)) @board-state)
       (count)))

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

(defn print-value
  [data]
  (println data)
  data)

(defn get-winner []
  (if (> (get-stone-count black-tile) (get-stone-count white-tile))
    black-tile
    white-tile))

(defn no-moves
  [fun]
  (if (true? @no-valid-moves?)
    (js/alert (str (get-winner) " wins!"))
    (do (reset! no-valid-moves? true)
           (js/alert "No valid moves.")
           (fun))))

(defn check-if-moves
  [fun data]
  (if (empty? data)
    (no-moves fun)
    (do (reset! no-valid-moves? false)
           (set data))))

(defn valid-moves
  []
  (->> (filter (fn [current]
                 (= (last current) (get-current-turn-icon))) @board-state)
       (keys)
       (mapcat find-valid-moves)
       (filter identity)
       (check-if-moves valid-moves)))

(defn generate-moves
  [origin values]
  (reduce (fn [coll value]
            (let [direction  (first (first value))]
              (->> (loop [current-coordinate origin
                     change-coordinates []]
                (if (= (@board-state current-coordinate) (get-current-turn-icon))
                  change-coordinates
                  (recur (mapv + (change-directions direction) current-coordinate) (conj change-coordinates current-coordinate))))
                   (into coll)))) [] values))

(defn change-tiles!
  [coordinates]
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

       (change-tiles!))
  (change-current-turn!))

(defn square
  [coordinate valid?]
  [:button
       {:on-click (fn []
                   (make-move coordinate))
       :class (when valid? "active")}
   [:span  [@board-state coordinate]]])

(defn header-component
  []
  [:div.header
   [:span.score
    [:h1 black-tile " " [get-stone-count black-tile]]]
   [:span [:h1 "Current turn: " [get-current-turn-icon]]]
   [:span.score
    [:h1 white-tile " " [get-stone-count white-tile]]]])

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
  [header-component]
   [board-component]])

(defn render! []
  (rdom/render
    [app-view]
    (js/document.getElementById "app")))
