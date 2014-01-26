(ns de.samaflost.clj-snake.core
  (:import (javax.swing JPanel JFrame Timer)
           (java.awt Dimension)
           (java.awt.event ActionListener KeyListener KeyEvent))
  (:use [de.samaflost.clj-snake apple config snake painting])
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

(defn- create-panel [snake apples]
  (proxy [JPanel] []
    (getPreferredSize []
      (Dimension. (* (:width board-size) pixel-per-point)
                  (* (:height board-size)  pixel-per-point)))
    (paintComponent [g]
      (proxy-super paintComponent g)
      (paint g @snake)
      (doseq [a @apples]
        (paint g a)))))

(defn- create-board []
  (let [snake (ref (new-snake true))
        apples (ref (initial-apples))
        frame (JFrame. "clj-snake")
        panel (doto (create-panel snake apples)
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
                          (one-turn snake)
                          (.repaint panel))))]
    (doto frame
      (.add panel)
      (.pack)
      (.setVisible true))
    (.start turn-timer)))

(defn -main
  [& args]
  (create-board))
