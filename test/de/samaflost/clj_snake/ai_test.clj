(ns de.samaflost.clj-snake.ai-test
  (:require [clojure.test :refer :all]
            [de.samaflost.clj-snake.game-test :as gt]
            [de.samaflost.clj-snake.ai :refer :all]))

(deftest random-direction-choice
  (testing "the expected four directions are present"
    (is (every? #{:left :right :up}
                (choose-directions {:strategy :random :direction :up} nil)))
    (is (every? (set (choose-directions {:strategy :random :direction :up} nil))
                [:left :right :up]))
    (is (= 4 (count (choose-directions {:strategy :random :direction :up} nil))))))

(deftest clockwise-direction-choice
  (testing "the expected four directions are present"
    (is (= [:up :right :down :left]
           (choose-directions {:strategy :clockwise :direction :rigth} nil)))))

(deftest picking-direction
  (testing "takes first choice when possible"
    (is (= :up (pick-direction {:body [[10 2] [10 3]]
                                :direction :right
                                :to-grow 0
                                :strategy :clockwise}
                               (gt/base-state))))))
