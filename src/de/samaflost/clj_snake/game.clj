(ns de.samaflost.clj-snake.game
  (:import (java.awt.event ActionListener)
           (javax.swing Timer))
  (:use [de.samaflost.clj-snake apple collision-detection config level snake ui])
  (:gen-class))

;;; Holds all game state and functions pertinent to the game's flow

(defn state-for-new-level [game-state level]
  (dosync
   (ref-set (:player game-state) (new-snake true))
   (ref-set (:level game-state) level)
   (ref-set (:apples game-state) (initial-apples level))
   (ref-set (:time-left-to-escape game-state) ms-to-escape)
   (ref-set (:mode game-state) :eating))
  game-state)

(defn create-game-state []
  (let [state {:player (ref [])
               :level (ref [])
               :apples (ref [])
               :time-left-to-escape (ref 0)
               :score (ref 0)
               :mode (ref :eating)}]
    (state-for-new-level state (create-level))))

(defn- apple-at-head [snake apples]
  (let [snake-head (head snake)]
    (some #(when (collide? snake-head %) %) apples)))

(defn- is-lost? [snake level]
  (let [snake-head (head snake)]
    (some #(collide? snake-head %) [level (tail snake)])))

(defn is-won? [snake level]
  (and (door-is-open? level top-door)
       (= top-door (first (:body snake)))))

(defn snake-is-out? [snake door]
  (not (hits-tail? door snake)))

(defn close-doors [level]
  (dosync
   (alter level open-close top-door :closed)
   (alter level open-close bottom-door :closed)))

(defn close-exit [level apples mode]
  (dosync
   (alter level open-close top-door :closed)
   (alter apples re-initialize level)
   (ref-set mode :eating)))

(defn open-exit [level mode]
  (dosync
   (alter level open-close top-door :open)
   (ref-set mode :escaping)))

(defn eating-only-turn-actions [{:keys [player apples level score mode]}]
  (alter apples age level)
  (let [eaten-apple (apple-at-head @player @apples)]
    (when eaten-apple
      (alter player consume eaten-apple)
      (alter score +' (:remaining-nutrition eaten-apple))
      (when-not (seq (alter apples remove-apple eaten-apple))
        (open-exit level mode)))))

(defn escaping-only-turn-actions [{:keys [time-left-to-escape apples level mode]}]
  (when (<= (alter time-left-to-escape - ms-per-turn) 0)
    (close-exit level apples mode)
    (ref-set time-left-to-escape ms-to-escape)))

(defn- one-turn [state]
  (dosync
   (if (= (deref (:mode state)) :eating)
     (eating-only-turn-actions state)
     (escaping-only-turn-actions state))
   (alter (:player state) move)))

(defn start-over [state]
  (dosync
   (state-for-new-level state (create-level))
   (ref-set (:score state) 0)))

(defn restart-or-exit
  [restart? state]
  (if (restart?) (start-over state)
      (System/exit 0)))

(defn bonus-remaining-time [score time-left-to-escape]
  (dosync
   (alter score +' @time-left-to-escape)))

(defn- create-board []
  (let [state (create-game-state)
        ui (create-ui state)
        r-o-e #(restart-or-exit % state)
        turn-timer (Timer. ms-per-turn
                           (proxy [ActionListener] []
                             (actionPerformed [event]
                               (one-turn state)
                               (when (and (door-is-open? (deref (:level state)) bottom-door)
                                          (snake-is-out? (deref (:player state)) bottom-door))
                                 (close-doors (:level state)))
                               (cond (is-won? (deref (:player state)) (deref (:level state)))
                                     (do
                                       (bonus-remaining-time (:score state) (:time-left-to-escape state))
                                       (r-o-e (:won ui)))
                                     (is-lost? (deref (:player state)) (deref (:level state))) (r-o-e (:lost ui))
                                     :else ((:repaint ui))))))]
    (.start turn-timer)))

(defn -main
  [& args]
  (create-board))
