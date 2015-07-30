(ns cmiles74.client.mud
  (:gen-class)
  (:require
   [taoensso.timbre :as timbre
    :refer (log  trace  debug  info  warn  error  fatal  report
                 logf tracef debugf infof warnf errorf fatalf reportf
                 spy get-env log-env)]
   [taoensso.timbre.appenders.core :as appenders]
   [taoensso.timbre.profiling :as profiling
    :refer (pspy pspy* profile defnp p p*)]
   [slingshot.slingshot :only [throw+ try+]]
   [dire.core :refer [with-handler!]]
   [aleph.http :as http]
   [manifold.stream :as stream]
   [manifold.deferred :as deferred]
   [clojure.core.async :as async]
   [cmiles74.client.terminal :as term]))

(defonce timbre-config
  (timbre/merge-config!
   {:appenders {:spit (appenders/spit-appender {:fname "mud-client.log"})}}))

;; text management

(defn break-lines
  "Returns a sequence of lines, each of the width of the screen."
  [width position text]
  (let [line-first (apply str (take (- width position) text))
        text-rest (drop (- width position) text)]
    (loop [lines [line-first] remaining text-rest]
      (let [line-this (apply str (take width remaining))
            line-rest (drop width remaining)]
        (if (< 0 (count line-rest))
          (recur (conj lines line-this) line-rest)
          (if (< 0 (count line-this))
            (conj lines line-this)
            lines))))))

(defn pad-line
  "Returns a new string padded out with spaces to match the supplied
  width."
  [width text]
  (apply str (cons text (apply str (take (- width (count text))
                                         (repeat " "))))))

;; console management

(defn clear-console
  "Clears the console's display."
  [console]
  (term/clear (:screen console))
  (reset! (:write-cursor console) {:row 0 :column 0}))

(defn scroll-console
  "Scrolls the middle of the console up one line."
  [console]
  (let [size-this (term/size (:screen console))
        height (second size-this)
        row-this (:row @(:write-cursor console))]
    (term/scroll-up (:screen console) 1 (- height 3))))

(defn update-console-chrome
  "Updates the 'chrome' or non-scrolling content for the console."
  [console]
  (let [width (first @(:size console))]
    (term/write-reverse (:screen console) 0 0 (pad-line width "Mud Client v0.0.0"))
    (term/write-reverse (:screen console) 0 (- (second @(:size console)) 2)
                   (pad-line width "READY"))
    (term/write (:screen console) 0 (- (second @(:size console)) 1) ">")))

(defn writeln-console [console line]
  "Writes the provided line of text to the scrolling content area of the console."
  (let [row-this (:row @(:write-cursor console))
        rows-total (- (second @(:size console)) 3)]
    (update-console-chrome console)
    (if (= rows-total row-this)
      (do
        (scroll-console console)
        (term/write-string (:screen console) 0 (:row @(:write-cursor console)) line))
      (do
        (reset! (:write-cursor console) {:row (inc row-this) :column 0})
        (term/write-string (:screen console) 0 (:row @(:write-cursor console)) line)))
    (term/refresh (:screen console))))

(defn break-writeln-console
  [console line]
  (doseq [line-this (break-lines (first @(:size console)) 0 line)]
    (writeln-console console line-this)))

(defn handle-resize-console
  [console size]
  (reset! (:size console) size)
  (update-console-chrome console))

(defn create-console
  "Returns a new console instance."
  []
  (let [screen-this (term/create)
        console-this {:screen screen-this
                      :write-cursor (atom {:row 0 :column 0})
                      :size (atom [80 40])}]
    (term/add-resize-handler screen-this
                        (fn [screen-this size]
                          (handle-resize-console console-this size)))
    (reset! (:size console-this) (term/size (:screen console-this)))
    (term/move-cursor screen-this 2 (- (second @(:size console-this)) 1))
    (update-console-chrome console-this)
    console-this))

(defn dispose-console
  "Releases a console and reclaims any associated resources."
  [console]
  (dosync (ref-set (:handle-input console) false))
  (async/close! (:input-channel console))
  (term/dispose (:screen console)))

;; input management

(def RETURN-KEY (term/vim-keystroke "<Return>"))
(def BACKSPACE-KEY (term/vim-keystroke "<BS>"))

(defn handle-pending-input
  [console]
  (async/go (loop []
              (when-let [keystroke (async/<! (:input-channel console))]
                (cond
                  (= keystroke RETURN-KEY)
                  (do
                    (writeln-console console (apply str @(:input-buffer console)))
                    (reset! (:input-buffer console) [])
                    (term/write (:screen console) 2 (- (second @(:size console)) 1)
                           (pad-line (first @(:size console)) " "))
                    (term/move-cursor (:screen console)
                                 (+ 2 (count @(:input-buffer console)))
                                 (- (second @(:size console)) 1))
                    (term/refresh (:screen console))
                    (recur))

                  (= keystroke BACKSPACE-KEY)
                  (do
                    (swap! (:input-buffer console) #(vec (reverse (rest (reverse %1)))))
                    (term/write (:screen console) 2 (- (second @(:size console)) 1)
                           (pad-line (first @(:size console)) (apply str @(:input-buffer console))))
                    (term/move-cursor (:screen console)
                                 (+ 2 (count @(:input-buffer console)))
                                 (- (second @(:size console)) 1))
                    (term/refresh (:screen console))
                    (recur))

                  :else
                  (do
                    (swap! (:input-buffer console) conj (.getCharacter keystroke))
                    (term/write (:screen console) 2 (- (second @(:size console)) 1)
                           (apply str @(:input-buffer console)))
                    (term/move-cursor (:screen console)
                                 (+ 2 (count @(:input-buffer console)))
                                 (- (second @(:size console)) 1))
                    (term/refresh (:screen console))
                    (recur)))))))

(defn create-interactive-console
  []
  (let [console (create-console)
        handle-input-flag (ref true)
        input-channel (async/chan 1024)
        input-agent (agent {:channel input-channel
                            :screen (:screen console)})
        console-out (merge console
                           {:handle-input handle-input-flag
                            :input-channel input-channel
                            :input-agent input-agent
                            :input-buffer (atom [])})]

    ;; start a thread to poll for input from the console
    (send-off input-agent
              (fn [state]
                (while @handle-input-flag
                  (let [key-in (.pollInput (:screen state))]
                    (if (not (nil? key-in))
                      (do (async/>!! (:channel state) key-in)
                          ;;(timbre/info key-in)
                          )
                      (Thread/sleep 100))))))

    ;; start a thread to handle pending input
    (handle-pending-input console-out)
    (term/refresh (:screen console-out))
    console-out))

(defn main
  [& args]
  (create-interactive-console))

(defn -main
  [& args]
  (apply main args))
