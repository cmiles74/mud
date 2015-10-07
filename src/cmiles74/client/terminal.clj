(ns cmiles74.client.terminal
  (:require
   [taoensso.timbre :as timbre
    :refer (log  trace  debug  info  warn  error  fatal  report
                 logf tracef debugf infof warnf errorf fatalf reportf
                 spy get-env log-env)]
   [taoensso.timbre.appenders.core :as appenders]
   [taoensso.timbre.profiling :as profiling
    :refer (pspy pspy* profile defnp p p*)]
   [slingshot.slingshot :only [throw+ try+]])
  (:import
   [com.googlecode.lanterna.terminal DefaultTerminalFactory]
   [com.googlecode.lanterna.screen TerminalScreen]
   [com.googlecode.lanterna TextCharacter]
   [com.googlecode.lanterna TerminalPosition]
   [com.googlecode.lanterna TerminalSize]
   [com.googlecode.lanterna.terminal SimpleTerminalResizeListener]
   [com.googlecode.lanterna.screen Screen]
   [com.googlecode.lanterna SGR]))

(defn create
  "Instantiates a new terminal and returns a map with references to the
  console and related data structures."
  []
  (let [terminal-factory (DefaultTerminalFactory.)
        terminal (.createTerminal terminal-factory)
        screen (TerminalScreen. terminal)]
    (.startScreen screen)
    (.clear screen)
    (.refresh screen)
    screen))

(defn dispose
  "Releases the screen instance and reclaims any associated resources."
  [screen]
  (.stopScreen screen))

(defn clear
  "Clears the screen of the console."
  [screen]
  (.clear screen))

(defn refresh
  "Refreshes the console's screen. Any content in the backing buffer
  will be displayed."
  [screen]
  (.refresh screen))

(defn size
  "Returns a sequences with the size of the screen, [columns, rows]."
  [screen]
  (let [terminal (.getTerminal screen)
        size-this (.getTerminalSize terminal)]
    (if (and (not (nil? (.getColumns size-this))) (not (nil? (.getRows size-this))))
      [(.getColumns size-this) (.getRows size-this)]
      [80 24])))

(defn move-cursor
  "Moves the cursor of the screen to the specified coordinate."
  [screen column row]
  (.setCursorPosition screen (TerminalPosition. column row)))

(defn write-char
  "Writes a character to the screen at the specified coordinate."
  [screen column row character]
  (.setCharacter screen column row (TextCharacter. character)))

(defn write-string
  "Writes a segment of text to the screen starting at the specified
  coordinate."
  [screen column row text]
  (let [text-graphics (.newTextGraphics screen)]
    (.putString text-graphics column row text)))

(defn scroll
  "Scrolls the screen lines between start and finish inclusive the
  specified number of lines. Negative numbers will add blank rows to the
  top, positive lines values will add blank rows to the bottom."
  [screen start finish lines]
  (.scrollLines screen start finish lines))

(defn scroll-up
  "Scrolls the screen up one line."
  [screen start finish]
  (scroll screen start finish 1))

(defn write
  "Writes the provided text at the specified position."
  [screen column row text]
  (let [text-graphics (.newTextGraphics screen)]
    (.putString text-graphics column row text)))

(defn write-reverse
  "Writes the provided text at the specified position with the
  foreground and background reversed."
  [screen column row text]
  (let [text-graphics (.newTextGraphics screen)]
    (.enableModifiers text-graphics (into-array [SGR/REVERSE]))
    (.putString text-graphics column row text)))

(defn move-cursor
  "Moves the cursor to the specified position for the supplied screen."
  [screen column row]
  (.setCursorPosition screen (TerminalPosition. column row)))

(defn add-resize-handler
  "Adds the provided function as a resize handler to the provided
  screen. When the screen's underlying terminal changes size, this
  fuction will be called with two arguments: the screen and a sequence
  with the new size, [columns, rows]."
  [screen resize-fn]
  (.addResizeListener (.getTerminal screen)
                      (proxy [SimpleTerminalResizeListener] [(TerminalSize. 80 24)]
                        (onResized [terminal new-size]
                          (resize-fn screen [(.getColumns new-size) (.getRows new-size)])))))
