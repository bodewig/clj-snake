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

(def highscore-list (ref (load-list)))

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
        added? (dosync
                (some (partial = new-score)
                      (alter highscore-list insert-score new-score)))]
    (when (and @persistent-scores added?) (persist-scores @highscore-list))
    added?))

(def ^:private table-columns [:score :name :date])
(def ^:private table-headings {:score "Score", :name "Name", :date "Date"})

(defn get-score-table
  "Returns the current highscore list as a list of an array f arrays
   for the values and an array of column titles - this format is
   suitable for a JTable constructor."
  []
  (letfn [(apply-column-selectors [row] (map #(% row) table-columns))]
    (list (to-array-2d (map apply-column-selectors @highscore-list))
          (to-array (apply-column-selectors table-headings)))))
