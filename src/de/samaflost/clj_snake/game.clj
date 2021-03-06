(ns de.samaflost.clj-snake.game
  (:import (java.util.concurrent Executors TimeUnit))
  (:use [de.samaflost.clj-snake ai apple ball collision-detection config
         highscore level snake ui])
  (:gen-class))

;;; Holds all game state and functions pertinent to the game's flow

(def scheduler
  ^:private
  (Executors/newScheduledThreadPool 1))

(defn- schedule-closing-doors [{:keys [level player count-down]}]
  (letfn [(close-doors []
            (dosync
             (alter level open-close-all-doors :closed)))]
    (.schedule scheduler close-doors
               (+ (* ms-per-turn (inc (:to-grow @player))) @count-down)
               TimeUnit/MILLISECONDS)))

(defn state-for-new-level
  "Set up the state for starting in a new level - may be the first one"
  [game-state level]
  (let [apples (initial-apples [level])]
    (dosync
     (ref-set (:player game-state) (new-snake true))
     (ref-set (:ai game-state) (new-snake false))
     (ref-set (:level game-state) level)
     (ref-set (:apples game-state) apples)
     (ref-set (:balls game-state) (create-balls (:balls level) [level apples]))
     (ref-set (:time-left-to-escape game-state) ms-to-escape)
     (ref-set (:mode game-state) :starting)
     (ref-set (:count-down game-state) 3000))
    game-state))

(defn- create-game-state []
  (let [state {:player (ref [])
               :ai (ref [])
               :level (ref [])
               :apples (ref [])
               :balls (ref [])
               :time-left-to-escape (ref 0)
               :score (ref 0)
               :count-down (ref 0)
               :mode (ref :starting)
               :lifes-left (ref initial-extra-lifes)
               :next-extra-life-at (ref grant-extra-life-at)}]
    (assoc (state-for-new-level state (create-initial-level))
      :mode (ref :initial))))

(defn- item-colliding-with-snake-head [snake items]
  (let [snake-head (head snake)]
    (first (filter (partial collide? snake-head) items))))

(defn- apple-at-head [snake apples]
  (item-colliding-with-snake-head snake apples))

(defn- is-lost? [{:keys [player level balls ai]}]
  (item-colliding-with-snake-head @player [@level (tail @player) @balls @ai]))

(defn- is-won? [{:keys [player level mode]}]
  (and
   (= :escaping @mode)
   (door-is-open? @level top-door)
   (= top-door (:location (head @player)))))

(defn eval-won-or-lost
  "Returns :won or :lost when appropriate or nil if neither is true
   right now"
  [{:keys [lifes-left] :as state}]
  (if (is-won? state) :won
      (when (is-lost? state)
        (if (pos? @lifes-left) :re-starting :lost))))

(defn- shrunk-to-head [snake]
  (and (get @snake :stuck)
       (= 1 (count (:body @snake)))))

(defn- add-to-score [{:keys [score next-extra-life-at lifes-left]} to-add]
  (when (> (alter score +' to-add) @next-extra-life-at)
    (alter next-extra-life-at +' grant-extra-life-at)
    (alter lifes-left inc)))

(defn move-ai [{:keys [ai apples score] :as state}]
  (alter ai walk state)
  (when-let [eaten-apple (apple-at-head @ai @apples)]
    (alter ai consume eaten-apple)
    (alter apples remove-apple eaten-apple))
  (when (shrunk-to-head ai)
    (add-to-score state 5000)))

(defn- move-and-eval-game [{:keys [mode player balls level ai] :as state}]
  (when (#{:eating :escaping :leaving} @mode)
    (move-ai state)
    (alter balls bounce-all [@player @level @ai])
    (alter player (if (= :leaving @mode) fake-leaving move))
    (when-let [new-state (eval-won-or-lost state)]
      (ref-set mode new-state))))

(defn- eat [apple {:keys [player] :as state}]
  (alter player consume apple)
  (add-to-score state (:remaining-nutrition apple)))

(defn- enter-escaping-mode [level mode]
  (ref-set mode :escaping)
  (alter level open-close top-door :open))

(defn eating-only-turn-actions
  "stuff done in a turn that starts by the player trying to eat apples.
   Must be called from within a transaction."
  [{:keys [player apples level mode] :as state}]
  (alter apples age [@level @player])
  (when-let [eaten-apple (apple-at-head @player @apples)]
    (eat eaten-apple state)
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
  [{:keys [time-left-to-escape] :as state}]
  (when (<= (alter time-left-to-escape - ms-per-turn) 0)
    (re-enter-eating-mode state)))

(defn leaving-actions
  "Make it look as if the snake was leaving through the top door.
   Must be called from within a transaction."
  [{:keys [player level] :as state}]
  (move-and-eval-game state)
  (when-not (seq (:body @player))
    (state-for-new-level state (next-level @level))
    (schedule-closing-doors state)))

(defn won-actions
  "stuff done in a turn if the player has escaped the level.
   Must be called from within a transaction."
  [{:keys [score time-left-to-escape mode] :as state}]
  (add-to-score state @time-left-to-escape)
  (ref-set mode :leaving)
  (leaving-actions state))

(defn starting-actions
  "stuff done before the level actually starts.
   Must be called from within a transaction."
  [{:keys [mode count-down]}]
  (when (neg? (alter count-down - ms-per-turn))
    (ref-set mode :eating)))

(defn re-starting-actions
  "stuff done when the user has been killed but lifes left.
   Must be called from within a transaction."
  [{:keys [level lifes-left] :as state}]
  (alter lifes-left dec)
  (state-for-new-level state (alter level open-close-all-doors :open))
  (schedule-closing-doors state))

(defn lost-actions
  "stuff done when the game is lost.
   Not to be called from within a transaction."
  [{:keys [mode score time-left-to-escape]} lost-callback]
  (lost-callback
   (add-score @score (.. System (getProperties) (get "user.name"))))
  (dosync
   (ref-set mode :initial)
   (ref-set time-left-to-escape ms-to-escape)))

(defn- one-turn [{:keys [mode] :as state} lost-callback]
  (dosync
   (move-and-eval-game state)
   (case @mode
     :eating (eating-only-turn-actions state)
     :escaping (escaping-only-turn-actions state)
     :won (won-actions state)
     :re-starting (re-starting-actions state)
     :starting (starting-actions state)
     :leaving (leaving-actions state)
     nil))
  ;; outside of the dosync as lost-callback may be blocking
  (when (= @mode :lost) (lost-actions state lost-callback)))

(defn start-over
  "Re-sets the game to the initial state"
  [{:keys [score lifes-left next-extra-life-at] :as state}]
  (dosync
   (state-for-new-level state (create-initial-level))
   (ref-set score 0)
   (ref-set lifes-left initial-extra-lifes)
   (ref-set next-extra-life-at grant-extra-life-at))
  (schedule-closing-doors state))

(defn- create-board []
  (let [state (create-game-state)
        lost-callback (create-ui state #(start-over state))]
    (.scheduleAtFixedRate scheduler #(one-turn state lost-callback)
                          ms-per-turn ms-per-turn
                          TimeUnit/MILLISECONDS)))

(defn -main
  [& args]
  (create-board))
