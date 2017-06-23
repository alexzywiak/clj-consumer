(ns clj-consumer.core
  (:require
   [clj-consumer.consumer :as consumer]
   [org.httpkit.server :refer :all])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (defn handler [req]
    (with-channel req channel
      (consumer/start-consumer (fn [msg]
                                 (send! channel msg)))
      (on-close channel (fn [status] (println "channel closed")))))
  (run-server handler {:port 8080}))
