(ns de.samaflost.clj-snake.ai
  (:require [de.samaflost.clj-snake.snake :as s]
            [de.samaflost.clj-snake.util :as u]))

;;; AI controlled snake movement

(defmulti choose-directions
  "Changing direction of the AI controlled snake (the first arg).
   Returns a seq of possible directions with decreasing preference."
  (fn [snake game-state] (:strategy snake)))

(defn- acceptable-direction-changes [from-direction]
  (filter (partial s/is-perpendicular? from-direction)
          (remove #{:stand} (keys s/dirs))))

;; 25% chance of changing directions
(defmethod choose-directions :random [{:keys [direction]} _]
  (shuffle (concat (acceptable-direction-changes direction)
                   (repeat 4 direction))))

(defn- distance-squared [head item]
  (reduce + (map #(* % %) (map - (:location head) (:location item)))))

(defn closest-apple [head apples]
  (when (seq apples)
    (apply min-key (partial distance-squared head) apples)))

(defn- location-of-head-if-snake-moved-to [snake direction]
  {:location (s/new-head (assoc snake :direction direction)) :direction direction})

;; tries to reach the closest apple
(defmethod choose-directions :greedy
  [{:keys [direction] :as snake} {:keys [apples]}]
  (let [random-choice (shuffle (concat (acceptable-direction-changes direction)
                                       (vector direction)))]
    (if-let [apple (closest-apple (s/head snake) @apples)]
      (sort-by (comp (partial distance-squared apple)
                     (partial location-of-head-if-snake-moved-to snake))
               random-choice)
      random-choice)))

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
    (letfn [(chop-head [sn] (assoc sn :body (:body (s/tail sn))))]
      (chop-head
       (assoc (s/move (assoc snake :direction :stand))
         :direction direction)))))

;;; test support strategies only
(defmethod choose-directions :stubborn [{:keys [direction]} _] [direction])
(defmethod choose-directions :clockwise [{:keys [direction]} _]
  [:up :right :down :left])
