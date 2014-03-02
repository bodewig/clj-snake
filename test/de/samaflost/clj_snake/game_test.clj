(ns de.samaflost.clj-snake.game-test
  (:import (java.awt Color))
  (:require [clojure.test :refer :all]
            [de.samaflost.clj-snake.config :refer :all]
            [de.samaflost.clj-snake.game :refer :all]
            [de.samaflost.clj-snake.level :refer [bottom-door door-is-open? top-door]]))

(defn base-state []
  {:player (ref {:body [[4 4] [4 5] [4 6]] :direction :left :to-grow 1})
   :ai (ref {:body [[9 2] [9 1]] :direction :right :to-grow 0 :strategy :stubborn})
   :level (ref {:walls [[1 1]] :type :level :top-door :closed :number 0 :balls 1})
   :apples (ref [{:location [7 7] :remaining-nutrition 100}])
   :balls (ref [{:location [8 8]}])
   :time-left-to-escape (ref 1000)
   :score (ref 0)
   :lifes-left (ref 0)
   :next-extra-life-at (ref 10000)
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
      (is (= ms-to-escape (deref (:time-left-to-escape
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
    (is (= 1000 (deref (:score (won-and-return (base-state))))))
    (is (= :leaving (deref (:mode (won-and-return (base-state)))))))
  (testing "may award an additional live"
    (letfn [(winning-state [] (assoc (base-state)
                                :time-left-to-escape (ref grant-extra-life-at)
                                :score (ref 1)))]
      (is (= 1 (deref (:lifes-left (won-and-return (winning-state))))))
      (is (= (+ 10000 grant-extra-life-at)
             (deref (:next-extra-life-at (won-and-return (winning-state)))))))))

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
                                     :balls (ref [{:location [4 4]}])))))
    (is (= :lost (eval-won-or-lost (assoc (base-state)
                                     :player (ref {:body
                                                   [[4 4] [4 5] [5 5]
                                                    [5 4] [4 4] [3 4]]})))))
    (is (= :lost (eval-won-or-lost (assoc (base-state)
                                     :ai (ref {:body [[4 4] [4 5] [5 5]]})))))
    (is (= :lost (eval-won-or-lost (assoc (base-state)
                                    :mode (ref :escaping)
                                    :level (ref {:top-door :closed :type :level})
                                    :player (ref {:body [top-door]}))))))
  (testing "restarts if there are lifes left"
    (is (= :re-starting (eval-won-or-lost (assoc (base-state)
                                            :lifes-left (ref 1)
                                            :level (ref {:walls [[4 4]]
                                                         :type :level
                                                         :top-door :closed})))))))

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

(defn leaving-and-return [s]
  (dosync (leaving-actions s))
  s)

(deftest turn-in-leaving-mode
  (testing "is awarded time left"
    (is (= [[4 4]] (:body (deref (:player (leaving-and-return (base-state)))))))
    (is (= :starting (deref (:mode (leaving-and-return
                                    (assoc (base-state)
                                      :player (ref {:body []})))))))))
(defn move-ai-and-return [s]
  (dosync (move-ai s))
  s)

(deftest moving-ai
  (testing "it moves"
    (is (= {:body [[10 2] [9 2]] :direction :right :to-grow 0 :strategy :stubborn}
           (deref (:ai (move-ai-and-return (base-state))))))))

(defn re-starting-and-return [s]
  (dosync (re-starting-actions s))
  s)

(deftest turn-in-re-starting-mode
  (testing "steals life and re-starts current level"
    (is (= 1 (deref (:lifes-left (re-starting-and-return
                                    (assoc (base-state)
                                      :lifes-left (ref 2)
                                      :mode (ref :re-starting)))))))
    (is (= :starting (deref (:mode (re-starting-and-return
                                    (assoc (base-state)
                                      :lifes-left (ref 2)
                                      :mode (ref :re-starting)))))))))
(defn start-over-and-return [s]
  (start-over s)
  s)

(deftest start-over-results
  (testing "start-over really resets all relevant properties"
    (is (= 1 (count (:body (deref (:player (start-over-and-return (base-state))))))))
    (is (= 1 (count (:body (deref (:player (start-over-and-return (base-state))))))))
    (is (= number-of-apples (count
                             (deref (:apples (start-over-and-return (base-state)))))))
    (is (= 1 (count (deref (:balls (start-over-and-return (base-state)))))))
    (is (= ms-to-escape 
           (deref (:time-left-to-escape (start-over-and-return (base-state))))))
    (is (= :starting (deref (:mode (start-over-and-return (base-state))))))
    (is (= 3000 (deref (:count-down (start-over-and-return (base-state))))))
    (is (= 1 (deref (:lifes-left (start-over-and-return (base-state))))))
    (is (= grant-extra-life-at
           (deref (:next-extra-life-at (start-over-and-return (base-state))))))))
