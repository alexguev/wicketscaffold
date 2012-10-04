(ns wicketscaffold.util)

(defn generate-file
  "tbd"
  [{:keys [path name] :as m} f]
  (let [{:keys [output]} *options*
        folder (file output path)]
    (.mkdirs folder)
    (with-open [w (writer (file folder name))]
      (.write w (f m)))))