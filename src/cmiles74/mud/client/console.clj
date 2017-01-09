(ns cmiles74.mud.client.console
  (:require
   [taoensso.timbre :as timbre
    :refer (log  trace  debug  info  warn  error  fatal  report
                 logf tracef debugf infof warnf errorf fatalf reportf
                 spy get-env log-env)]
   [taoensso.timbre.appenders.core :as appenders]
   [taoensso.timbre.profiling :as profiling
    :refer (pspy pspy* profile defnp p p*)]
   [slingshot.slingshot :only [throw+ try+]]
   [aleph.http :as http]
   [manifold.stream :as stream]
   [manifold.deferred :as deferred]
   [clojure.core.async :as async]
   [cmiles74.mud.client.terminal :as term]))

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
  "Updates the 'chrome' or non-scrolling content of the console."
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

(defn handle-resize-console
  "Handles updating the console display upon resize events."
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

(defn handle-unbound-key
  [console keystroke]
  (swap! (:input-buffer console) conj (.getCharacter keystroke))
  (term/write (:screen console) 2 (- (second @(:size console)) 1)
              (apply str @(:input-buffer console)))
  (term/move-cursor (:screen console)
                    (+ 2 (count @(:input-buffer console)))
                    (- (second @(:size console)) 1))
  (term/refresh (:screen console)))

(defn handle-pending-input
  "Reads new key strokes off the input channel and handles them on
  behalf of the application."
  [console]
  (async/go (loop []
              (when-let [keystroke (async/<! (:input-channel console))]
                (let [keybinding ((:keybindings console) keystroke)]
                  (if (not (nil? keybinding))
                    ((:handler keybinding) console)
                    (handle-unbound-key console keystroke)))
                (recur)))))

(defn create-interactive-console
  "Creates a new console that can also handle keyboard input."
  [keybindings server-socket]
  (let [console (create-console)
        handle-input-flag (ref true)
        handle-server-flag (ref true)
        input-channel (async/chan 1024)
        input-agent (agent {:channel input-channel
                            :screen (:screen console)})
        console-out (merge console
                           {:handle-input handle-input-flag
                            :input-channel input-channel
                            :input-agent input-agent
                            :server-socket server-socket
                            :input-buffer (atom [])
                            :keybindings keybindings})]

    ;; start a thread to poll for input and write to a channel
    (send-off input-agent
              (fn [state]
                (while @handle-input-flag
                  (let [key-in (.pollInput (:screen state))]
                    (if (not (nil? key-in))
                      (async/>!! (:channel state) key-in)
                      (Thread/sleep 100))))))

    ;; start a thread to handle pending input
    (handle-pending-input console-out)
    (term/refresh (:screen console-out))
    console-out))

(defn dispose-console
  "Releases a console and reclaims any associated resources."
  [console]
  (dosync (ref-set (:handle-input console) false))
  (async/close! (:input-channel console))
  (term/dispose (:screen console)))

;; editing functions

(defn erase-previous-character
  [console]
  (swap! (:input-buffer console) #(vec (reverse (rest (reverse %1)))))
  (term/write (:screen console) 2 (- (second @(:size console)) 1)
              (pad-line (first @(:size console)) (apply str @(:input-buffer console))))
  (term/move-cursor (:screen console)
                    (+ 2 (count @(:input-buffer console)))
                    (- (second @(:size console)) 1))
  (term/refresh (:screen console)))

(defn clear-input-buffer
  [console]
  (reset! (:input-buffer console) []))

(defn clear-input-area
  [console]
  (reset! (:input-buffer console) [])
  (term/write (:screen console) 2 (- (second @(:size console)) 1)
              (pad-line (first @(:size console)) " "))
  (term/move-cursor (:screen console) 2 (- (second @(:size console)) 1))
  (term/refresh (:screen console)))

(defn break-writeln-console
  "Breaks the provided line of text into lines that match the width of
  the console and then write then all to the scrollable content area of
  the console."
  [console line]
  (doseq [line-this (break-lines (first @(:size console)) 0 line)]
    (writeln-console console line-this)))
