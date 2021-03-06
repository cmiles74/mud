(ns cmiles74.mud.client.keybinding
  "Functions for managing keybindings"
  (:require [cmiles74.mud.client.console :as console])
  (:import [com.googlecode.lanterna.input KeyStroke]))

(defn vim-keystroke
  "Returns the KeyStroke that matches the given Vim description string."
  [vim-description]
  (KeyStroke/fromString vim-description))

;; map of default keybindings
(def DEFAULT-KEYBINDINGS
  {(vim-keystroke "<BS>")
   {:description "Erase the previous chacter"
    :handler console/erase-previous-character}})
