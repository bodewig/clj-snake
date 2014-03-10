(ns de.samaflost.clj-snake.level-test
  (:require [clojure.test :refer :all]
            [de.samaflost.clj-snake.config :refer [snake-configuration]]
            [de.samaflost.clj-snake.level :refer :all]
            [de.samaflost.clj-snake.levels :refer :all]))

(def half-width (/ (get-in @snake-configuration [:board-size :width]) 2))

(defn has-frame [level]
  (is (some #{[0 0]} (:walls level)))
  (is (some #{[0 (dec (get-in @snake-configuration [:board-size :height]))]}
            (:walls level)))
  (is (some #{[0 10]} (:walls level)))
  (is (some #{[10 0]} (:walls level)))
  (is (some #{[(dec (get-in @snake-configuration [:board-size :width])) 0]}
            (:walls level)))
  (is (some #{[(dec (get-in @snake-configuration [:board-size :width])) 10]}
            (:walls level)))
  (is (some #{[(dec (get-in @snake-configuration [:board-size :width]))
               (dec (get-in @snake-configuration [:board-size :height]))]}
            (:walls level))))

(deftest creation-of-level
  (testing "frame-walls"
    (has-frame (create-initial-level))
    (is (not (some #{[10 10]} (:walls (create-initial-level)))))
    (is (not (some #{[half-width 0]} (:walls (create-initial-level)))))
    (is (not (some #{[half-width
                      (dec (get-in @snake-configuration [:board-size :height]))]}
                   (:walls (create-initial-level))))))
  (testing "doors"
    (is :open (:bottom-door (create-initial-level))))
  (testing "level number"
    (is (= 0 (:number (create-initial-level)))))
  (testing "balls"
    (is (= 1 (:balls (create-initial-level))))))

(deftest second-level
  (testing "frame-walls"
    (has-frame (next-level (create-initial-level))))
  (testing "doors"
    (is :open (:bottom-door (next-level (create-initial-level)))))
  (testing "level number"
    (is (= 1 (:number (next-level (create-initial-level))))))
  (testing "balls"
    (is (= 1 (:balls (next-level (create-initial-level)))))))

(deftest wrapping-around
  (testing "frame-walls"
    (has-frame (next-level
                (assoc (create-initial-level)
                  :number (dec number-of-levels)))))
  (testing "doors"
    (is :open (:bottom-door (next-level
                             (assoc (create-initial-level)
                               :number (dec number-of-levels))))))
  (testing "level number"
    (is (= number-of-levels (:number (next-level
                                      (assoc (create-initial-level)
                                        :number (dec number-of-levels)))))))
  (testing "balls"
    (is (= 2 (:balls (next-level
                      (assoc (create-initial-level)
                        :number (dec number-of-levels))))))))

(deftest is-open?
  (testing "bottom-door"
    (is (door-is-open? {:bottom-door :open} bottom-door))
    (is (not (door-is-open? {:bottom-door :closed} bottom-door))))
  (testing "top-door"
    (is (door-is-open? {:top-door :open} top-door))
    (is (not (door-is-open? {:top-door :closed} top-door)))))

(deftest open-close-test
  (testing "bottom-door"
    (is (= :open (:bottom-door
                  (open-close {:bottom-door :closed} bottom-door :open))))
    (is (= :closed (:bottom-door
                    (open-close {:bottom-door :closed} bottom-door :closed)))))
  (testing "top-door"
    (is (= :open (:top-door
                  (open-close {:top-door :closed} top-door :open))))
    (is (= :closed (:top-door
                    (open-close {:top-door :closed} top-door :closed))))))

(deftest open-close-all-doors-test
  (testing "bottom-door"
    (is (= :open (:bottom-door
                  (open-close-all-doors {:bottom-door :closed :top-door :closed} :open))))
    (is (= :closed (:bottom-door
                    (open-close-all-doors {:bottom-door :closed :top-door :closed} :closed))))
    (is (= :open (:bottom-door
                  (open-close-all-doors {:bottom-door :open :top-door :closed} :open))))
    (is (= :closed (:bottom-door
                    (open-close-all-doors {:bottom-door :open :top-door :closed} :closed))))
    (is (= :open (:bottom-door
                  (open-close-all-doors {:bottom-door :closed :top-door :open} :open))))
    (is (= :closed (:bottom-door
                    (open-close-all-doors {:bottom-door :closed :top-door :open} :closed))))
    (is (= :open (:bottom-door
                  (open-close-all-doors {:bottom-open :closed :top-door :open} :open))))
    (is (= :closed (:bottom-door
                    (open-close-all-doors {:bottom-open :closed :top-door :open} :closed)))))
  (testing "top-door"
    (is (= :open (:top-door
                  (open-close-all-doors {:bottom-door :closed :top-door :closed} :open))))
    (is (= :closed (:top-door
                    (open-close-all-doors {:bottom-door :closed :top-door :closed} :closed))))
    (is (= :open (:top-door
                  (open-close-all-doors {:bottom-door :open :top-door :closed} :open))))
    (is (= :closed (:top-door
                    (open-close-all-doors {:bottom-door :open :top-door :closed} :closed))))
    (is (= :open (:top-door
                  (open-close-all-doors {:bottom-door :closed :top-door :open} :open))))
    (is (= :closed (:top-door
                    (open-close-all-doors {:bottom-door :closed :top-door :open} :closed))))
    (is (= :open (:top-door
                  (open-close-all-doors {:bottom-open :closed :top-door :open} :open))))
    (is (= :closed (:top-door
                    (open-close-all-doors {:bottom-open :closed :top-door :open} :closed))))))

