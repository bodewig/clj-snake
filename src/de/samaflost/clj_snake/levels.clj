(ns de.samaflost.clj-snake.levels
  (:require [de.samaflost.clj-snake.config :refer [snake-configuration]]))

;; the different level designs

(def max-height
  (get-in @snake-configuration [:board-size :height]))

(def max-width
  (get-in @snake-configuration [:board-size :width]))

(def number-of-levels 5)

(defmulti internal-walls (fn [number] (mod number number-of-levels)))

(defmethod internal-walls 0 [number]
  (list [(/ max-width 2) (/ max-height 2)]))

(defmethod internal-walls 1 [number]
  (concat (map vector
               (range 4 (- max-width 5) 5)
               (range 4 (- max-height 5) 5))))

(defmethod internal-walls 2 [number]
  (concat
   (map (partial vector (/ max-width 6))
        (range 6 (- max-height 7)))
   (map (partial vector (* (/ max-width 6) 5))
        (range 6 (- max-height 7)))))

(defmethod internal-walls 3 [number]
  (concat
   (map #(vector % (/ max-height 4))
        (range 1 (/ max-width 8)))
   (map #(vector % (/ max-height 2))
        (range (* 7 (/ max-width 8)) max-width))
   (map #(vector % (* (/ max-height 4) 3))
        (range 1 (/ max-width 8)))))

(defmethod internal-walls 4 [number]
  (concat
   (map (partial vector (/ max-width 2))
        (range 9 (- max-height 10)))
   (map #(vector % (/ max-height 2))
        (range 9 (- max-width 10)))))
