(ns de.samaflost.clj-snake.snake-test
  (:require [clojure.test :refer :all]
            [de.samaflost.clj-snake.snake :refer :all]))

(deftest snake-creation
  (testing "Creating a new Snake"
    (testing "for the player"
      (is (= (list [25 49]) (:body (new-snake true))))
      (is (= :up (:direction (new-snake true)))))
    (testing "for the AI"
      (is (= (list [25 0]) (:body (new-snake false))))
      (is (= :down (:direction (new-snake false)))))))

(deftest moving-the-snake
  (testing "moving"
    (testing "without growing"
      (is (= (list [25 48]) (:body (move (assoc (new-snake true)
                                           :to-grow 0)))))
      (is (= (list [25 50]) (:body (move (assoc (new-snake true)
                                           :direction :down
                                           :to-grow 0)))))
      (is (= (list [24 49]) (:body (move (assoc (new-snake true)
                                           :direction :left
                                           :to-grow 0)))))
      (is (= (list [26 49]) (:body (move (assoc (new-snake true)
                                           :direction :right
                                           :to-grow 0)))))))
    (testing "with growing"
      (is (= (list [25 48] [25 49]) (:body (move (new-snake true)))))
      (is (= (list [25 50] [25 49])
             (:body (move (assoc (new-snake true)
                            :direction :down)))))
      (is (= (list [24 49] [25 49])
             (:body (move (assoc (new-snake true)
                            :direction :left)))))
      (is (= (list [26 49] [25 49])
             (:body (move (assoc (new-snake true)
                            :direction :right))))))
    (testing "changes in to-grow"
      (is (= 3 (:to-grow (move (new-snake true)))))
      (is (= 0 (:to-grow (move (assoc (new-snake true)
                                 :to-grow 1)))))
      (is (= 0 (:to-grow (move (assoc (new-snake true)
                                 :to-grow -1)))))
      (is (= 41 (:to-grow (move (assoc (new-snake true)
                                  :to-grow 42)))))))

