(ns de.samaflost.clj-snake.levels
  (:require [de.samaflost.clj-snake.config :refer [snake-configuration]]))

;; the different level designs

(def max-height
  (get-in @snake-configuration [:board-size :height]))

(def max-width
  (get-in @snake-configuration [:board-size :width]))

(def ^:private layouts
  [(list [(/ max-width 2) (/ max-height 2)])
   (concat (map vector
                (range 4 (- max-width 5) 5)
                (range 4 (- max-height 5) 5)))
   (concat
    (map (partial vector (/ max-width 6))
         (range 6 (- max-height 7)))
    (map (partial vector (* (/ max-width 6) 5))
         (range 6 (- max-height 7))))
   (concat
    (map #(vector % (/ max-height 4))
         (range 1 (/ max-width 8)))
    (map #(vector % (/ max-height 2))
         (range (* 7 (/ max-width 8)) max-width))
    (map #(vector % (* (/ max-height 4) 3))
         (range 1 (/ max-width 8))))
   (concat
    (map (partial vector (/ max-width 2))
         (range 9 (- max-height 10)))
    (map #(vector % (/ max-height 2))
         (range 9 (- max-width 10))))])

(def number-of-levels (count layouts))

(defn internal-walls
  "Produces the walls inside a level."
  [number]
  (layouts (mod number number-of-levels)))
