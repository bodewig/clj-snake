(ns de.samaflost.clj-snake.ui
  (:import (javax.swing JPanel JFrame JOptionPane JLabel
                        SwingConstants Timer)
           (java.awt Color Dimension BorderLayout Font)
           (java.awt.event KeyListener KeyEvent ActionListener))
  (:require [de.samaflost.clj-snake.config
             :refer [board-size ms-per-turn pixel-per-point ms-to-escape]]
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

(defn- create-escape-panel [time-left-to-escape]
  (let [width (* (:width board-size) pixel-per-point)
        height (* 2  pixel-per-point)]
  (proxy [JPanel] []
    (getPreferredSize []
      (Dimension. width height))
    (paintComponent [g]
      (proxy-super paintComponent g)
      (let [fraction-left (/ @time-left-to-escape ms-to-escape)]
        (when (< fraction-left 1)
          (.setColor g
                     (cond (> fraction-left 0.7) Color/GREEN
                           (> fraction-left 0.3) Color/YELLOW
                           :else Color/RED))
          (.fillRect g 0 0 (* width fraction-left) height)))))))

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

(defn repaint [game-panel score-label escape-panel score]
  (.repaint game-panel)
  (.repaint escape-panel)
  (.setText score-label (str @score)))

(defn- restart-or-exit [start-over restart?]
  (if restart? (start-over) (System/exit 0)))

(defn- create-repaint-timer [start-over
                             frame game-panel score-label escape-panel
                             mode score]
  (let [r-or-e (partial restart-or-exit start-over)]
    (Timer. (/ ms-per-turn 2)
            (proxy [ActionListener] []
              (actionPerformed [event]
                (repaint game-panel score-label escape-panel score)
                (when (#{:won :lost} @mode)
                  (case @mode
                    :won (r-or-e
                          (ask-for-restart frame "You have won!" "Start over?"))
                    :lost (r-or-e
                           (ask-for-restart frame "Game Over!" "Try again?"))
                    )))))))

(defn create-ui [{:keys [level player apples score mode time-left-to-escape]}
                 start-over]
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
        escape-panel (create-escape-panel time-left-to-escape)
        repaint-timer (create-repaint-timer start-over
                                            frame game-panel score-label escape-panel 
                                            mode score)]
    (let [f (.getFont score-label)]
      (.setFont score-label
                (Font. (.getName f) (Font/BOLD) (* 2 pixel-per-point))))
    (doto frame
      (.add game-panel BorderLayout/CENTER)
      (.add (doto (JPanel. (BorderLayout.))
              (.add score-label BorderLayout/EAST)
              (.add escape-panel BorderLayout/SOUTH))
            BorderLayout/NORTH)
      (.pack)
      (.setVisible true))
    (.start repaint-timer)))
