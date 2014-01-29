(ns de.samaflost.clj-snake.collision-detection
  (:require [de.samaflost.clj-snake.level
             :refer [bottom-door door-is-open? top-door]]))

;;; General purpose collision detection used for detecting whether the
;;; snake hist a wall, itself, the other snake or the ball, the ball
;;; hist a wall, an apple is generated on top of a snake and so on

(defn- location-in-collection? [location collection]
  (contains? (set collection) location))

(defmulti collide?
  "Tests whether probe and target collide where probe must be a map
   with a :location key - dispatches based on target"
  (fn [probe target]
    (cond (find target :location) :has-location
          (find target :body) :has-body
          :else (:type target))))

(defmethod collide? :has-location [{probe-location :location}
                                   {target-location :location}]
  (= probe-location target-location))

(defmethod collide? :has-body [{:keys [location]} {:keys [body]}]
  (location-in-collection? location body))

(defmethod collide? :level [{:keys [location]} level]
  (letfn [(collides-with-closed-door [door]
            (and (= location door) (not (door-is-open? level door))))]
    (or
     (location-in-collection? location (:walls level))
     (some collides-with-closed-door [top-door bottom-door]))))
