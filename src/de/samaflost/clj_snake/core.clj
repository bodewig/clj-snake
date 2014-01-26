(ns de.samaflost.clj-snake.core
  (:import (javax.swing JPanel JFrame Timer)
           (java.awt Color Dimension)
           (java.awt.event ActionListener KeyListener KeyEvent))
  (:use [de.samaflost.clj-snake.config]
        [de.samaflost.clj-snake.snake])
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

(defn- one-turn [snake]
  (dosync
   (alter snake move)))

(defn- paint [g snake]
  (doseq [pt (:body snake)]
    (.setColor g Color/GREEN)
    (.fillRect g
               (* (first pt) pixel-per-point)
               (* (second pt) pixel-per-point)
               pixel-per-point pixel-per-point)))

(defn- create-panel [snake]
  (proxy [JPanel] []
    (getPreferredSize []
      (Dimension. (* (:width board-size) pixel-per-point)
                  (* (:height board-size)  pixel-per-point)))
    (paintComponent [g]
      (proxy-super paintComponent g)
      (paint g @snake))))

(defn- create-board []
  (let [snake (ref (new-snake true))
        frame (JFrame. "clj-snake")
        panel (doto (create-panel snake)
                (.setFocusable true)
                (.addKeyListener
                 (proxy [KeyListener] []
                   (keyPressed [e]
                     (change-snake-direction snake (.getKeyCode e)))
                   (keyReleased [e])
                   (keyTyped [e]))))
        timer (Timer. ms-per-turn
                      (proxy [ActionListener] []
                        (actionPerformed [event]
                          (one-turn snake)
                          (.repaint panel))))]
    (doto frame
      (.add panel)
      (.pack)
      (.setVisible true))
    (.start timer)))

(defn -main
  [& args]
  (create-board))
