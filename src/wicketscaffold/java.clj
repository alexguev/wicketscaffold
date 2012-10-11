(ns wicketscaffold.java
  (:use [wicketscaffold.util :only [to-path]]))

(defn generate [{:keys [package name properties] :as clazz}]
  {:file-path (to-path package) :file-name (str name "Page.java") :content "TBD!"})