(ns com.nervestaple.mud.console.interface
  (:require
   [com.nervestaple.mud.console.core :as core]))

(defn break-lines
  "Returns a sequence of lines, each of the provided width.."
  [width position text]
  (core/break-lines width position text))

(defn pad-line
  "Returns a new string padded out with spaces to match the supplied width."
  [width text]
  (core/pad-line width text))

(defn clear-console
  "Clears the console display."
  [console]
  (core/clear-console console))

(defn scroll-console
  "Scrolls the middle of the console up one line."
  [console]
  (core/scroll-console console))

(defn update-console-chrome
  "Updates the 'chrome' or non-scrolling content of the console."
  [console]
  (core/update-console-chrome console))

(defn writeln-console
  "Writes the provided line of text to the scrolling content area of the console."
  [console line]
  (core/writeln-console console line))

(defn handle-resize-console
  "Handles updating the console display upon resize events."
  [console size]
  (core/handle-resize-console console size))

(defn create-console
  "Returns a new map of console data. This map contains the following
  keys: :screen, :write-cursor, :size. It creates a new terminal and initializes
  the console. The :screen key in the map returns a reference to this consoles
  terminal instance."
  []
  (core/create-console))

(defn handle-unbound-key
  "Handles unbounded keystrokes from the console by writing them to the screen."
  [console keystroke]
  (core/handle-unbound-key console keystroke))

(defn handle-pending-input
  "Reads new key strokes off the input channel and handles them on
  behalf of the application."
  [console]
  (core/handle-pending-input console))

(defn create-interactive-console
  "Creates a new console that can also handle keyboard input and communication
  with the server websocket stream. It returns a map of console data. This map
  contains the following keys: :screen, :write-cursor, :size."
  [keybindings server-socket]
  (core/create-interactive-console keybindings server-socket))

(defn dispose-console
  "Releases a console and reclaims any associated resources."
  [console]
  (core/dispose-console console))

(defn erase-previous-character
  [console]
  (core/erase-previous-character console))

(defn clear-input-buffer
  [console]
  (core/clear-input-buffer console))

(defn clear-input-area
  [console]
  (core/clear-input-area console))

(defn break-writeln-console
  "Breaks the provided line of text into lines that match the width of
  the console and then write then all to the scrollable content area of
  the console."
  [console line]
  (core/break-writeln-console console line))
