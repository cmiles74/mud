(ns com.nervestaple.mud.config.interface
  (:require
   [com.nervestaple.mud.config.core :as core]))

(defn read-from-resource
  "Reads and parses an resource file from the provided path and returns the
  results. If the resource-path doesn't exist or cannot be read, an empty map is
  returned."
  [resource-path]
  (core/read-from-resource resource-path))

(defn read-from-file
  "Reads and parses a file from the provided path and returns the results. If the
  file-path doesn't exist or cannot be read, an empty map is returned."
  [file-path]
  (core/read-from-file file-path))

(defn read-from-environment
  "Maps over the items in variable-list and fetches the matching value from the
  current environment, returning a map with the variables and their values. The
  keys in the map are lower-cased keywords, the variable \"LOCAL_USER\" will
  yield the key \":local_user\" in the map returned.

  On UNIX-like operating systems, the case of the variable needs to match the
  case of the environment variable in order to match. On other operating
  systems, like Windows, casing is ignored."
  [variable-list]
  (core/read-from-environment variable-list))

(defn read-config
  "Accepts three parameters, each a location for fetching configuration data: a
  path to a resource file, a path to a file and a list of environment variables.
  Each of these is read and parsed (if present) and the results are merged into
  a single map. Environment variables take the highest precedence followed by
  data in the configuration file and, lastly, data read from the resource path."
  [resource-path file-path variable-list]
  (core/read-config resource-path file-path variable-list))
