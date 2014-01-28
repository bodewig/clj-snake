(ns de.samaflost.clj-snake.apple
  (:import (java.awt Color))
  (:use [de.samaflost.clj-snake.config :only [board-size number-of-apples]]
        [de.samaflost.clj-snake.level]))

(def initial-nutrition 500)

(defn- random-apple []
  (let [min-init-nutrition (int (/ initial-nutrition 3))]
    {
     :location [(inc (rand-int (- (:width board-size) 2)))
                (inc (rand-int (- (:height board-size) 2)))]
     :color Color/RED
     :remaining-nutrition (+ (rand-int (- initial-nutrition min-init-nutrition))
                             min-init-nutrition)
     :type :apple}))

(defn- age-apple [apple]
  (let [new-nutrition (dec (:remaining-nutrition apple))]
    (assoc apple
      :remaining-nutrition new-nutrition
      :color (if (> new-nutrition (/ initial-nutrition 2))
               Color/RED Color/YELLOW))))

(defn initial-apples [level]
  ^{:doc "creates the initial set of apples"}
  (vec (take number-of-apples
             (distinct
              (remove #(hits-wall? % level)
                      (repeatedly random-apple))))))

(defn remove-apple [apples eaten]
  (vec (remove #(= % eaten) apples)))

(defn age [apples level]
  ^{:doc "rots all apples a bit"}
  (if (seq apples)
    (let [remaining (vec (filter #(> (:remaining-nutrition %) 0)
                                 (map age-apple apples)))]
      (if (seq remaining) remaining (initial-apples level)))
    []))

(defn re-initialize [apples level]
  (initial-apples level))
