(ns wicketscaffold.html
  (:use [wicketscaffold.util :only [write-to-file to-path]]
        [hiccup.core :only [html]]))


(declare transform wicket-html-page-template)

(defn generate [{:keys [package name properties] :as clazz}]
  (->> (transform properties)
       (wicket-html-page-template name)
       (hash-map :file-path (to-path package) :file-name (str name "Page.html") :content))
  )

(defn transform [properties]
  (into [:table] (for [p properties] [:tr [:td]])))

(defn wicket-html-page-template [title content]
  (html [:html 
         [:head [:title title]]
         [:body content]]))