(ns de.samaflost.clj-snake.level
  (:use [de.samaflost.clj-snake.config :only [board-size]]))

(defn door [row]
  [(/ (:width board-size) 2) row])

(def top-door
  (door 0))

(def bottom-door
  (door (dec (:height board-size))))

(defn framing []
  (let [max-height (:height board-size)
        max-width (:width board-size)]
    (remove #(or (= bottom-door %) (= top-door %))
            (concat (map #(vector 0 %) (range max-height))
                    (map #(vector (dec max-width) %) (range max-height))
                    (map #(vector % 0) (range max-width))
                    (map #(vector % (dec max-height)) (range max-width))))))

(defn initial-walls []
    (set
     (concat (framing)
             ;; a single extra-wall to make things more interesting
             (list [(/ (:width board-size) 2) (/ (:height board-size) 2)]))))

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
