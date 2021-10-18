(set-env!
  :source-paths   #{"src/clj"}

  :dependencies '[
                  ;; Clojure/Script
                  [org.clojure/clojure        "1.8.0"]
                  [org.clojure/tools.nrepl    "0.2.12"    :scope "test"]
                  ;; Atom.io proto-repl plugin
                  [proto-repl                 "0.3.1"     :scope "test"]
                  ;; Boot-clj with tasks
                  [adzerk/boot-reload         "0.5.1"     :scope "test"]
                  ;; Server templating
                  [hiccup                     "1.0.5"]
                  ;; XML
                  [org.clojure/data.xml       "0.2.0-alpha2"]])

(require '[adzerk.boot-reload :refer [reload]])

(task-options!
  pom {:project 'internet-log-analyzer
       :version "0.1.0-SNAPSHOT"}
  aot {:all true}
  jar {:manifest {"description" "Internet log analyzer"}
       :main 'analyzer.core
       :file "analyzer.jar"})

(deftask dev
  "Start development environment"
  []
  (comp
    (watch)
    (reload)
    (repl :server true)))

(deftask prod-clj
  "JAR"
  []
  (comp
    (aot)
    (uber)
    (jar)
    (sift :include #{#"analyzer.jar"})
    (target :dir #{"prod"})))
