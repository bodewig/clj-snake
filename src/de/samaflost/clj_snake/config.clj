;; configuration of the snake Game
(ns de.samaflost.clj-snake.config)

(def board-size {:width 60 :height 60})
(def ms-per-turn 100)
(def pixel-per-point 10)
(def number-of-apples 5)
(def ms-to-escape 12000)
(def ai-strategy :short-sighted)
