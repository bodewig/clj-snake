(ns de.samaflost.clj-snake.ui
  (:import (javax.swing JPanel JFrame JOptionPane JLabel SwingConstants)
           (java.awt Color Dimension BorderLayout)
           (java.awt.event KeyListener KeyEvent))
  (:require [de.samaflost.clj-snake.config
             :refer [board-size ms-per-turn pixel-per-point]]
        [de.samaflost.clj-snake.level :refer [bottom-door top-door door-is-open?]]
        [de.samaflost.clj-snake.snake :refer [change-direction]]))

;;; The Swing UI of the game

(defmulti paint
  "painting something (the second arg) on an AWT Graphics instance"
  (fn [g item] (:type item)))

(defn- scale-x [location]
  (* (first location) pixel-per-point))

(defn- scale-y [location]
  (* (second location) pixel-per-point))

(defn- paint-rect [g color location]
  (.setColor g color)
  (.fillRect g (scale-x location) (scale-y location)
             pixel-per-point pixel-per-point))

(defmethod paint :snake [g snake]
  (doseq [pt (:body snake)]
    (paint-rect g (:color snake) pt)))

(defmethod paint :apple [g apple]
  (let [loc (:location apple)]
    (.setColor g (:color apple))
    (.fillOval g (scale-x loc) (scale-y loc)
               pixel-per-point pixel-per-point)))

(defmethod paint :level [g level]
  (let [all-walls
        (concat (:walls level)
                (filter (complement (partial door-is-open? level))
                        [top-door bottom-door]))]
    (doseq [pt all-walls]
      (paint-rect g Color/GRAY pt))))

(defn- create-panel [level snake apples]
  (proxy [JPanel] []
    (getPreferredSize []
      (Dimension. (* (:width board-size) pixel-per-point)
                  (* (:height board-size)  pixel-per-point)))
    (paintComponent [g]
      (proxy-super paintComponent g)
      (doseq [item (flatten [@level @snake @apples])]
        (paint g item)))))

(def key-code-to-direction
  ^{:private true}
  {
   KeyEvent/VK_LEFT :left
   KeyEvent/VK_RIGHT :right
   KeyEvent/VK_UP :up
   KeyEvent/VK_DOWN :down
   })

(defn- change-snake-direction [snake key-code]
  (when-let [new-dir (get key-code-to-direction key-code)]
    (dosync (alter snake change-direction new-dir))))

(defn ask-for-restart [frame title message]
  (=
   (JOptionPane/showConfirmDialog frame message title JOptionPane/YES_NO_OPTION)
   JOptionPane/YES_OPTION))

(defn repaint [game-panel score-label score]
  (.repaint game-panel)
  (.setText score-label (str @score)))

(defn create-ui [{:keys [level player apples score]}]
  (let [frame (JFrame. "clj-snake")
        game-panel (doto (create-panel level player apples)
                     (.setFocusable true)
                     (.addKeyListener
                      (proxy [KeyListener] []
                        (keyPressed [e]
                          (change-snake-direction player (.getKeyCode e)))
                        (keyReleased [e])
                        (keyTyped [e]))))
        score-label (JLabel. "0")
        ]
    (doto frame
      (.add game-panel BorderLayout/CENTER)
      (.add (doto (JPanel. (BorderLayout.))
              (.add score-label BorderLayout/EAST))
            BorderLayout/NORTH)
      (.pack)
      (.setVisible true))
    {:repaint #(repaint game-panel score-label score)
     :won #(ask-for-restart frame "You have won!" "Start over?")
     :lost #(ask-for-restart frame "Game Over!" "Try again?")}))

