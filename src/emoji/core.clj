(ns emoji.core
  (:import [org.jsoup Jsoup]))

(defonce page (-> "http://unicode.org/emoji/charts/full-emoji-list.html"
                Jsoup/connect
                (.timeout 60000)
                (.maxBodySize (* 100 1024 1024))
                .get))

(def table (first (.select page "table")))

(def trs (.select table "tr"))

(def trs-tds (for [tr trs]
               (vec (for [td (.select tr "td")]
                      (.text td)))))

(def emoji
  (for [row (filter not-empty trs-tds)]
    {:unicode (get row 1)
     :string (get row 2)
     :name (get row 15)
     :keywords (mapv clojure.string/trim
                 (clojure.string/split (get row 18) #","))}))

(with-open [out (clojure.java.io/writer "/tmp/emoji.edn")]
  (.write out "[\n")
  (doseq [e emoji]
    (.write out "  ")
    (.write out (pr-str e))
    (.write out "\n"))
  (.write out "]"))
