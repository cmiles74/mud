# Command Line Interface

This component provides functions to make configuring the command line interface
for the application easier. It uses the [Clojure tools.cli][clojure-tools-cli]
library to parse out command line arguments.

* [Clojure tools.cli][clojure-tools-cli]

## Usage

The general idea is that we break-up the different pieces of the command line
interface into a usage message that describes how the tool works and a list of
options that the tool accepts.

```clojure
(def cli-usage
  (->> ["Sample Command Line Tool"
        ""
        "This provides a sample command line tool. Enjoy!"
        ""
        "Usage: [options] command"
        ""
        "Command may be one of the following:"
        ""
        "  hello   - Display hello message"
        "  goodbye - Display goodbye message"
        ""
        "Options:"]
        (string/join \newline)))

(def cli-options
  [["-?" "--help" "Display usage information"]
   ["-c" "--config FILE-PATH" "Specify configuration file"
    :default "config.edn"]])
```

Next code up a function that accepts the incoming list of options and arguments
and emits a sequence of error messages. If messages are returned then they are
displayed on the console and the tool exits with an error status. If nil is
returned then processing continues.

The text in the long version of the option (the "config" in "--config") from the
vector of options in the `cli-options` function will be used as a key to get the
matching option value.

```clojure
(def cli-validate
  [options arguments]
  (seq (keep identity
             [(when (and (:config options) (= "badfile.edn" (:config options)))
              "You may not use 'badfile.edn' for this application")]
             [(when (!= 1 (count arguments))
              "Only one command may be provided")]
             [(when (not (or (= "hello" (first arguments))
                             (= "goodbye" (first arguments))))
               "That is not a valid command")])))
```

Now you are free to code up a function that accepts the parsed set of options
and arguments and do the actual work of your tool!

```clojure
(defn cli-handle-arguments
  [options arguments]
  (let [command (first arguments)
        config (edn/read (slurp (:config options)))]
    (cond (= "hello" command)
          (println "Hi there!")
          
          (= "goodbye")
          (println "Goodbye my friend!"))))
```

With all that work out of the way, you're free to code up your main method to
provide a real, live command line experience!

```clojure
(defn -main
  [& args]
  (cli/parse-cli-args cli-usage
                      cli-options
                      cli-validate
                      cli-handle-arguments))
```

[clojure-tools-cli]: https://github.com/clojure/tools.cli
