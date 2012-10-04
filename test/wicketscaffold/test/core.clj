(ns wicketscaffold.test.core
  (:use [clojure.test]
        [wicketscaffold.core]
        [clojure.java.io :only [file]])
  (:import [wicketscaffold.test.core FakeHibernateEntity]))

(defn file-exists [parent child & more]
  (.exists (apply file (into [parent child] more)))) 

(deftest test-generate-wicket-scaffold
  (is (true? (do (generate-wicket-scaffold FakeHibernateEntity :output "temp")
                 (and (file-exists "temp"  "wicketscaffold/test/core" "FakeHibernateEntityPage.java")
                      (file-exists "temp" "wicketscaffold/test/core" "FakeHibernateEntityPage.html")
                      (file-exists "temp" "wicketscaffold/test/core" "FakeHibernateEntityVO.java"))))))


(deftest test-verify
  (testing "fail on hibernate entities"
    (is (false? (verify {})))))