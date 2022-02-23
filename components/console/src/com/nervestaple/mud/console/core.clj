(ns com.nervestaple.mud.console.core
  (:require
   [clojure.core.async :as async]
   [com.nervestaple.mud.terminal.interface :as terminal]))

(defn break-lines
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
  [width text]
  (apply str (cons text (apply str (take (- width (count text))
                                         (repeat " "))))))

(defn clear-console
  [console]
  (terminal/clear (:screen console))
  (reset! (:write-cursor console) {:row 0 :column 0}))

(defn scroll-console
  [console]
  (let [size-this (terminal/size (:screen console))
        height (second size-this)]
    (terminal/scroll-up (:screen console) 1 (- height 3))))

(defn update-console-chrome
  [console]
  (let [width (first @(:size console))]
    (terminal/write-reverse (:screen console) 0 0 (pad-line width "Mud Client v0.0.0"))
    (terminal/write-reverse (:screen console) 0 (- (second @(:size console)) 2)
                   (pad-line width "READY"))
    (terminal/write (:screen console) 0 (- (second @(:size console)) 1) ">")))

(defn writeln-console
  [console line]
  (let [row-this (:row @(:write-cursor console))
        rows-total (- (second @(:size console)) 3)]
    (update-console-chrome console)
    (if (= rows-total row-this)
      (do
        (scroll-console console)
        (terminal/write-string (:screen console) 0 (:row @(:write-cursor console)) line))
      (do
        (reset! (:write-cursor console) {:row (inc row-this) :column 0})
        (terminal/write-string (:screen console) 0 (:row @(:write-cursor console)) line)))
    (terminal/refresh (:screen console))))

(defn handle-resize-console
  [console size]
  (reset! (:size console) size)
  (update-console-chrome console))

(defn create-console
  []
  (let [screen-this (terminal/create)
        console-this {:screen screen-this
                      :write-cursor (atom {:row 0 :column 0})
                      :size (atom [80 40])}]
    (terminal/add-resize-handler screen-this
                        (fn [_ size]
                          (handle-resize-console console-this size)))
    (reset! (:size console-this) (terminal/size (:screen console-this)))
    (terminal/move-cursor screen-this 2 (- (second @(:size console-this)) 1))
    (update-console-chrome console-this)
    console-this))

(defn handle-unbound-key
  [console keystroke]
  (swap! (:input-buffer console) conj (.getCharacter keystroke))
  (terminal/write (:screen console) 2 (- (second @(:size console)) 1)
              (apply str @(:input-buffer console)))
  (terminal/move-cursor (:screen console)
                    (+ 2 (count @(:input-buffer console)))
                    (- (second @(:size console)) 1))
  (terminal/refresh (:screen console)))

(defn handle-pending-input
  [console]
  (async/go (loop []
              (when-let [keystroke (async/<! (:input-channel console))]
                (let [keybinding ((:keybindings console) keystroke)]
                  (if (not (nil? keybinding))
                    ((:handler keybinding) console)
                    (handle-unbound-key console keystroke)))
                (recur)))))

(defn create-interactive-console
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
    (terminal/refresh (:screen console-out))
    console-out))

(defn dispose-console
  [console]
  (dosync (ref-set (:handle-input console) false))
  (async/close! (:input-channel console))
  (terminal/dispose (:screen console)))

(defn erase-previous-character
  [console]
  (swap! (:input-buffer console) #(vec (reverse (rest (reverse %1)))))
  (terminal/write (:screen console) 2 (- (second @(:size console)) 1)
              (pad-line (first @(:size console)) (apply str @(:input-buffer console))))
  (terminal/move-cursor (:screen console)
                    (+ 2 (count @(:input-buffer console)))
                    (- (second @(:size console)) 1))
  (terminal/refresh (:screen console)))

(defn clear-input-buffer
  [console]
  (reset! (:input-buffer console) []))

(defn clear-input-area
  [console]
  (reset! (:input-buffer console) [])
  (terminal/write (:screen console) 2 (- (second @(:size console)) 1)
              (pad-line (first @(:size console)) " "))
  (terminal/move-cursor (:screen console) 2 (- (second @(:size console)) 1))
  (terminal/refresh (:screen console)))

(defn break-writeln-console
  [console line]
  (doseq [line-this (break-lines (first @(:size console)) 0 line)]
    (writeln-console console line-this)))
