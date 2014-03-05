(ns de.samaflost.clj-snake.levels
  (:require [de.samaflost.clj-snake.config :refer [snake-configuration]]))

;; the different level designs

(def max-height
  (get-in @snake-configuration [:board-size :height]))

(def max-width
  (get-in @snake-configuration [:board-size :width]))

(def ^:private layouts
  (letfn [(x-stripe
            ([x-off y] (x-stripe x-off (- max-width x-off) y))
            ([from to y]
               (map #(vector % y) (range from to))))
          (y-stripe
            ([y-off x] (y-stripe y-off (- max-height y-off) x))
            ([from to x]
               (map (partial vector x) (range from to))))
          (frac
            ([of denom] (frac of 1 denom))
            ([of num denom] (int (* (/ of denom) num))))]
    [
     (list [(frac max-width 2) (frac max-height 2)])
     (concat (map vector
                  (range 4 (- max-width 5) 5)
                  (range 4 (- max-height 5) 5)))
     (concat
      (y-stripe 6 (- max-height 7) (frac max-width 6))
      (y-stripe 6 (- max-height 7) (frac max-width 5 6)))
     (concat
      (x-stripe 1 (frac max-width 8) (frac max-height 4))
      (x-stripe (frac max-width 7 8) max-width (frac max-height 2))
      (x-stripe 1 (frac max-width 8) (frac max-height 3 4)))
     (concat
      (y-stripe 9 (- max-height 10) (frac max-width 2))
      (x-stripe 9 (- max-width 10) (frac max-height 2)))
     (filter #(not= (frac max-width 2) (first %))
             (mapcat (partial x-stripe 8) (range 8 (- max-height 8) 8)))
     (concat
      (mapcat (partial x-stripe 5) [5 8])
      (mapcat (partial y-stripe 15 max-height) [5 8 (- max-width 6) (- max-width 9)]))
     (concat
      (x-stripe (frac max-width 5) (frac max-height 8))
      (x-stripe (frac max-width 8) (frac max-height 7 8))
      (y-stripe (frac max-height 8) (frac max-width 8))
      (y-stripe (frac max-height 8) (frac max-width 7 8))
      (y-stripe (frac max-height 8) (frac max-height 4 5) (frac max-width 5))
      (y-stripe (frac max-height 8) (frac max-height 4 5) (frac max-width 4 5)))
     ]))

(def number-of-levels (count layouts))

(defn internal-walls
  "Produces the walls inside a level."
  [number]
  (layouts (mod number number-of-levels)))
