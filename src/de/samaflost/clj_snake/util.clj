(ns de.samaflost.clj-snake.util
  (:require [de.samaflost.clj-snake.collision-detection :refer :all]
            [taoensso.tower :as tower]
            [de.samaflost.clj-snake.config :refer [snake-configuration]]))

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
  (letfn [(rand-inside [dimension]
            (-> (get-in @snake-configuration [:board-size dimension])
                (- 2) (rand-int) (inc)))]
    {:location [(rand-inside :width) (rand-inside :height)]}))

(defn next-location
  "Returns location of moving from one location into a given direction
   either of which is given as a vector."
  [location direction]
  (vec (map + location direction)))

(defn no-collisions
  "Returns item if item doesn't collide with places-taken or nil."
  [places-taken item]
  (when-not (collide? item places-taken) item))

(def ^:private tower-config
  {:dev-mode false, :fallback-locale :en :dictionary "texts.clj"})

(def ^{:doc "wrapper around tower's t that uses the current JVM locale"}
  t (partial tower/t :jvm-default tower-config))
