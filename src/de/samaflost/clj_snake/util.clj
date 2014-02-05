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
  {:location [(inc (rand-int (- (get-in @snake-configuration [:board-size :width]) 2)))
              (inc (rand-int
                    (- (get-in @snake-configuration [:board-size :height]) 2)))]})

(defn next-location
  "Returns location of moving from one location into a given direction
   either of which is given as a vector."
  [location direction]
  (vec (apply map + [location direction])))

(defn no-collisions
  "Returns item if item doesn't collide with places-taken or nil."
  [places-taken item]
  (when-not (collide? item places-taken) item))

(def ^:private tower-config
  {:dev-mode false, :fallback-locale :en :dictionary "texts.clj"})

(def ^:private locale-key
  ;; work-around for tower issue 37 which is supposed to be fixed in 2.0.2
  ;; remove once 2.0.2 is available in clojars

  ;; use the fact there are only translations for two-letter codes in this game
  (keyword (subs (name (tower/locale-key :jvm-default)) 0 2)))

(def ^{:doc "wrapper around tower's t that uses the current JVM locale"}
  t (partial tower/t locale-key tower-config))
