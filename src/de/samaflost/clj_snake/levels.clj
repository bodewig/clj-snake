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
          (diag
            [from-x from-y to-x direction]
            (map #(vector % (+ from-y (* direction (- % from-x))))
                 (range from-x to-x)))
          (frac
            ([of denom] (frac of 1 denom))
            ([of num denom] (int (* (/ of denom) num))))]
    [
     ; single wall
     (list [(frac max-width 2) (frac max-height 2)])
     ; scattered diag
     (concat (map vector
                  (range 4 (- max-width 5) 5)
                  (range 4 (- max-height 5) 5)))
     ; two verticals
     (concat
      (y-stripe 6 (- max-height 7) (frac max-width 6))
      (y-stripe 6 (- max-height 7) (frac max-width 5 6)))
     ; spikes
     (concat
      (x-stripe 1 (frac max-width 8) (frac max-height 4))
      (x-stripe (frac max-width 7 8) max-width (frac max-height 2))
      (x-stripe 1 (frac max-width 8) (frac max-height 3 4)))
     ; cross
     (concat
      (y-stripe 9 (- max-height 10) (frac max-width 2))
      (x-stripe 9 (- max-width 10) (frac max-height 2)))
     ; couple of horizontals
     (remove #(or (= (second %) (first %))
                  (= (second %) (- max-width (first %))))
             (mapcat (partial x-stripe 8) (range 8 (- max-height 8) 8)))
     ; hexagons
     (let [mid-x (frac max-width 2)
           mid-y (frac max-height 2)
           hexagon (fn [diameter]
                     (concat
                      (x-stripe (- mid-x (frac diameter 2)) (- mid-y diameter))
                      (x-stripe (- mid-x (frac diameter 2)) (+ mid-y diameter))
                      (y-stripe (- mid-y (frac diameter 2)) (- mid-x diameter))
                      (y-stripe (- mid-y (frac diameter 2)) (+ mid-x diameter))
                      (diag (- mid-x diameter)
                            (- mid-y (frac diameter 2))
                            (- mid-x (frac diameter 2))
                            -1)
                      (diag (- mid-x diameter)
                            (+ mid-y (frac diameter 2))
                            (- mid-x (frac diameter 2))
                            1)
                      (diag (+ mid-x (frac diameter 2))
                            (+ mid-y diameter)
                            (+ mid-x diameter)
                            -1)
                      (diag (+ mid-x (frac diameter 2))
                            (- mid-y diameter)
                            (+ mid-x diameter)
                            1)
                      ))
               ]
       (remove #(or (< -2 (- mid-x (first %)) 2)
                    (< -2 (- mid-y (second %)) 2))
               (mapcat hexagon [(frac max-height 3 8) (frac max-height 8)])))
     ; box
     (concat
      (x-stripe (frac max-width 5) (frac max-height 8))
      (x-stripe (frac max-width 8) (frac max-height 7 8))
      (y-stripe (frac max-height 8) (frac max-width 8))
      (y-stripe (frac max-height 8) (frac max-width 7 8))
      (y-stripe (frac max-height 8) (frac max-height 4 5) (frac max-width 5))
      (y-stripe (frac max-height 8) (frac max-height 4 5) (frac max-width 4 5)))
     ; KSnake rip-off
     (concat
      (mapcat (partial x-stripe 5) [5 8])
      (mapcat (partial y-stripe 15 max-height) [5 8 (- max-width 6) (- max-width 9)]))
     ; zigzag
     (let [x-step (frac max-width 8)
           y-step (frac max-height 8)]
       (remove #(= (mod (first %) x-step) 0)
               (mapcat #(concat
                         (diag x-step % (* 2 x-step) 1)
                         (diag (* 2 x-step) (+ % y-step) (* 3 x-step) -1)
                         (diag (* 3 x-step) % (* 4 x-step) 1)
                         (diag (* 4 x-step) (+ % y-step) (* 5 x-step) -1)
                         (diag (* 5 x-step) % (* 6 x-step) 1)
                         (diag (* 6 x-step) (+ % y-step) (* 7 x-step) -1))
                       (range y-step (* y-step 7) (frac y-step 5 3)))))
     ]))

(def number-of-levels (count layouts))

(defn internal-walls
  "Produces the walls inside a level."
  [number]
  (layouts (mod number number-of-levels)))
