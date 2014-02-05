(ns de.samaflost.clj-snake.highscore
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json])
  (:import (java.util Date)))

;;; Management of the highscore list

; only here to make test transient
(def persistent-scores (atom true))
(def highscore-file
  (io/file (.. System (getProperties) (get "user.home")) ".clj-snake" "scores.json"))

(defn- load-list []
  (if (and @persistent-scores (.exists highscore-file))
    (with-open [r (io/reader highscore-file)]
      (json/read r :key-fn keyword))
    []))

(def highscore-list (atom (load-list)))

(defn- insert-score
  [score-list new-score]
  (take 10 (sort-by (comp - :score) (conj score-list new-score))))

(defn- persist-scores [score-list]
  (io/make-parents highscore-file)
  (with-open [w (io/writer highscore-file)]
    (json/write score-list w)))

(defn add-score
  "Adds a score to the list"
  [score name]
  (let [new-score {:score score :name name :date (.getTime (Date.))}
        added? (some (partial = new-score)
                     (swap! highscore-list insert-score new-score))]
    (when (and @persistent-scores added?) (persist-scores @highscore-list))
    added?))

