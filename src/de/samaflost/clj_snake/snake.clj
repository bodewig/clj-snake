(ns de.samaflost.clj-snake.snake
  (:import (java.awt Color))
  (:require [de.samaflost.clj-snake.config :refer [snake-configuration]]
            [de.samaflost.clj-snake.util :as u]))

;;; snake creation, movement and slicing

(def dirs
  {:up [0 -1] :down [0 1] :left [-1 0] :right [1 0] :stand [0 0]})

(defn new-head [snake]
  (u/next-location (first (:body snake)) (dirs (:direction snake))))

(defn is-perpendicular? [dir1 dir2]
  (= 0 (reduce + (map * (dirs dir1) (dirs dir2)))))

(defn new-snake
  "Creates a new snake that will start at the bottom for a player or
  at the top for the program-controlled snake"
  [player?]
  {:body (list [(/ (get-in @snake-configuration [:board-size :width]) 2)
                (if player?
                  (dec (get-in @snake-configuration [:board-size :height]))
                  0)])
   :direction (if player? :up :down)
   :to-grow 4
   :color (if player? Color/GREEN Color/BLUE)
   :type :snake
   :strategy (keyword (:ai-strategy @snake-configuration))
   })

(defn move
  "Moves and potentially grows a snake"
  [{:keys [body to-grow] :as snake}]
  (let [h (new-head snake)
        b (if (pos? to-grow) body (butlast body))]
    (assoc snake
      :body (if-not (= h (first b)) (cons h b) b)
      :to-grow (if (> to-grow 1) (dec to-grow) 0))))

(defn change-direction
  "changes the direction of the snake to new-dir if new-dir is
   perpendicular to current direction, otherwise the current direction
   is kept"
  [snake new-dir]
  (if (is-perpendicular? (:direction snake) new-dir)
    (assoc snake :direction new-dir)
    snake))

(defn consume
  "grows the snake as it eats an apple"
  [snake apple]
  (assoc snake :to-grow
         (+ (:to-grow snake) (/ (:remaining-nutrition apple) 25))))

(defn head
  "Returns the snake's head as a :location-map"
  [snake]
  {:location (first (:body snake))})

(defn tail
  "Returns the snake's tail (all but its head) as a :body-map"
  [snake]
  {:body (next (:body snake))})

(defn fake-leaving
  "chops of two items from the tail of the snake"
  [snake]
  (assoc snake :body (butlast (butlast (:body snake)))))
