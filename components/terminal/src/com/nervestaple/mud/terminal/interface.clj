(ns com.nervestaple.mud.terminal.interface
  (:require
   [com.nervestaple.mud.terminal.core :as core]))

(defn create
  "Instantiates a new terminal and returns a map with references to the
  console and related data structures."
  []
  (core/create))

(defn dispose
  "Releases the screen instance and reclaims any associated resources."
  [screen]
  (core/dispose screen))

(defn clear
  "Clears the screen of the console."
  [screen]
  (core/clear screen))

(defn refresh
  "Refreshes the consoles screen. Any content in the backing buffer
  will be displayed."
  [screen]
  (core/refresh screen))

(defn size
  "Returns a sequences with the size of the screen, [columns, rows]."
  [screen]
  (core/size screen))

(defn move-cursor
  "Moves the cursor of the screen to the specified coordinate."
  [screen column row]
  (core/move-cursor screen column row))

(defn write-char
  "Writes a character to the screen at the specified coordinate."
  [screen column row character]
  (core/write-char screen column row character))

(defn write-string
  "Writes a segment of text to the screen starting at the specified
  coordinate."
  [screen column row text]
  (core/write-string screen column row text))

(defn scroll
  "Scrolls the screen lines between start and finish inclusive the
  specified number of lines. Negative numbers will add blank rows to the
  top, positive lines values will add blank rows to the bottom."
  [screen start finish lines]
  (core/scroll screen start finish lines))

(defn scroll-up
  "Scrolls the screen up one line."
  [screen start finish]
  (core/scroll-up screen start finish))

(defn write
  "Writes the provided text at the specified position."
  [screen column row text]
  (core/write screen column row text))

(defn write-reverse
  "Writes the provided text at the specified position with the
  foreground and background reversed."
  [screen column row text]
  (core/write-reverse screen column row text))

(defn add-resize-handler
  "Adds the provided function as a resize handler to the provided
  screen. When the screen's underlying terminal changes size, this
  function will be called with two arguments: the screen and a sequence
  with the new size, [columns, rows]."
  [screen resize-fn]
  (core/add-resize-handler screen resize-fn))
