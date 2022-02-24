# Terminal

This component provides functions that make it easier to provide a terminal
based user agent. This component leverages the [Lanterna][lanterna] library to
drive the console-based interface.

* [Lanterna][lanterna]

Lanterna provides good support for terminal emulators under Linux or MacOS. Some
work has been done to support Windows-based consoles but that work isn't far
enough along to support this project. Lanterna provides a Swing-based emulated
terminal that may be used under Windows.

While Lanterna provides a GUI toolkit, we aren't using that in this project.
Instead this component provides the functions needed to develop the console
client.

You can initialize a new terminal with the create function.

```clojure
(ns com.nervestaple.project
  (:require [com.nervestaple.mud.terminal.interface :as terminal]))
  
(def ts (terminal/create))
```

A Java [`TerminalScreen`][termscreen] instance will be returned. This may be
passed into the rest of the provided functions in order to manipulate the
display.

```clojure
(terminal/move-cursor ts 0 0)
(terminal/write-string ts "Hello world!")
(terminal/move-cursor ts 0 2)
(terminal/refresh ts)
```

All of the drawing commands will make changes to a backing buffer, calling
`refresh` will merge the backing buffer with the currently displayed content.

----
[lanterna][https://github.com/mabe02/lanterna]
[termscreen][http://mabe02.github.io/lanterna/apidocs/3.1/com/googlecode/lanterna/screen/TerminalScreen.html]
