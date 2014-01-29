(ns de.samaflost.clj-snake.collision-detection
  (:use [de.samaflost.clj-snake.level
         :only [bottom-door door-is-open? top-door]]))

(defmulti collide? (fn [probe target]
                     (cond (find target :location) :has-location
                           (find target :body) :has-body
                           :else (:type target))))

(defmethod collide? :has-location [{probe-location :location}
                                   {target-location :location}]
  (= probe-location target-location))

(defn location-in-collection [location collection]
  (contains? (set collection) location))

(defmethod collide? :has-body [{:keys [location]} {:keys [body]}]
  (location-in-collection location body))

(defmethod collide? :level [{:keys [location]} level]
  (letfn [(collides-with-closed-door [door]
            (and (= location door) (not (door-is-open? level door))))]
    (or
     (location-in-collection location (:walls level))
     (some collides-with-closed-door [top-door bottom-door]))))
