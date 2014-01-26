(ns de.samaflost.clj-snake.apple
  (:import (java.awt Color))
  (:use [de.samaflost.clj-snake.config :only [board-size number-of-apples]]))

(defn- random-apple []
  {
   :location [(rand-int (:width board-size))
              (rand-int (:height board-size))]
   :color Color/RED
   :type :apple})

(defn initial-apples []
  ^{:doc "creates the initial set of apples"}
  (vec (repeatedly number-of-apples random-apple)))

