# Logging

This component provides functions for setting the log level, logging messages 
and directing those log messages to files on disk. It's a thin veneer over the
backing logging library and provides one location for all of our log handling 
code.

## Usage

Logging messages is as easy as...

```clojure
(log/debug "There's a bug that needs debugging!")
(log/info "Bad input from the launch system:" launch-system)
(log/warn "Internal detonation in ten minutes from this mark!")
```

If you need to write you logs to a file, we have a function for that!

```clojure
(log/add-file "path-to-log-file.log")
```

You may add the same path multiple times, we don't have a function for removing
a path from the current configuration.
