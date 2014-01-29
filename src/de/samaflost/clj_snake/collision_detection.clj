(ns de.samaflost.clj-snake.collision-detection
  (:use [de.samaflost.clj-snake.level
         :only [bottom-door door-is-open? top-door]]))

(defmulti collide? (fn [probe target] [(:type probe) (:type target)]))

(defmethod collide? [:snake-head :apple] [snake-head apple]
  (= (:location snake-head) (:location apple)))

(defmethod collide? [:snake-head :snake-body] [snake-head snake-body]
  (contains? (set (:body snake-body)) (:location snake-head)))

(defmethod collide? [:snake-head :level] [snake-head level]
  (or
   (contains? (:walls level) (:location snake-head))
   (and (= top-door (:location snake-head))
        (not (door-is-open? level top-door)))
   (and (= bottom-door (:location snake-head))
        (not (door-is-open? level bottom-door)))))

