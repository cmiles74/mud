# Configuration

This component provides functions that make it easier to load configuration 
information in the [EDN][edn] format from resource files as well as locations on
disk. There are also functions that make it easy to combine this configuration
data with environment variables, letting you easily customize runtime behavior.

## Usage

Here's a simple example...
* We try to load the resource file "config.edn" (it needs to be a map)
* We check to see if a file has been provided and we load that, otherwise we
  load "config.edn" from the current directory (it needs to be a map)
* We check the environment for the two variables "APP_HOST" and "APP_PORT", if
  the variables aren't present in the environment then they are ignored (we used
  these to create a map)
  
These maps are then merged together (top to bottom with the bottom taking
precendence) and returned.

```clojure
(defn load-config
  [file-path]
  (config/read-config "config.edn"
    (or file-path "config.edn")
    ["APP_HOST"
     "APP_PORT"]))
```

[edn]: https://github.com/edn-format/edn
