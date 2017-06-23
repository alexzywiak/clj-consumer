(ns clj-consumer.core
  (:require
   [clj-consumer.consumer :as consumer]
   [org.httpkit.server :refer :all]
   [clojure.core.async :refer [chan close! go <! >! >!!]])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "main")
  (def chatatoms (atom {}))

  (future (consumer/start-consumer (fn [msg]
                                     (doseq [c @chatatoms]
                                       (go (>! c msg))))))
 
  (defn handler [req]
    (println "handler")
    (with-channel req channel
      (let [c (chan)
            uuid (java.util.UUID/randomUUID)]
        (println @chatatoms)
        (swap! chatatoms #(assoc % uuid c))
        (go (while true (send! channel (<! c))))
        (on-close channel (fn [status]
                            (println "channel closed")
                            (close! c)
                            (swap! chatatoms #(dissoc % uuid)))))))
  (run-server handler {:port 8080}))
