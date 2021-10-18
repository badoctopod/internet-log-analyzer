(ns analyzer.core
  (:require
    [clojure.string   :as s    :refer [split blank?]]
    [clojure.data.xml :as xml  :refer [parse]]
    [clojure.java.io  :as io   :refer [reader]]
    [hiccup.page      :as html :refer [html5]]))

(defn- read-csv [file]
  (rest (with-open [file (reader file)]
                     (doall (for [line (line-seq file)]
                              (s/split line #","))))))

(defn- parse-ripe
  [url ip]
  (xml/parse (java.io.StringReader. (slurp (str url ip)))))

(defn- netname [ip]
  (-> (parse-ripe "http://rest.db.ripe.net/search?flags=r&query-string=" ip)
      (:content)
      (nth 5)
      (:content)
      (nth 1)
      (:content)
      (nth 7)
      (:content)
      (nth 3)
      (:attrs)
      (:value)))

(defn- parse-csv [file]
  (remove nil?
    (into []
      (distinct
        (for [i (read-csv file)]
          (let [date (str (nth i 1))
                ip   (str (nth i 10))]
            (if-not (s/blank? ip)
              {:date date
               :ip ip
               :netname (netname ip)})))))))

(defn- render-html [file]
  (html5 {:lang "ru"}
    [:head
      [:meta {:charset :utf-8}]
      [:meta {:name    "viewport"
              :content "width=device-width, initial-scale=1"}]
      [:title "Статистика использования сети Интернет"]
      [:link {:rel "stylesheet" :href "css/jquery.dataTables.min.css"}]]
    [:body
      [:table#statistics.display
        [:thead
          [:tr
            [:th "Дата"]
            [:th "IP-адрес сети"]
            [:th "Имя сети"]]]
        [:tbody
        (for [{:keys [date ip netname]} (parse-csv file)]
          [:tr
            [:td date]
            [:td ip]
            [:td netname]])]]
    [:script {:src "js/jquery-1.12.4.js"}]
    [:script {:src "js/jquery.dataTables.min.js"}]
    [:script "$(document).ready(function() {
                $('#statistics').DataTable();});"]]))

(defn- write-html [file]
  (spit "html/statistics.html" (render-html file)))

(defn -main [file]
  (println "Writing 'html/statistics.html'...")
  (write-html file)
  (println "'html/statistics.html' has been written successfully"))
