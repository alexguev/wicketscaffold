(ns wicketscaffold.util
  (:use [clojure.java.io])
  (:require [clojure.string :as s]))

(defn to-path [package] (s/replace package "." "/"))

(defn when-message [x m]
  (when x (println m) true))

(defn write-to-file
  "tbd"
  [path name content]
  (let [{:keys [output]} {:output "temp"}
        folder (file output path)]
    (.mkdirs folder)
    (with-open [w (writer (file folder name))]
      (.write w content))))