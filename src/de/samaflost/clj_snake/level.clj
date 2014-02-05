(ns de.samaflost.clj-snake.level
  (:require [de.samaflost.clj-snake.config :refer [snake-configuration]]))

;;; level holds the walls and doors

(defn- door [row]
  [(/ (get-in @snake-configuration [:board-size :width]) 2) row])

(def top-door
  ^{:doc "the door at the top, the program-controlled snake enters
  here and the snake will use it to escape once the level has been
  clerred of apples"}
  (door 0))

(def bottom-door
  ^{:doc "the door at the bottom, the player-controlled snake enters
  here."}
  (door (dec (get-in @snake-configuration [:board-size :height]))))

(defn- framing []
  (let [max-height (get-in @snake-configuration [:board-size :height])
        max-width (get-in @snake-configuration [:board-size :width])]
    (remove #{bottom-door top-door}
            (concat (map (partial vector 0) (range max-height))
                    (map (partial vector (dec max-width)) (range max-height))
                    (map #(vector % 0) (range max-width))
                    (map #(vector % (dec max-height)) (range max-width))))))

(defn- initial-walls []
    (set
     (concat (framing)
             ;; a single extra-wall to make things more interesting
             (list [(/ (get-in @snake-configuration [:board-size :width]) 2)
                    (/ (get-in @snake-configuration [:board-size :height]) 2)]))))

(defn- key-for-door [door]
  (if (= bottom-door door) :bottom-door :top-door))

(defn- create-level [number]
  {:walls (initial-walls)
   :top-door :closed
   :bottom-door :open
   :number number
   :balls (inc number)
   :type :level})

(defn create-initial-level
  "Create the walls of the first level and open doors"
  []
  (create-level 0))

(defn door-is-open?
  "is the given door open in the given level?"
  [level door]
  (= :open ((key-for-door door) level)))

(defn open-close
  "opens or closes a door"
  [level door new-state]
  (assoc level (key-for-door door) new-state))

(defn next-level
  "Returns the level after the given one"
  [previous-level]
  (create-level (inc (:number previous-level))))
