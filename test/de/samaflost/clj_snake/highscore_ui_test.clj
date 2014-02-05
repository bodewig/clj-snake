(ns de.samaflost.clj-snake.highscore-ui-test
  (:require [clojure.test :refer :all]
            [de.samaflost.clj-snake.highscore :refer :all]
            [de.samaflost.clj-snake.highscore-test
             :refer [with-empty-highscore-list]]
            [de.samaflost.clj-snake.highscore-ui :refer :all]))

(use-fixtures :each with-empty-highscore-list)

(deftest get-table-test
  (testing "expected headlines"
    (is (= 3 (count (seq (second (get-score-table)))))))
  (testing "body content"
    (is (= 0 (alength (first (get-score-table)))))
    (add-score 42 "a1")
    (add-score 12 "b1")
    (add-score 99 "c1")
    (is (= 3 (alength (first (get-score-table)))))
    (is (= 3 (alength (aget (first (get-score-table)) 0))))
    (is (= 99 (aget (aget (first (get-score-table)) 0) 0)))
    (is (= "b1" (aget (aget (first (get-score-table)) 2) 1)))))
