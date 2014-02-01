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

