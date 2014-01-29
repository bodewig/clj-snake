(ns de.samaflost.clj-snake.level
  (:use [de.samaflost.clj-snake.config :only [board-size]]))

;;; level holds the walls and doors

(defn- door [row]
  [(/ (:width board-size) 2) row])

(def top-door
  ^{:doc "the door at the top, the program-controlled snake enters
  here and the snake will use it to escape once the level has been
  clerred of apples"}
  (door 0))

(def bottom-door
  ^{:doc "the door at the bottom, the player-controlled snake enters
  here."}
  (door (dec (:height board-size))))

(defn- framing []
  (let [max-height (:height board-size)
        max-width (:width board-size)]
    (remove #{bottom-door top-door}
            (concat (map (partial vector 0) (range max-height))
                    (map (partial vector (dec max-width)) (range max-height))
                    (map #(vector % 0) (range max-width))
                    (map #(vector % (dec max-height)) (range max-width))))))

(defn- initial-walls []
    (set
     (concat (framing)
             ;; a single extra-wall to make things more interesting
             (list [(/ (:width board-size) 2) (/ (:height board-size) 2)]))))

(defn- key-for-door [door]
  (if (= bottom-door door) :bottom-door :top-door))

(defn create-level []
  "Create the walls of the level and open doors"
  {:walls (initial-walls)
   :top-door :closed
   :bottom-door :open
   :type :level})

(defn door-is-open? [level door]
  "is the given door open in the given level?"
  (= :open ((key-for-door door) level)))

(defn open-close [level door new-state]
  "opens or closes a door"
  (assoc level (key-for-door door) new-state))
