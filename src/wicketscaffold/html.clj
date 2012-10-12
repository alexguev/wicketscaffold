(ns wicketscaffold.html
  (:use [wicketscaffold.util :only [write-to-file to-path]]
        [hiccup.core :only [html]])
  (:import [org.w3c.tidy Tidy]
           [java.io StringReader StringWriter]))

(declare transform wicket-html-page-template to-wicket-id to-human-readable-text format-html)

(defn generate [{:keys [package name properties] :as clazz}]
  (->> (transform name)
       (wicket-html-page-template name)
       (format-html)
       (hash-map :file-path (to-path package) :file-name (str name "Page.html") :content))
  )

(defn transform [name]
  [[:div [:a {:wicket:id "addNewLink"}]]
   [:table {:class "dataview" :wicket:id (to-wicket-id name)} (to-human-readable-text name)]])

;(into [:table] (for [p properties] [:tr [:td]]))

(defn wicket-html-page-template [title content]
  (html [:html
         [:head
          [:meta {:http_equiv "Content-Type" :content "text/html; charset=ISO-8859-1"}]
          [:title title]]
         [:body
          (into [:wicket:extend] content)]]))

(defn to-wicket-id [name]
  name)

(defn to-human-readable-text [name]
  name)

(defn jtidy []
  (doto (new Tidy)
    (.setDocType "html PUBLIC")
    (.setXmlTags true)
    (.setSmartIndent true)
    (.setWraplen 120)))

(defn format-html [html]
  (let [w (StringWriter.)]
    (.parse (jtidy) (StringReader. (str html)) w)
    (str "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n"
         w)))