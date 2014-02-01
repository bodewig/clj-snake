(ns de.samaflost.clj-snake.apple
  (:import (java.awt Color))
  (:require [de.samaflost.clj-snake.config :refer [number-of-apples]]
            [de.samaflost.clj-snake.collision-detection :refer :all]
            [de.samaflost.clj-snake.util :as u]))

;;; generation and aging of apples

(def initial-nutrition 500)

(defn- random-apple []
  (let [min-init-nutrition (int (/ initial-nutrition 3))]
    (assoc (u/randomly-located-thing)
      :color Color/RED
      :remaining-nutrition (+ (rand-int (- initial-nutrition min-init-nutrition))
                              min-init-nutrition)
      :type :apple)))

(defn- age-apple [apple]
  (let [new-nutrition (dec (:remaining-nutrition apple))]
    (assoc apple
      :remaining-nutrition new-nutrition
      :color (if (> new-nutrition (/ initial-nutrition 2))
               Color/RED Color/YELLOW))))

(defn initial-apples
  "creates the initial set of apples"
  [places-taken]
  (letfn [(place-is-taken? [apple]
            (some (partial collide? apple) places-taken))]
    (vec (take number-of-apples
               (remove place-is-taken?
                       (u/distinct-location (repeatedly random-apple)))))))

(defn remove-apple
  "Removes the given apple"
   [apples eaten]
  (vec (remove #{eaten} apples)))

(defn age
  "rots all apples a bit"
  [apples places-taken]
  (let [remaining (vec (filter #(pos? (:remaining-nutrition %))
                               (map age-apple apples)))]
    (if (seq remaining) remaining (initial-apples places-taken))))


(defn re-initialize
  "Really just redirects to initial-apples ignoring the first arg
   needed for the contract of alter"
  [_ places-taken]
  (initial-apples places-taken))
