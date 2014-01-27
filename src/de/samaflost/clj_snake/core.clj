(ns de.samaflost.clj-snake.core
  (:import (java.awt.event ActionListener)
           (javax.swing Timer))
  (:use [de.samaflost.clj-snake apple config level snake ui])
  (:gen-class))

(defn- apple-at-head [snake apples]
  (let [head (first (:body snake))]
    (some #(when (= (:location %) head) %) apples)))

(defn- is-lost? [snake level]
  (let [head (first (:body snake))]
    (or 
     (hits-wall? head level)
     (out-of-bounds? head)
     (hits-tail? head snake))))

(defn is-won? [snake level]
  (and (door-is-open? level top-door)
       (= top-door (first (:body snake)))))

(defn snake-is-out? [snake door]
  (not (hits-tail? door snake)))

(defn close-doors [level]
  (dosync
   (alter level open-close top-door :closed)
   (alter level open-close bottom-door :closed)))

(defn- one-turn [snake apples level]
  (dosync
   (let [eaten-apple (apple-at-head @snake @apples)]
     (when eaten-apple
       (alter snake consume eaten-apple)
       (alter apples remove-apple eaten-apple))
     (alter apples age level)
     (alter snake move))))

(defn open-exit [level]
  (dosync
   (alter level open-close top-door :open)))

(defn close-exit [level apples]
  (dosync
   (alter level open-close top-door :closed)
   (alter apples re-initialize level)))

(defn start-over [snake apples level close-exit-timer]
  (.stop close-exit-timer)
  (dosync
   (ref-set snake (new-snake true))
   (ref-set level (create-level))
   (ref-set apples (initial-apples level))))

(defn restart-or-exit [restart? snake apples level close-exit-timer]
  (if (restart?) (start-over snake apples level close-exit-timer)
      (System/exit 0)))

(defn- create-board []
  (let [snake (ref (new-snake true))
        level (ref (create-level))
        apples (ref (initial-apples level))
        time-to-escape (ref ms-to-escape)
        ui (create-ui level snake apples)
        close-exit-timer (doto
                             (Timer. ms-to-escape
                                     (proxy [ActionListener] []
                                       (actionPerformed [event]
                                         (close-exit level apples))))
                           (.setRepeats false))
        r-o-e #(restart-or-exit % snake apples level close-exit-timer)
        turn-timer (Timer. ms-per-turn
                           (proxy [ActionListener] []
                             (actionPerformed [event]
                               (one-turn snake apples level)
                               (when (and (door-is-open? @level bottom-door)
                                          (snake-is-out? @snake bottom-door))
                                 (close-doors level))
                               (when (not (or (seq @apples)
                                              (door-is-open? @level top-door)
                                              (.isRunning close-exit-timer)))
                                 (open-exit level)
                                 (.restart close-exit-timer))
                               (cond (is-won? @snake @level) (r-o-e (:won ui))
                                     (is-lost? @snake @level) (r-o-e (:lost ui))
                                     :else ((:repaint ui))))))]
    (.start turn-timer)))

(defn -main
  [& args]
  (create-board))
