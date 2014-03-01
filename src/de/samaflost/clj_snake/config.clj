;; configuration of the snake Game
(ns de.samaflost.clj-snake.config
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]))

;; de-facto constants
(def ms-per-turn 100)
(def pixel-per-point 10)
(def number-of-apples 5)
(def ms-to-escape 12000)
(def initial-lifes 1)

(def config-file
  (io/file (.. System (getProperties) (get "user.home")) ".clj-snake" "config.json"))

(defn- load-config []
  (let [default-config {:board-size {:width 60 :height 60}
                        :ai-strategy "short-sighted"
                        :noise 50}]
    (if (.exists config-file)
      (with-open [r (io/reader config-file)]
        (merge default-config (json/read r :key-fn keyword)))
      default-config)))

(def ^{:doc "the configurable values of the game"}
  snake-configuration (atom (load-config)))

(defn- set-config [_ new-config] new-config)

(defn set-and-save-configuration
  "Persists and activates a configuration"
  [config]
  (io/make-parents config-file)
  (with-open [w (io/writer config-file)]
    (json/write config w))
  (swap! snake-configuration set-config config))

