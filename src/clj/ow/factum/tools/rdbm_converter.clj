(ns ow.factum.tools.rdbm-converter)

(defn- convert-row [fields row]
  (into [] (map (fn [f]
                  [nil f (get row f) nil :add]))
        fields))

(defn convert-table [fields rows]
  (map #(convert-row fields %) rows))
