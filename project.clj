(defproject nubank-authorizer "0.1.0-SNAPSHOT"
  :description "Nubank's Authorizer Code Challenge"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/data.json "1.0.0"]
                 [camel-snake-kebab "0.4.1"]
                 [clj-time "0.15.2"]]
  :main ^:skip-aot nubank-authorizer.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
