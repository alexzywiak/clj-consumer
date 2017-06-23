(ns clj-consumer.core
  (:require
   [clj-consumer.consumer :as consumer]
   [org.httpkit.server :refer :all]
   [clojure.core.async :refer [chan close! go <! >! >!!]])
  (:import [java.net InetAddress])
  (:gen-class))

(def host-name (.getHostName (. InetAddress getLocalHost)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "main")
  (def chatatoms (atom {}))

  (future (consumer/start-consumer (fn [msg]
                                     (println host-name)
                                     (doseq [c (vals @chatatoms)]
                                       (go (>! c msg))))))
 
  (defn handler [req]
    (println "handler")
    (with-channel req channel
      (let [c (chan)
            uuid (java.util.UUID/randomUUID)]
        (swap! chatatoms #(assoc % uuid c))
        (go (while true (send! channel (<! c))))
        (on-close channel (fn [status]
                            (println "channel closed")
                            (close! c)
                            (swap! chatatoms #(dissoc % uuid)))))))
  (run-server handler {:port 8080}))
