(ns de.samaflost.clj-snake.ai
  (:require [de.samaflost.clj-snake.snake :as s]
            [de.samaflost.clj-snake.util :as u]))

;;; AI controlled snake movement

(defmulti choose-directions
  "Changing direction of the AI controlled snake (the first arg).
   Returns a seq of possible directions with decreasing preference."
  (fn [snake game-state] (:strategy snake)))

(defn- acceptable-directions [{:keys [body direction] :as snake}]
  (if (> (count body) 1)
    (concat (filter (partial s/is-perpendicular? direction)
                    (remove #{:stand} (keys s/dirs)))
            (vector direction))
    (remove #{:stand} (keys s/dirs))))

;; 25% chance of changing directions
(defmethod choose-directions :random [{:keys [direction] :as snake} _]
  (distinct
   (shuffle (concat (acceptable-directions snake)
                    (repeat 3 direction)))))

(defn- distance-squared [head item]
  (reduce + (map #(* % %) (map - (:location head) (:location item)))))

(defn closest [head targets]
  (when (seq targets)
    (apply min-key (partial distance-squared head) targets)))

(defn- location-of-head-if-snake-moved-to [snake direction]
  {:location (s/new-head (assoc snake :direction direction)) :direction direction})

(defn- try-to-reach [snake target]
  (sort-by (comp (partial distance-squared target)
                 (partial location-of-head-if-snake-moved-to snake))
           (acceptable-directions snake)))

;; tries to reach the closest apple
(defmethod choose-directions :greedy
  [snake {:keys [apples]}]
  (if-let [apple (closest (s/head snake) @apples)]
    (try-to-reach snake apple)
    (shuffle (acceptable-directions snake))))

(defn pick-direction
  "Pick the prefered direction who's road is clear"
  [snake {:keys [level player balls] :as game-state}]
  (let [places-taken [@level @player @balls (s/tail snake)]]
    (some #(when (u/no-collisions places-taken %) (:direction %))
          (map (partial location-of-head-if-snake-moved-to snake)
               (choose-directions snake game-state)))))


(defn walk
  "Moving the AI controlled snake"
  [{:keys [direction] :as snake} game-state]
  (if-let [new-direction (pick-direction snake game-state)]
    (s/move (assoc snake :direction new-direction))
    (assoc (s/move (assoc snake :direction :stand))
      :direction direction)))

;;; test support strategies only
(defmethod choose-directions :stubborn [{:keys [direction]} _] [direction])
(defmethod choose-directions :clockwise [{:keys [direction]} _]
  [:up :right :down :left])
