(ns wicketscaffold.html
  (:use [wicketscaffold.util :only [generate-file]]
        [hiccup.core :only [html]]))


(declare transform wicket-html-page-template name )
(defn generate [path name properties]
  (->> (transform properties)
       (wicket-html-page-template name)
       (generate-file path (str name ".html")))
  )

(defn wicket-html-page-template [title content]
  (html [:html 
         [:head [:title title]]
         [:body content]]))

(defn transform [fields]
  [:table])
