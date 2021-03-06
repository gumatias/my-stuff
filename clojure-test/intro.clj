(ns browser-testing.core-test
    (:require [clojure.test :refer :all]
                          [clj-webdriver.taxi :as t]))

(defn selenium-fixture
    [& browsers]
    (fn [test]
          (doseq [browser browsers]
                  (println (str "\n[ Testing " browser " ]"))
                  (t/set-driver! {:browser browser})
                  (test)
                  (t/quit))))

(use-fixtures :once (selenium-fixture :firefox))

(deftest ^:browser test-clojure
           (t/to "http://clojure.org")
         
           (is (= (t/title) "Clojure - home"))
           (is (= (t/current-url) "http://example.com/")))

         (deftest ^:browser test-clojure-download
                    (t/to "http://clojure.org")
                    (t/click {:xpath "//div[@class='menu']/*/a[text()='Download']"})
                  
                    (is (= (t/title) "Clojure - downloads"))
                    (is (= (t/current-url) "http://clojure.org/downloads"))
                    (is (re-find #"Rich Hickey" (t/text {:id "foot"}))))
