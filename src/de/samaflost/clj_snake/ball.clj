(ns de.samaflost.clj-snake.ball
  (:import (java.awt Color))
  (:require [de.samaflost.clj-snake.collision-detection :refer :all]
            [de.samaflost.clj-snake.util :as u]))

(def ^:private dirs [[1 1] [-1 1] [-1 -1] [1 -1]])

(defn- random-ball []
  (assoc (u/randomly-located-thing)
    :color Color/ORANGE
    :direction (dirs (rand-int 4))
    :type :ball))

(defn create-balls
  "creates the initial set of balls"
  [num-balls places-taken]
  (letfn [(place-is-taken? [b]
            (some (partial collide? b) places-taken))]
    (vec (take num-balls
               (remove place-is-taken?
                       (u/distinct-location (repeatedly random-ball)))))))

