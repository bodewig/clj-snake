(ns de.samaflost.clj-snake.config-ui
  (:require [de.samaflost.clj-snake.config :refer :all]
            [de.samaflost.clj-snake.util :refer [t]])
  (:import (javax.swing Box BoxLayout JDialog JLabel
                        JButton JTextField JComboBox JMenuItem)
           (java.awt GridLayout)
           (java.awt.event ActionListener)))

(def ^:private strategy-names
  (apply array-map
         (mapcat #(vector (name %) (t %))
                 [:random :greedy :aggressive :short-sighted])))

(defn- as-strategy-key [strategy-input]
  (let [value (.getSelectedItem strategy-input)]
    (key (some #(when (= (val %) value) %) strategy-names))))

(defn- as-board-size [width-input height-input]
  (letfn [(grab-value [input] (Integer/valueOf (.getText input)))]
    {:width (grab-value width-input) :height (grab-value height-input)}))

(defn- show-settings-dialog [frame]
  (let [dialog (JDialog. frame (t :title/settings))
        width-input (JTextField. (str (get-in @snake-configuration
                                              [:board-size :width])))
        height-input (JTextField. (str (get-in @snake-configuration
                                               [:board-size :height])))
        strategy-input (doto (JComboBox.
                              (into-array (vals strategy-names)))
                         (.setSelectedItem
                          (get strategy-names
                               (:ai-strategy @snake-configuration))))]
    (doto dialog
      (.setLayout (GridLayout. 3 2))
      (.add (JLabel. (t :label/board-size)))
      (.add (doto (Box/createHorizontalBox)
              (.add (Box/createHorizontalStrut 5))
              (.add width-input)
              (.add (Box/createHorizontalStrut 3))
              (.add (JLabel. "x"))
              (.add (Box/createHorizontalStrut 3))
              (.add height-input)
              (.add (Box/createHorizontalStrut 2))))
      (.add (JLabel. (t :label/ai)))
      (.add (doto (Box/createHorizontalBox)
              (.add (Box/createHorizontalStrut 5))
              (.add strategy-input)))
      (.add (doto (Box/createHorizontalBox)
              (.add (Box/createHorizontalGlue))
              (.add (doto (JButton. "OK")
                      (.addActionListener 
                       (proxy [ActionListener] []
                         (actionPerformed [event]
                           (set-and-save-configuration
                            {:ai-strategy (as-strategy-key strategy-input)
                             :board-size (as-board-size width-input height-input)})
                           (.setVisible dialog false))))))
              (.add (Box/createHorizontalGlue))))
      (.add (doto (Box/createHorizontalBox)
              (.add (Box/createHorizontalGlue))
              (.add (doto (JButton. (t :cancel))
                      (.addActionListener 
                       (proxy [ActionListener] []
                         (actionPerformed [event]
                           (.setVisible dialog false))))))
              (.add (Box/createHorizontalGlue))))
      (.pack)
      (.setVisible true))))

(defn create-settings-menu
  "Returns a JMenuItem with an ActionListener that shows the settings
  dialog."
  [frame]
  (doto (JMenuItem. (t :menu/settings))
    (.addActionListener
     (proxy [ActionListener] []
       (actionPerformed [event] (show-settings-dialog frame))))))
