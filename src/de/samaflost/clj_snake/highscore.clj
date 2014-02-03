(ns de.samaflost.clj-snake.highscore
  (:import (java.util Date)))

;;; Management of the highscore list

(def highscore-list (ref []))

(defn- insert-score
  [score-list new-score]
  (take 10 (sort-by (comp - :score) (conj score-list new-score))))

(defn add-score
  "Adds a score to the list"
  [score name]
  (let [new-score {:score score :name name :date (Date.)}]
    (dosync
     (some (partial = new-score)
           (alter highscore-list insert-score new-score)))))

