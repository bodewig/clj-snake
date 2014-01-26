(ns de.samaflost.clj-snake.snake
  (:use [de.samaflost.clj-snake.config :only [board-size]]))

(def dirs
  ^{:private true}
  {:up [0 -1] :down [0 1] :left [-1 0] :right [1 0]})

(defn- new-head [snake]
  (vec (apply map + [(first (:body snake)) (get dirs (:direction snake))])))

(defn new-snake [player?]

  ^{:doc "Creates a new snake that will start at the bottom for a
          player or at the top for the program-controlled snake"}
  {
   :body (list [(/ (:width board-size) 2)
                (if player? (:height board-size) 0)])
   :direction (if player? :up :down)
   :to-grow 0
   })

(defn move [snake]
  ^{:doc "Moves and potentially grows a snake"}
  (let [body (:body snake)
        to-grow (:to-grow snake)]
    (assoc snake :body (cons (new-head snake)
                             (if (> to-grow 0) body (butlast body)))
          :to-grow (if (> to-grow 1) (dec to-grow) 0))))

(defn change-direction [snake new-dir]
  ^{:doc "changes the direction of the snake to new-dir"}
  (assoc snake :direction new-dir))

