(ns de.samaflost.clj-snake.highscore
  (:import (java.util Date)))

;;; Management of the highscore list

(def highscore-list (ref []))

(defn min-score
  "The smallest score that is part of the highscore list"
  []
  (if-let [scores (seq @highscore-list)]
    (:score (last scores))
    0))

(defn- insert-score
  [score-list new-score]
  (take 10 (sort-by (comp - :score) (conj score-list new-score))))

(defn add-score
  "Adds a score to the list"
  [score name]
  (dosync
   (alter highscore-list insert-score
          {:score score :name name :date (Date.)})))

