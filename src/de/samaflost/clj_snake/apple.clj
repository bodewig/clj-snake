(ns de.samaflost.clj-snake.apple
  (:import (java.awt Color))
  (:use [de.samaflost.clj-snake.config :only [board-size number-of-apples]]))

(def initial-nutrition 500)

(defn- random-apple []
  {
   :location [(rand-int (:width board-size))
              (rand-int (:height board-size))]
   :color Color/RED
   :remaining-nutrition initial-nutrition
   :type :apple})

(defn- age-apple [apple]
  (let [new-nutrition (dec (:remaining-nutrition apple))]
    (assoc apple
      :remaining-nutrition new-nutrition
      :color (if (> new-nutrition (/ initial-nutrition 2))
               Color/RED Color/YELLOW))))

(defn initial-apples []
  ^{:doc "creates the initial set of apples"}
  (vec (repeatedly number-of-apples random-apple)))

(defn remove-apple [apples eaten]
  (vec (remove #(= % eaten) apples)))

(defn age [apples]
  ^{:doc "rots all apples a bit"}
  (vec (filter #(> (:remaining-nutrition %) 0)
               (map age-apple apples))))
