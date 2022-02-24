(ns com.nervestaple.mud.terminal.core
  (:import
   [com.googlecode.lanterna.terminal DefaultTerminalFactory]
   [com.googlecode.lanterna.screen TerminalScreen]
   [com.googlecode.lanterna TextCharacter]
   [com.googlecode.lanterna TerminalPosition]
   [com.googlecode.lanterna TerminalSize]
   [com.googlecode.lanterna.terminal SimpleTerminalResizeListener]
   [com.googlecode.lanterna SGR]))

(defn create
  []
  (let [terminal-factory (DefaultTerminalFactory.)
        terminal (.createTerminal terminal-factory)
        screen (TerminalScreen. terminal)]
    (.startScreen screen)
    (.clear screen)
    (.refresh screen)
    screen))

(defn dispose
  [screen]
  (.stopScreen screen))

(defn clear
  [screen]
  (.clear screen))

(defn refresh
  [screen]
  (.refresh screen))

(defn size
  [screen]
  (let [terminal (.getTerminal screen)
        size-this (.getTerminalSize terminal)]
    (if (and (not (nil? (.getColumns size-this))) (not (nil? (.getRows size-this))))
      [(.getColumns size-this) (.getRows size-this)]
      [80 24])))

(defn move-cursor
  [screen column row]
  (.setCursorPosition screen (TerminalPosition. column row)))

(defn cursor-position
  [screen]
  (let [cursor-position (.getCursorPosition screen)]
    [(.getColumn cursor-position) (.getRow cursor-position)]))

(defn write-char
  [screen column row character]
  (.setCharacter screen column row (TextCharacter. character)))

(defn write-string
  [screen column row text]
  (let [text-graphics (.newTextGraphics screen)]
    (.putString text-graphics column row text)))

(defn scroll
  [screen start finish lines]
  (.scrollLines screen start finish lines))

(defn scroll-up
  [screen start finish]
  (scroll screen start finish 1))

(defn write
  [screen column row text]
  (let [text-graphics (.newTextGraphics screen)]
    (.putString text-graphics column row text)))

(defn write-reverse
  [screen column row text]
  (let [text-graphics (.newTextGraphics screen)]
    (.enableModifiers text-graphics (into-array [SGR/REVERSE]))
    (.putString text-graphics column row text)))

(defn add-resize-handler
  [screen resize-fn]
  (.addResizeListener (.getTerminal screen)
                      (proxy [SimpleTerminalResizeListener] [(TerminalSize. 80 24)]
                        (onResized [terminal new-size]
                          (resize-fn screen [(.getColumns new-size)
                                             (.getRows new-size)])))))
