(ns de.samaflost.clj-snake.painting
  (:use [de.samaflost.clj-snake.config :only [pixel-per-point]]))

(defmulti paint (fn [g item] (:type item)))

(defmethod paint :snake [g snake]
  ^{:doc "paint the snake"}
  (doseq [pt (:body snake)]
    (.setColor g (:color snake))
    (.fillRect g
               (* (first pt) pixel-per-point)
               (* (second pt) pixel-per-point)
               pixel-per-point pixel-per-point)))

(defmethod paint :apple [g apple]
  ^{:doc "paint the apple"}
  (let [loc (:location apple)]
    (.setColor g (:color apple))
    (.fillOval g
               (* (first loc) pixel-per-point)
               (* (second loc) pixel-per-point)
               pixel-per-point pixel-per-point)))

