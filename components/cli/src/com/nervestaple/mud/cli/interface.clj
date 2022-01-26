(ns com.nervestaple.mud.cli.interface
  (:require
    [com.nervestaple.mud.cli.core :as core]))

;; exit codes
(def SUCCESS core/SUCCESS)
(def WARN core/WARN)

(defn error-msg
  "Accepts a sequence of strings. Returns a string by appending a generic error
  message and then joining the sequence together with new lines between each
  one."
  [errors]
  (core/error-msg errors))

(defn exit
  "Accepts an exit status and a sequence of strings. Joins the strings with new
  lines and displays the provided message, then exits the runtime with the given
  status."
  [status message]
  (core/exit status (error-msg message)))

(defn parse-cli-args
  "This function does several thins in order to make it easier to integrate a
  command line interface with your application.

  - parse any command line options and arguments
  - display usage text
  - facilitate validation of options and arguments

  The `usage-text` variable should be a string describing your application. A
  vector of command line options should be provided with the `options` argument.
  The `validate-options-fn` should be a function that accepts two arguments (a
  map of the parsed options and a sequence of arguments) and returns a sequence
  of error messages or nil if there are no errors. The `handle-args-fn` should
  accept two arguments (a map of the parsed options and a sequence of
  arguments), it will then be responsible for doing your application's work.
  Lastly, the `args` should be a sequence of the command line arguments, for
  instance what you received from a call to your `-main` method."
  [usage-text options validate-options-fn handle-args-fn args]
  (core/parse-cli-args usage-text
                       options
                       validate-options-fn
                       handle-args-fn
                       args))

