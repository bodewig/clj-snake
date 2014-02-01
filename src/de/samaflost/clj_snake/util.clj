(ns de.samaflost.clj-snake.util
  (:require [de.samaflost.clj-snake.config :refer [board-size]]))

(defn distinct-location
  "Returns a lazy sequence of coll with elements with duplicate :location removed."
  [coll]
  (letfn [(wrapper [coll taken]
            (lazy-seq
             (loop [c coll t taken]
               (when-let [s (seq c)]
                 (let [head (first s)]
                   (if (t (:location head))
                     (recur (rest s) t)
                     (cons head
                           (wrapper (rest s) (conj t (:location head))))))))))]
    (wrapper coll #{})))

(defn randomly-located-thing
  "Returns a map with a random :location inside the bounds of the level"
  []
  {:location [(inc (rand-int (- (:width board-size) 2)))
              (inc (rand-int (- (:height board-size) 2)))]})

(defn next-location
  "Returns location of moving from one location into a given direction
   either of which is given as a vector."
  [location direction]
  (vec (apply map + [location direction])))
