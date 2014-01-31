(ns de.samaflost.clj-snake.game-test
  (:import (java.awt Color))
  (:require [clojure.test :refer :all]
            [de.samaflost.clj-snake.game :refer :all]
            [de.samaflost.clj-snake.level :refer [top-door]]))

(defn base-state []
  {:player (ref {:body [[4 4] [4 5] [4 6]] :direction :left :to-grow 1})
   :level (ref {:walls [[1 1]] :type :level :top-door :closed})
   :apples (ref [{:location [7 7] :remaining-nutrition 100}])
   :time-left-to-escape (ref 1000)
   :score (ref 0)
   :count-down (ref 1000)
   :mode (ref :eating)})

(defn eating-only-and-return [s]
  (dosync (eating-only-turn-actions s))
  s)

(deftest turn-in-eating-mode
  (testing "aging of apples"
    (is (= 99 (:remaining-nutrition
               (first (deref (:apples (eating-only-and-return (base-state))))))))
    (is (= 5 (count
              (deref (:apples 
                      (eating-only-and-return (assoc (base-state)
                                                :apples (ref []))))))))
    (is (= 5 (count
              (deref (:apples
                      (eating-only-and-return (assoc (base-state)
                                                :apples
                                                (ref [{:location [4 4]
                                                       :remaining-nutrition 1}])))))))))
  (testing "eating - may lead to escaping"
    (is (= 0 (deref (:score (eating-only-and-return (base-state))))))
    (let [last-apple {:location [4 4] :remaining-nutrition 100}
          two-apples [last-apple {:location [7 7] :remaining-nutrition 200}]]
      (is (= 99 (deref (:score (eating-only-and-return
                                (assoc (base-state) :apples (ref two-apples)))))))
      (is (= [{:location [7 7] :remaining-nutrition 199 :color Color/YELLOW}]
             (deref (:apples (eating-only-and-return
                              (assoc (base-state) :apples (ref two-apples)))))))
      (is (= []
             (deref (:apples (eating-only-and-return
                              (assoc (base-state) :apples (ref [last-apple])))))))
      (is (= :escaping
             (deref (:mode (eating-only-and-return
                              (assoc (base-state) :apples (ref [last-apple])))))))
      (is (= :open
             (:top-door (deref (:level
                                (eating-only-and-return
                                 (assoc (base-state) :apples (ref [last-apple])))))))))))

(defn escaping-only-and-return [s]
  (dosync (escaping-only-turn-actions s))
  s)

(deftest turn-in-escaping-mode
  (letfn [(esc-base-state [] (assoc (base-state) :mode (ref :escaping)))]
    (testing "counting down of time-left-to-escape - may lead to eating"
      (is (= 900 (deref (:time-left-to-escape (escaping-only-and-return (esc-base-state))))))
      (is (= 15000 (deref (:time-left-to-escape
                           (escaping-only-and-return (assoc (esc-base-state)
                                                       :time-left-to-escape (ref 99)))))))
      (is (= :eating (deref (:mode
                             (escaping-only-and-return (assoc (esc-base-state)
                                                         :time-left-to-escape (ref 99)))))))
      (is (= :closed (:top-door (deref (:level
                                        (escaping-only-and-return
                                         (assoc (esc-base-state) :time-left-to-escape (ref 99)))))))))))

(defn won-and-return [s]
  (dosync (won-actions s))
  s)

(deftest turn-in-won-mode
  (testing "is awarded time left"
    (is (= 1000 (deref (:score (won-and-return (base-state))))))))

(deftest won-or-lost
  (testing "properly evaluates won-lost states"
    (is (not (eval-won-or-lost (base-state))))
    (is (= :won (eval-won-or-lost (assoc (base-state)
                                    :mode (ref :escaping)
                                    :level (ref {:top-door :open})
                                    :player (ref {:body [top-door]})))))
    (is (= :lost (eval-won-or-lost (assoc (base-state)
                                     :level (ref {:walls [[4 4]]
                                                  :type :level
                                                  :top-door :closed})))))
    (is (= :lost (eval-won-or-lost (assoc (base-state)
                                     :player (ref {:body
                                                   [[4 4] [4 5] [5 5]
                                                    [5 4] [4 4] [3 4]]})))))
    (is (= :lost (eval-won-or-lost (assoc (base-state)
                                    :mode (ref :escaping)
                                    :level (ref {:top-door :closed :type :level})
                                    :player (ref {:body [top-door]})))))))

(defn starting-and-return [s]
  (dosync (starting-actions s))
  s)

(deftest turn-in-starting-mode
  (testing "counts down and eventually switches to :eating"
    (is (= 900 (deref (:count-down (starting-and-return
                                    (assoc (base-state)
                                      :mode (ref :starting)))))))
    (is (= :starting (deref (:mode (starting-and-return
                                    (assoc (base-state)
                                      :mode (ref :starting)))))))
    (is (= :eating (deref (:mode (starting-and-return
                                  (assoc (base-state)
                                    :count-down (ref 99)
                                    :mode (ref :starting)))))))))
