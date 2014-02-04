(ns de.samaflost.clj-snake.highscore-ui
  (:require [de.samaflost.clj-snake.highscore :refer [highscore-list]])
  (:import (javax.swing JOptionPane JLabel
                        JMenuItem JTable JScrollPane)
           (javax.swing.table DefaultTableCellRenderer)
           (java.awt.event ActionListener)
           (java.util Date)
           (java.text DateFormat)))

(def ^:private table-columns [:score :name :date])
(def ^:private table-headings {:score "Score", :name "Name", :date "Date"})
(def ^:private date-format (DateFormat/getDateTimeInstance))

(defn get-score-table
  "Returns the current highscore list as a list of an array f arrays
   for the values and an array of column titles - this format is
   suitable for a JTable constructor."
  []
  (letfn [(apply-column-selectors [row] (map #(% row) table-columns))]
    (list (to-array-2d (map apply-column-selectors @highscore-list))
          (to-array (apply-column-selectors table-headings)))))

(defn- show-highscore-list [frame]
  (let [table-model (get-score-table)
        table (JTable. (first table-model) (second table-model))
        get-column (fn [idx] (.getColumn table (.getColumnName table idx)))]
    (.setCellRenderer (get-column 0)
                      (doto (DefaultTableCellRenderer.)
                        (.setHorizontalAlignment JLabel/RIGHT)))
    (.setCellRenderer (get-column 2)
                      (proxy [DefaultTableCellRenderer] []
                        (setValue [value]
                          (proxy-super setValue
                                       (.format date-format (Date. value))))))
    (doto table
      (.setFillsViewportHeight true)
      (.setShowGrid false))
    (JOptionPane/showMessageDialog frame (JScrollPane. table)
                                   "Highscore List"
                                   JOptionPane/INFORMATION_MESSAGE)))

(defn create-highscore-menu
  "Returns a JMenuItem with an ActionListener that shows the highscore list."
  [frame]
  (doto (JMenuItem. "Show Highscore List")
    (.addActionListener
     (proxy [ActionListener] []
       (actionPerformed [event] (show-highscore-list frame))))))
