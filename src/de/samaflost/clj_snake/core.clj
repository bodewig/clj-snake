(ns de.samaflost.clj-snake.core
  (:import (javax.swing JOptionPane)
           (java.awt.event ActionListener))
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

(defn snake-is-out? [snake door]
  (not (hits-tail? door snake)))

(defn close-doors [level]
  (dosync
   (alter level open-close top-door :closed)
   (alter level open-close bottom-door :closed)))

(defn- one-turn [snake apples]
  (dosync
   (let [eaten-apple (apple-at-head @snake @apples)]
     (when eaten-apple
       (alter snake consume eaten-apple)
       (alter apples remove-apple eaten-apple))
     (alter apples age)
     (alter snake move))))


(defn- create-board []
  (let [snake (ref (new-snake true))
        level (ref (create-level))
        apples (ref (initial-apples level))
        turn-action (fn [frame panel]
                      (proxy [ActionListener] []
                        (actionPerformed [event]
                          (one-turn snake apples)
                          (when (and (door-is-open? @level bottom-door)
                                     (snake-is-out? @snake bottom-door))
                            (close-doors level))
                          (if (is-lost? @snake @level)
                            (JOptionPane/showMessageDialog frame "Game Over!")
                            (.repaint panel)))))]
    (create-ui turn-action level snake apples)))

(defn -main
  [& args]
  (create-board))
