(ns cmiles74.client.keybinding
  (:require [cmiles74.client.console :as console])
  (:import [com.googlecode.lanterna.input KeyStroke]))

(defn vim-keystroke
  "Returns the KeyStroke that matches the given Vim description string."
  [vim-description]
  (KeyStroke/fromString vim-description))

(def DEFAULT-KEYBINDINGS
  {(vim-keystroke "<BS>")
   {:description "Erase the previous chacter"
    :handler console/erase-previous-character}})
