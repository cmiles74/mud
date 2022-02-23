(ns com.nervestaple.mud.client.keybinding
  (:require
   [com.nervestaple.mud.console.interface :as console])
  (:import
   [com.googlecode.lanterna.input KeyStroke]))

(defn vim-keystroke
  "Returns the KeyStroke that matches the given Vim description string."
  [vim-description]
  (KeyStroke/fromString vim-description))

(def DEFAULT-KEYBINDINGS
  "Map of default keybindings."
  {(vim-keystroke "<BS>")
   {:description "Erase the previous character"
    :handler console/erase-previous-character}})
