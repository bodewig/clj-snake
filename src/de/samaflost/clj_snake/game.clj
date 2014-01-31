(ns de.samaflost.clj-snake.game
  (:import (java.util.concurrent Executors TimeUnit))
  (:use [de.samaflost.clj-snake apple collision-detection config level snake ui])
  (:gen-class))

;;; Holds all game state and functions pertinent to the game's flow

(def scheduler
  ^:private
  (Executors/newScheduledThreadPool 1))

(defn- schedule-closing-doors [{:keys [level player count-down]}]
  (letfn [(close-doors []
            (dosync
             (doseq [d [top-door bottom-door]]
               (alter level open-close d :closed))))]
    (.schedule scheduler close-doors
               (+ (* ms-per-turn (inc (:to-grow @player))) @count-down)
               TimeUnit/MILLISECONDS)))

(defn state-for-new-level
  "Set up the state for starting in a new level - may be the first one"
  [game-state level]
  (dosync
   (ref-set (:player game-state) (new-snake true))
   (ref-set (:level game-state) level)
   (ref-set (:apples game-state) (initial-apples [level]))
   (ref-set (:time-left-to-escape game-state) ms-to-escape)
   (ref-set (:mode game-state) :starting)
   (ref-set (:count-down game-state) 3000))
  game-state)

(defn- create-game-state []
  (let [state {:player (ref [])
               :level (ref [])
               :apples (ref [])
               :time-left-to-escape (ref 0)
               :score (ref 0)
               :count-down (ref 0)
               :mode (ref :starting)}]
    (state-for-new-level state (create-level))))

(defn- item-colliding-with-snake-head [snake items]
  (let [snake-head (head snake)]
    (first (filter (partial collide? snake-head) items))))

(defn- apple-at-head [snake apples]
  (item-colliding-with-snake-head snake apples))

(defn- is-lost? [{:keys [player level]}]
  (item-colliding-with-snake-head @player [@level (tail @player)]))

(defn- is-won? [{:keys [player level mode]}]
  (and
   (= :escaping @mode)
   (door-is-open? @level top-door)
   (= top-door (:location (head @player)))))

(defn eval-won-or-lost
  "Returns :won or :lost when appropriate or nil if neither is true
   right now"
  [state]
  (if (is-won? state) :won
      (when (is-lost? state) :lost)))

(defn- move-and-eval-game [state]
  (when (#{:eating :escaping} (deref (:mode state)))
    (alter (:player state) move)
    (when-let [new-state (eval-won-or-lost state)]
      (ref-set (:mode state) new-state))))

(defn- eat [player apple score]
  (alter player consume apple)
  (alter score +' (:remaining-nutrition apple)))

(defn- enter-escaping-mode [level mode]
  (ref-set mode :escaping)
  (alter level open-close top-door :open))

(defn eating-only-turn-actions
  "stuff done in a turn that starts by the player trying to eat apples.
   Must be called from within a transaction."
  [{:keys [player apples level score mode]}]
  (alter apples age [@level @player])
  (when-let [eaten-apple (apple-at-head @player @apples)]
    (eat player eaten-apple score)
    (when-not (seq (alter apples remove-apple eaten-apple))
      (enter-escaping-mode level mode))))

(defn- re-enter-eating-mode [{:keys [player level apples mode time-left-to-escape]}]
  (alter level open-close top-door :closed)
  (alter apples re-initialize [@level @player])
  (ref-set mode :eating)
  (ref-set time-left-to-escape ms-to-escape))

(defn escaping-only-turn-actions
  "stuff done in a turn that starts by the player trying to leave the level.
   Must be called from within a transaction."
  [state]
  (let [time-left-to-escape (:time-left-to-escape state)]
    (when (<= (alter time-left-to-escape - ms-per-turn) 0)
      (re-enter-eating-mode state))))

(defn won-actions [{:keys [score time-left-to-escape]}]
  "stuff done in a turn if the player has escaped the level"
  (alter score +' @time-left-to-escape))

(defn starting-actions
  "stuff done before the level actually starts.
   Must be called from within a transaction."
  [{:keys [mode count-down]}]
  (when (neg? (alter count-down - ms-per-turn))
    (ref-set mode :eating)))

(defn- one-turn [state]
  (dosync
   (move-and-eval-game state)
   (case (deref (:mode state))
     :eating (eating-only-turn-actions state)
     :escaping (escaping-only-turn-actions state)
     :won (won-actions state)
     :starting (starting-actions state)
     nil)))

(defn- start-over [state]
  (dosync
   (state-for-new-level state (create-level))
   (ref-set (:score state) 0))
  (schedule-closing-doors state))

(defn- create-board []
  (let [state (create-game-state)
        ui (create-ui state #(start-over state))]
    (.scheduleAtFixedRate scheduler #(one-turn state)
                          ms-per-turn ms-per-turn
                          TimeUnit/MILLISECONDS)
    (schedule-closing-doors state)))

(defn -main
  [& args]
  (create-board))
