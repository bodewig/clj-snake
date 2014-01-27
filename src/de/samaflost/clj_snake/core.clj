(ns de.samaflost.clj-snake.core
  (:import (javax.swing JPanel JFrame Timer JOptionPane)
           (java.awt Dimension)
           (java.awt.event ActionListener KeyListener KeyEvent))
  (:use [de.samaflost.clj-snake apple config level snake painting])
  (:gen-class))


(def key-code-to-direction
  ^{:private true}
  {
   KeyEvent/VK_LEFT :left
   KeyEvent/VK_RIGHT :right
   KeyEvent/VK_UP :up
   KeyEvent/VK_DOWN :down
   })

(defn- change-snake-direction [snake key-code]
  (let [new-dir (get key-code-to-direction key-code)]
    (when new-dir (dosync (alter snake change-direction new-dir)))))

(defn- apple-at-head [snake apples]
  (let [head (first (:body @snake))]
    (some #(when (= (:location %) head) %) @apples)))

(defn- is-lost? [snake level]
  (let [head (first (:body @snake))]
    (or 
     (hits-wall? head @level)
     (out-of-bounds? head)
     (hits-tail? head @snake))))

(defn snake-is-out? [snake door]
  (not (hits-tail? door @snake)))

(defn close-doors [level]
  (dosync
   (alter level open-close top-door :closed)
   (alter level open-close bottom-door :closed)))

(defn- one-turn [snake apples]
  (dosync
   (let [eaten-apple (apple-at-head snake apples)]
     (when eaten-apple
       (alter snake consume eaten-apple)
       (alter apples remove-apple eaten-apple))
     (alter apples age)
     (alter snake move))))

(defn- create-panel [level snake apples]
  (proxy [JPanel] []
    (getPreferredSize []
      (Dimension. (* (:width board-size) pixel-per-point)
                  (* (:height board-size)  pixel-per-point)))
    (paintComponent [g]
      (proxy-super paintComponent g)
      (paint g @level)
      (paint g @snake)
      (doseq [a @apples]
        (paint g a)))))

(defn- create-board []
  (let [snake (ref (new-snake true))
        level (ref (create-level))
        apples (ref (initial-apples level))
        frame (JFrame. "clj-snake")
        panel (doto (create-panel level snake apples)
                (.setFocusable true)
                (.addKeyListener
                 (proxy [KeyListener] []
                   (keyPressed [e]
                     (change-snake-direction snake (.getKeyCode e)))
                   (keyReleased [e])
                   (keyTyped [e]))))
        turn-timer (Timer. ms-per-turn
                      (proxy [ActionListener] []
                        (actionPerformed [event]
                          (one-turn snake apples)
                          (when (and (door-is-open? @level bottom-door)
                                     (snake-is-out? snake bottom-door))
                            (close-doors level))
                          (if (is-lost? snake level)
                            (JOptionPane/showMessageDialog frame "Game Over!")
                            (.repaint panel)))))]
    (doto frame
      (.add panel)
      (.pack)
      (.setVisible true))
    (.start turn-timer)))

(defn -main
  [& args]
  (create-board))
