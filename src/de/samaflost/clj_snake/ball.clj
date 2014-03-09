(ns de.samaflost.clj-snake.ball
  (:import (java.awt Color))
  (:require [de.samaflost.clj-snake.collision-detection :refer :all]
            [de.samaflost.clj-snake.util :as u]))

;;; ball(s) creation and movement

(def ^:private dirs [[1 1] [-1 1] [-1 -1] [1 -1]])

(defn- diag-dirs [dir]
  (let [direction (dirs dir)]
    [[(first direction) 0] [0 (second direction)]]))

(defn- running-into-diagonal [{:keys [ location direction]} places-taken]
  (and
   ; not stuck
   (not (collide?
         {:location (u/next-location location (dirs (mod (+ direction 2) 4)))}
         places-taken))
   ; hits diagonal
   (every? #(collide?
             {:location (u/next-location location %)}
             places-taken)
           (diag-dirs direction))))

(defn- random-ball []
  (assoc (u/randomly-located-thing)
    :color Color/BLACK
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
  (letfn [(move-into-direction [new-dir]
            (assoc ball
              :location (u/next-location location (dirs new-dir))
              :direction new-dir))]
    (if (running-into-diagonal ball places-taken)
      (move-into-direction (mod (+ direction 2) 4))
      (some (partial u/no-collisions places-taken)
            (concat
             (map move-into-direction
                  [direction
                   (mod (inc direction) 4)
                   (mod (dec direction) 4)
                   (mod (+ direction 2) 4)])
             (vector ball))))))

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
