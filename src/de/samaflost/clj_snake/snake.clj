(ns de.samaflost.clj-snake.snake
  (:import (java.awt Color))
  (:use [de.samaflost.clj-snake.config :only [board-size]]))

(def dirs
  ^{:private true}
  {:up [0 -1] :down [0 1] :left [-1 0] :right [1 0]})

(defn- new-head [snake]
  (vec (apply map + [(first (:body snake)) (get dirs (:direction snake))])))

(defn new-snake [player?]

  ^{:doc "Creates a new snake that will start at the bottom for a
          player or at the top for the program-controlled snake"}
  {
   :body (list [(/ (:width board-size) 2)
                (if player? (dec (:height board-size)) 0)])
   :direction (if player? :up :down)
   :to-grow 4
   :color (if player? Color/GREEN Color/BLUE)
   :type :snake
   })

(defn move [snake]
  ^{:doc "Moves and potentially grows a snake"}
  (let [body (:body snake)
        to-grow (:to-grow snake)]
    (assoc snake :body (cons (new-head snake)
                             (if (> to-grow 0) body (butlast body)))
          :to-grow (if (> to-grow 1) (dec to-grow) 0))))

(defn is-perpendicular? [dir1 dir2]
  (= 0 (reduce + (map * (dirs dir1) (dirs dir2)))))

(defn change-direction [snake new-dir]
  ^{:doc "changes the direction of the snake to new-dir"}
  (if (is-perpendicular? (:direction snake) new-dir)
    (assoc snake :direction new-dir)
    snake))

(defn consume [snake apple]
  ^{:doc "grows the snake as it eats an apple"}
  (assoc snake :to-grow
         (+ (:to-grow snake) (/ (:remaining-nutrition apple) 25))))

(defn head [snake]
  {:location (first (:body snake))})

(defn tail [snake]
  {:body (next (:body snake))})

