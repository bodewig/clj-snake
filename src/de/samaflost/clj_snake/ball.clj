(ns de.samaflost.clj-snake.ball
  (:import (java.awt Color))
  (:require [de.samaflost.clj-snake.collision-detection :refer :all]
            [de.samaflost.clj-snake.util :as u]))

;;; ball(s) creation and movement

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

;; only non-private for tests
(defn bounce [{:keys [location direction] :as ball} places-taken]
  (some #(when-not (collide? % places-taken) %)
        (concat
         (map #(assoc ball
                 :location (u/next-location location (dirs %))
                 :direction %)
              [direction
               (mod (inc direction) 4)
               (mod (dec direction) 4)
               (mod (+ direction 2) 4)])
         (vector ball))))

(defn bounce-all
  "Moves all balls one step taking collisions and reflections into account"
  [balls places-taken]
  (letfn [(wrapper [bs new]
            ((fn [remaining-bs new-bs]
               (if-let [s (seq remaining-bs)]
                 (recur (rest s)
                        (conj new-bs
                              (bounce (first s)
                                      (concat places-taken new-bs (rest s)))))
                 new-bs))
             bs new))]
    (wrapper balls [])))
