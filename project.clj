(defproject io.lacuna/artifex "0.1.0-SNAPSHOT"
  :description ""
  :url "https://github.com/lacuna/artifex"
  :license {:name "MIT License"}
  :dependencies [#_[io.lacuna/bifurcan "0.1.0-alpha5"]]
  :java-source-paths ["src" "../bifurcan/src"]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]
                                  [criterium "0.4.3"]
                                  [virgil "0.1.8"]
                                  [org.clojure/test.check "0.9.0"]]}}
  :jvm-opts ^:replace ["-server"
                       "-Xmx10g"
                       "-ea:io.lacuna..."
                       "-XX:-OmitStackTraceInFastThrow"
                       #_"-XX:+UnlockDiagnosticVMOptions"
                       #_"-XX:+PrintAssembly"
                       #_"-XX:CompileCommand=print,io.lacuna.artifex.Vec::equals"
                       #_"-XX:CompileCommand=dontinline,io.lacuna.artifex.Vec::equals"]

  ;; Maven properties for the Maven God
  :scm {:url "git@github.com:lacuna/artifex.git"}
  :pom-addition [:developers [:developer
                              [:name "Zach Tellman"]
                              [:url "http://ideolalia.com"]
                              [:email "ztellman@gmail.com"]
                              [:timezone "-8"]]]
  :classifiers {:javadoc {:java-source-paths ^:replace []
                          :source-paths      ^:replace []
                          :resource-paths    ^:replace ["javadoc"]}
                :sources {:java-source-paths ^:replace ["src"]
                          :resource-paths    ^:replace []}})
