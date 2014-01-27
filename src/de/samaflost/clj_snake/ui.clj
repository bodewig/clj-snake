(ns de.samaflost.clj-snake.ui
  (:import (javax.swing JPanel JFrame JOptionPane)
           (java.awt Color Dimension)
           (java.awt.event KeyListener KeyEvent))
  (:use [de.samaflost.clj-snake.config :only [board-size ms-per-turn pixel-per-point]]
        [de.samaflost.clj-snake.level :only [bottom-door top-door]]
        [de.samaflost.clj-snake.snake :only [change-direction]]))

(defmulti paint (fn [g item] (:type item)))

(defmethod paint :snake [g snake]
  ^{:doc "paint the snake"}
  (doseq [pt (:body snake)]
    (.setColor g (:color snake))
    (.fillRect g
               (* (first pt) pixel-per-point)
               (* (second pt) pixel-per-point)
               pixel-per-point pixel-per-point)))

(defmethod paint :apple [g apple]
  ^{:doc "paint the apple"}
  (let [loc (:location apple)]
    (.setColor g (:color apple))
    (.fillOval g
               (* (first loc) pixel-per-point)
               (* (second loc) pixel-per-point)
               pixel-per-point pixel-per-point)))

(defmethod paint :level [g level]
  ^{:doc "paint the level walls"}
  (let [all-walls
        (concat (:walls level)
                (when (= :closed (:top-door level)) (list top-door))
                (when (= :closed (:bottom-door level)) (list bottom-door)))]
    (doseq [pt all-walls]
      (.setColor g Color/GRAY)
      (.fillRect g
                 (* (first pt) pixel-per-point)
                 (* (second pt) pixel-per-point)
                 pixel-per-point pixel-per-point))))

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

(defn ask-for-restart [frame title message]
  (=
   (JOptionPane/showConfirmDialog frame message title JOptionPane/YES_NO_OPTION)
   JOptionPane/YES_OPTION))

(defn create-ui [level snake apples]
  (let [frame (JFrame. "clj-snake")
        panel (doto (create-panel level snake apples)
                (.setFocusable true)
                (.addKeyListener
                 (proxy [KeyListener] []
                   (keyPressed [e]
                     (change-snake-direction snake (.getKeyCode e)))
                   (keyReleased [e])
                   (keyTyped [e]))))
        ]
    (doto frame
      (.add panel)
      (.pack)
      (.setVisible true))
    {:repaint #(.repaint panel)
     :won #(ask-for-restart frame "You have won!" "Start over?")
     :lost #(ask-for-restart frame "Game Over!" "Try again?")}))

