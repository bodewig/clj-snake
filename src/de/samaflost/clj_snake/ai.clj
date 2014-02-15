(ns de.samaflost.clj-snake.ai
  (:require [de.samaflost.clj-snake.config :refer [snake-configuration]]
            [de.samaflost.clj-snake.snake :as s]
            [de.samaflost.clj-snake.util :as u]))

;;; AI controlled snake movement

(defmulti choose-directions
  "Changing direction of the AI controlled snake (the first arg).
   Returns a seq of possible directions with decreasing preference."
  (fn [snake game-state] (:strategy snake)))

(defn- acceptable-directions [{:keys [body direction] :as snake}]
  (let [real-directions (remove #{:stand} (keys s/dirs))]
    (if (> (count body) 1)
      (concat (filter (partial s/is-perpendicular? direction)
                      real-directions)
              (vector direction))
      (if (pos? (second (:location (s/head snake))))
        real-directions
        (remove #{:up} real-directions)))))

;; 25% chance of changing directions
(defn- biased-random-direction [{:keys [direction] :as snake}]
  (distinct
   (shuffle (concat (acceptable-directions snake)
                    (repeat 3 direction)))))

(defmethod choose-directions :random [snake _]
  (biased-random-direction snake))

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

;; tries to reach the player's snake's head
(defmethod choose-directions :aggressive
  [snake {:keys [player]}]
  (try-to-reach snake (s/head (s/move @player))))

(def range-of-vision ^:private
  (reduce + (map #(* % %)
                 [(/ (get-in @snake-configuration [:board-size :width]) 2.5)
                  (/ (get-in @snake-configuration [:board-size :width]) 2.5)])))

;; walks randomly unless it sees an apple or the player within its
;; limited sight - I know snakes smell rather than see
(defmethod choose-directions :short-sighted
  [snake {:keys [apples player]}]
  (let [target (closest (s/head snake) (conj @apples (s/head (s/move @player))))
        dist (distance-squared target (s/head snake))]
    (if (< dist range-of-vision)
      (try-to-reach snake target)
      (biased-random-direction snake))))

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
