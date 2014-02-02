(ns de.samaflost.clj-snake.ai
  (:require [de.samaflost.clj-snake.snake :as s]
            [de.samaflost.clj-snake.util :as u]))

;;; AI controlled snake movement

(defmulti choose-directions
  "Changing direction of the AI controlled snake (the first arg).
   Returns a seq of possible directions with decreasing preference."
  (fn [snake game-state] (:strategy snake)))

;; 25% chance of changing directions
(defmethod choose-directions :random [{:keys [direction]} _]
  (shuffle (concat (filter (partial s/is-perpendicular? direction)
                           (remove #{:stand} (keys s/dirs)))
                   [direction direction direction direction])))

(defn pick-direction
  "Pick the prefered direction who's road is clear"
  [snake {:keys [level player balls] :as game-state}]
  (let [try-direction #(assoc snake :direction %)
        places-taken [@level @player @balls (s/tail snake)]]
    (some #(when (u/no-collisions places-taken %) (:direction %))
          (map (comp #(assoc % :location (s/new-head %)) try-direction)
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
