(ns de.samaflost.clj-snake.level
  (:use [de.samaflost.clj-snake.config :only [board-size]]))

(defn door [row]
  [(/ (:width board-size) 2) row])

(def top-door
  (door 0))

(def bottom-door
  (door (dec (:height board-size))))

(defn remove-doors [walls]
  (let [middle (/ (:width board-size) 2)]
    (remove #(= bottom-door %)
            (remove #(= top-door %) walls))))

(defn initial-walls []
  (let [max-height (dec (:height board-size))
        max-width (dec (:width board-size))]
    (set
     (remove-doors
      (concat (map #(vector 0 %) (range (:height board-size)))
              (map #(vector max-width %) (range (:height board-size)))
              (map #(vector % 0) (range (:width board-size)))
              (map #(vector % max-height) (range (:width board-size)))
              ;; a single extra-wall to make things more interesting
              (list [(/ (:width board-size) 2) (/ (:height board-size) 2)]))))))

(defn create-level []
  {:walls (initial-walls)
   :top-door :closed
   :bottom-door :open
   :type :level})

(defn hits-wall? [snake-head level]
  (or
   (contains? (:walls level) snake-head)
   (and (= :top-door :closed) (= top-door snake-head))
   (and (= :bottom-door :closed) (= bottom-door snake-head))))
