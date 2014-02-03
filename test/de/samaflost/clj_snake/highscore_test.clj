(ns de.samaflost.clj-snake.highscore-test
  (:require [clojure.test :refer :all]
            [de.samaflost.clj-snake.highscore :refer :all]))

(use-fixtures :each
  (fn [test]
    (reset! persistent-scores false)
    (dosync (ref-set highscore-list []))
    (test)
    (dosync (ref-set highscore-list []))))

(deftest adding
  (testing "list is sorted by score desc"
    (add-score 42 "a")
    (add-score 12 "b")
    (add-score 99 "c")
    (add-score 7 "d")
    (add-score 94 "e")
    (add-score 33 "f")
    (is (= [99 94 42 33 12 7] (map :score @highscore-list)))
    (is (= ["c" "e" "a" "f" "b" "d"] (map :name @highscore-list))))
  (testing "list is limited to 10 entries"
    (add-score 1 "a2")
    (add-score 19 "b2")
    (add-score 66 "c2")
    (add-score 192 "d2")
    (add-score 51 "e2")
    (add-score 18 "f2")
    (is (= [192 99 94 66 51 42 33 19 18 12] (map :score @highscore-list)))
    (is (= ["d2" "c" "e" "c2" "e2" "a" "f" "b2" "f2" "b"]
           (map :name @highscore-list))))
  (testing "add-score's returns whether a score has been added"
    (is (add-score 22 "foo"))
    (is (not (add-score 2 "bar")))))
