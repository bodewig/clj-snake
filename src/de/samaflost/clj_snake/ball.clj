(ns de.samaflost.clj-snake.ball
  (:import (java.awt Color))
  (:require [de.samaflost.clj-snake.collision-detection :refer :all]
            [de.samaflost.clj-snake.util :as u]))

(def ^:private dirs [[1 1] [-1 1] [-1 -1] [1 -1]])

(defn- random-ball []
  (assoc (u/randomly-located-thing)
    :color Color/ORANGE
    :direction (rand-int 4)
    :type :ball))

(defn create-balls
  "creates the initial set of balls"
  [num-balls places-taken]
  (letfn [(place-is-taken? [b]
            (some (partial collide? b) places-taken))]
    (vec (take num-balls
               (remove place-is-taken?
                       (u/distinct-location (repeatedly random-ball)))))))

(defn bounce [{:keys [location direction] :as ball} places-taken]
  (first
   (remove #(collide? % places-taken)
           (concat
            (map #(assoc ball
                    :location (u/next-location location (dirs %))
                    :direction %)
                 [direction
                  (mod (inc direction) 4)
                  (mod (dec direction) 4)
                  (mod (+ direction 2) 4)])
            (vector ball)))))
