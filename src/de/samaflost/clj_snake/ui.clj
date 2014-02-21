(ns de.samaflost.clj-snake.ui
  (:import (javax.swing JPanel JFrame JOptionPane JLabel
                        JMenu JMenuBar JMenuItem
                        KeyStroke SwingConstants Timer)
           (java.awt Color Dimension BorderLayout Font)
           (java.awt.event KeyListener KeyEvent
                           ActionListener ActionEvent))
  (:require [de.samaflost.clj-snake.config
             :refer [snake-configuration ms-per-turn pixel-per-point ms-to-escape]]
        [de.samaflost.clj-snake.config-ui :refer [create-settings-menu]]
        [de.samaflost.clj-snake.highscore-ui :refer [create-highscore-menu]]
        [de.samaflost.clj-snake.level :refer [bottom-door top-door door-is-open?]]
        [de.samaflost.clj-snake.snake :refer [change-direction head tail]]
        [de.samaflost.clj-snake.util :refer [t]]))

;;; The Swing UI of the game

(defmulti paint
  "painting something (the second arg) on an AWT Graphics instance"
  (fn [g item] (:type item)))

(defn- scale-x [location]
  (* (first location) pixel-per-point))

(defn- scale-y [location]
  (* (second location) pixel-per-point))

(defn- paint-rect [g color location]
  (doto g
    (.setColor color)
    (.fillRect (scale-x location) (scale-y location)
               pixel-per-point pixel-per-point)))

(defn- paint-half-rect [g color location half]
  (let [width (if (#{:up :down} half) pixel-per-point (/ pixel-per-point 2))
        height (if (#{:up :down} half) (/ pixel-per-point 2) pixel-per-point)
        x-off (if (= :right half) (/ pixel-per-point 2) 0)
        y-off (if (= :down half) (/ pixel-per-point 2) 0)]
    (doto g
      (.setColor color)
      (.fillRect (+ (scale-x location) x-off) (+ (scale-y location) y-off)
                 width height))))

(defn- paint-oval 
  ([g item] (paint-oval g item (:color item)))
  ([g item color]
     (let [loc (:location item)]
       (doto g
         (.setColor color)
         (.fillOval (scale-x loc) (scale-y loc)
                    pixel-per-point pixel-per-point)))))

(def ^:private opposite-of
  {:up :down
   :down :up
   :left :right
   :right :left})

(defmethod paint :snake [g snake]
  (let [head (head snake)]
    (paint-oval g head (:color snake))
    (when-let [tail (seq (:body (tail snake)))]
      (paint-half-rect g (:color snake) (:location head)
                       (opposite-of (:direction snake)))
      (doseq [pt tail]
        (paint-rect g (:color snake) pt)))))

(defmethod paint :apple [g apple]
  (paint-oval g apple))

(defmethod paint :ball [g ball]
  (paint-oval g ball))

(defmethod paint :level [g level]
  (let [all-walls
        (concat (:walls level)
                (filter (complement (partial door-is-open? level))
                        [top-door bottom-door]))]
    (dorun (map (partial paint-rect g Color/GRAY) all-walls))))

(defn- paint-count-down [g count-down]
  (let [number (str (inc (int (/ count-down 1000))))
        bounds (.. g (getFont) (getStringBounds number (.getFontRenderContext g)))
        x (int (/ (- (* (get-in @snake-configuration [:board-size :width])
                        pixel-per-point)
                     (.getWidth bounds)) 2))
        y (int (/ (+ (* (get-in @snake-configuration [:board-size :height])
                        pixel-per-point)
                     (.getHeight bounds)) 2))]
    (doto g
      (.setColor Color/MAGENTA)
      (.drawString number x y))))

(defn- create-panel [mode level ai snake apples balls count-down]
  (proxy [JPanel] []
    (getPreferredSize []
      (Dimension. (* (get-in @snake-configuration [:board-size :width])
                     pixel-per-point)
                  (* (get-in @snake-configuration [:board-size :height])
                     pixel-per-point)))
    (paintComponent [g]
      (proxy-super paintComponent g)
      (when-not (= @mode :initial)
        (if (= @mode :starting)
          (paint-count-down g @count-down)
          (dorun (map (partial paint g) (flatten [@snake @apples @balls @ai]))))
        (paint g @level)))))

(defn- create-escape-panel [time-left-to-escape]
  (let [width (* (get-in @snake-configuration [:board-size :width]) pixel-per-point)
        height (* 2  pixel-per-point)]
  (proxy [JPanel] []
    (getPreferredSize []
      (Dimension. width height))
    (paintComponent [g]
      (proxy-super paintComponent g)
      (let [fraction-left (/ @time-left-to-escape ms-to-escape)]
        (when (< fraction-left 1)
          (doto g
            (.setColor
             (cond (> fraction-left 0.7) Color/GREEN
                   (> fraction-left 0.3) Color/YELLOW
                   :else Color/RED))
            (.fillRect 0 0 (* width fraction-left) height))))))))

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

(defn- create-cursor-listener [player]
  (proxy [KeyListener] []
    (keyPressed [e]
      (change-snake-direction player (.getKeyCode e)))
    (keyReleased [e])
    (keyTyped [e])))

(defn lost-callback [frame made-high-score?]
  (JOptionPane/showMessageDialog
   frame
   (t (if made-high-score? :lost/highscore :lost/no-highscore))
   (t :game-over)
   JOptionPane/INFORMATION_MESSAGE))

(defn repaint [game-panel score-label escape-panel score]
  (.repaint game-panel)
  (.repaint escape-panel)
  (.setText score-label (str @score)))

(defn- create-repaint-timer [start-over
                             frame game-panel score-label escape-panel
                             mode score]
  (Timer. (/ ms-per-turn 2)
          (proxy [ActionListener] []
            (actionPerformed [event]
              (repaint game-panel score-label escape-panel score)))))

(defn- create-menu-bar [frame start-over]
  (doto (JMenuBar.)
    (.add (doto (JMenu. "Snake")
            (.add (doto (JMenuItem. (t :menu/new) KeyEvent/VK_N)
                    (.setAccelerator
                     (KeyStroke/getKeyStroke
                      KeyEvent/VK_N ActionEvent/CTRL_MASK))
                    (.addActionListener
                     (proxy [ActionListener] []
                       (actionPerformed [event] (start-over))))))
            (.addSeparator)
            (.add (create-highscore-menu frame))
            (.add (create-settings-menu frame))
            (.addSeparator)
            (.add (doto (JMenuItem. (t :menu/quit) KeyEvent/VK_Q)
                    (.setAccelerator
                     (KeyStroke/getKeyStroke
                      KeyEvent/VK_Q ActionEvent/CTRL_MASK))
                    (.addActionListener
                     (proxy [ActionListener] []
                       (actionPerformed [event] (System/exit 0))))))))))
                    

(defn create-ui [{:keys [level ai player apples score mode
                         time-left-to-escape balls count-down]}
                 start-over]
  (let [frame (JFrame. "clj-snake")
        game-panel (doto (create-panel mode level ai player apples balls count-down)
                     (.setFocusable true)
                     (.addKeyListener (create-cursor-listener player)))
        score-label (JLabel. "0")
        escape-panel (create-escape-panel time-left-to-escape)
        repaint-timer (create-repaint-timer start-over
                                            frame game-panel score-label escape-panel 
                                            mode score)]
    (let [f (.getFont score-label)]
      (.setFont score-label
                (Font. (.getName f) (Font/BOLD) (* 2 pixel-per-point)))
      (.setFont game-panel
                (Font. (.getName f) (Font/BOLD) (* 10 pixel-per-point))))
    (doto frame
      (.add game-panel BorderLayout/CENTER)
      (.add (doto (JPanel. (BorderLayout.))
              (.add score-label BorderLayout/EAST)
              (.add escape-panel BorderLayout/SOUTH))
            BorderLayout/NORTH)
      (.setJMenuBar (create-menu-bar frame start-over))
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.pack)
      (.setVisible true))
    (.start repaint-timer)
    (partial lost-callback frame)))
