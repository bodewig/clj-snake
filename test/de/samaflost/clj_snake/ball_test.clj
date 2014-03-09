(ns de.samaflost.clj-snake.ball-test
  (:import (java.awt Color))
  (:require [clojure.test :refer :all]
            [de.samaflost.clj-snake.ball :refer :all]))

(deftest create-balls-test
  (testing "potentially long running test until rand has found the few eligible places"
    (let [places-taken [{:body (map vector
                                    (flatten (repeat 40 (range 0 50)))
                                    (repeatedly #(rand-int 50)))}
                        {:location [(rand-int 50) (rand-int 50)]}]]
      (is (= 2000 (count (:body (first places-taken)))))
      (is (= 5 (count (create-balls 5 places-taken))))
      (is (not (some (set (:body (first places-taken)))
                     (map :location (create-balls 5 places-taken)))))
      (is (not (some (set (:location (second places-taken)))
                     (map :location (create-balls 5 places-taken))))))))

(deftest bounce-single-ball
  (testing "bouncing with no obstacles"
    (is (= {:location [2 2] :direction 0}
           (bounce {:location [1 1] :direction 0} [])))
    (is (= {:location [0 2] :direction 1}
           (bounce {:location [1 1] :direction 1} [])))
    (is (= {:location [0 0] :direction 2}
           (bounce {:location [1 1] :direction 2} [])))
    (is (= {:location [2 0] :direction 3}
           (bounce {:location [1 1] :direction 3} []))))
  (testing "bouncing with vertical wall to the left"
    (let [wall (map #(array-map :location [0 %]) (range 0 3))]
      (is (= {:location [2 2] :direction 0}
             (bounce {:location [1 1] :direction 0} wall)))
      (is (= {:location [2 2] :direction 0}
             (bounce {:location [1 1] :direction 1} wall)))
      (is (= {:location [2 0] :direction 3}
             (bounce {:location [1 1] :direction 2} wall)))
      (is (= {:location [2 0] :direction 3}
             (bounce {:location [1 1] :direction 3} wall)))))
  (testing "bouncing with vertical wall to the right"
    (let [wall (map #(array-map :location [2 %]) (range 0 3))]
      (is (= {:location [0 2] :direction 1}
             (bounce {:location [1 1] :direction 0} wall)))
      (is (= {:location [0 2] :direction 1}
             (bounce {:location [1 1] :direction 1} wall)))
      (is (= {:location [0 0] :direction 2}
             (bounce {:location [1 1] :direction 2} wall)))
      (is (= {:location [0 0] :direction 2}
             (bounce {:location [1 1] :direction 3} wall)))))
  (testing "bouncing with horizontal wall at the top"
    (let [wall (map #(array-map :location [% 0]) (range 0 3))]
      (is (= {:location [2 2] :direction 0}
             (bounce {:location [1 1] :direction 0} wall)))
      (is (= {:location [0 2] :direction 1}
             (bounce {:location [1 1] :direction 1} wall)))
      (is (= {:location [0 2] :direction 1}
             (bounce {:location [1 1] :direction 2} wall)))
      (is (= {:location [2 2] :direction 0}
             (bounce {:location [1 1] :direction 3} wall)))))
  (testing "bouncing with horizontal wall at the bottom"
    (let [wall (map #(array-map :location [% 2]) (range 0 3))]
      (is (= {:location [2 0] :direction 3}
             (bounce {:location [1 1] :direction 0} wall)))
      (is (= {:location [0 0] :direction 2}
             (bounce {:location [1 1] :direction 1} wall)))
      (is (= {:location [0 0] :direction 2}
             (bounce {:location [1 1] :direction 2} wall)))
      (is (= {:location [2 0] :direction 3}
             (bounce {:location [1 1] :direction 3} wall)))))
  (testing "bouncing in a corner"
    (is (= {:location [0 0] :direction 2}
           (bounce {:location [1 1] :direction 0}
                   (concat
                    (map #(array-map :location [% 2]) (range 0 3))
                    (map #(array-map :location [2 %]) (range 0 3))))))
    (is (= {:location [2 0] :direction 3}
           (bounce {:location [1 1] :direction 1}
                   (concat
                    (map #(array-map :location [% 2]) (range 0 3))
                    (map #(array-map :location [0 %]) (range 0 3))))))
    (is (= {:location [2 2] :direction 0}
           (bounce {:location [1 1] :direction 2}
                   (concat
                    (map #(array-map :location [% 0]) (range 0 3))
                    (map #(array-map :location [0 %]) (range 0 3))))))
    (is (= {:location [0 2] :direction 1}
           (bounce {:location [1 1] :direction 3}
                   (concat
                    (map #(array-map :location [% 0]) (range 0 3))
                    (map #(array-map :location [2 %]) (range 0 3)))))))
  (testing "bouncing with diagonal"
    (let [downward [{:location [5 5]} {:location [6 6]}]
          upward [{:location [5 5]} {:location [6 4]}]]
      (is (= {:location [4 3] :direction 2}
             (bounce {:location [5 4] :direction 0} upward)))
      (is (= {:location [7 4] :direction 3}
             (bounce {:location [6 5] :direction 1} downward)))
      (is (= {:location [7 6] :direction 0}
             (bounce {:location [6 5] :direction 2} upward)))
      (is (= {:location [4 7] :direction 1}
             (bounce {:location [5 6] :direction 3} downward)))))
  (testing "stuck"
    (let [wall (concat
                (map #(array-map :location [% 2]) (range 0 3))
                (map #(array-map :location [% 0]) (range 0 3))
                (map #(array-map :location [0 %]) (range 0 3))
                (map #(array-map :location [2 %]) (range 0 3)))]
      (is (= {:location [1 1] :direction 0}
             (bounce {:location [1 1] :direction 0} wall)))
      (is (= {:location [1 1] :direction 1}
             (bounce {:location [1 1] :direction 1} wall)))
      (is (= {:location [1 1] :direction 2}
             (bounce {:location [1 1] :direction 2} wall)))
      (is (= {:location [1 1] :direction 3}
             (bounce {:location [1 1] :direction 3} wall))))))

(deftest bouncy-many-balls
  (testing "no collisions"
    (is (= [{:location [2 2] :direction 0} {:location [2 4] :direction 1}]
           (bounce-all [{:location [1 1] :direction 0} {:location [3 3] :direction 1}]
                       []))))
  (testing "crossing over"
    (is (= [{:location [2 2] :direction 0} {:location [1 2] :direction 1}]
           (bounce-all [{:location [1 1] :direction 0} {:location [2 1] :direction 1}]
                       []))))
  (testing "bouncing against each other"
    (is (= [{:location [2 2] :direction 0} {:location [2 0] :direction 2}]
           (bounce-all [{:location [1 1] :direction 0} {:location [3 1] :direction 1}]
                       [])))
    (is (= [{:location [0 2] :direction 1} {:location [1 3] :direction 1}]
           (bounce-all [{:location [1 1] :direction 0} {:location [2 2] :direction 1}]
                       [])))))
