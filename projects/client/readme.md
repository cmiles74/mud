# Client

This project provides the console client for interacting with the Mud server. It
provides a friendly terminal based user interface (TUI) that makes it easy to
connect and communicate with other players.

The client works well in most consoles under Linux and MacOS, it is flaky under
Windows. If you need to run the client in a Windows environment you may use the
emulated Swing based console.

## Building and Running

From this directory, run the following to build a standalone JAR file with all
of the server code and dependencies.

```shell
$ clj -X:uberjar
```

The JAR file will be created and you may then invoke it from the terminal. The
command below will display the usage information for the tool.

```shell
$ java -jar mud-client.jar -h
```

If you're running under Windows you may use the Swing-based emulated console. 
Simply launch with `javaw`.

```shell
$ javaw -jar mud-client.jar -h
```

A sample configuration file is provided. You may copy the `config.edn.sample`
file to `config.edn` and then customize the entries to match your requirements.

The application can be started from the terminal by invoking the JAR without any
arguments.

```shell
$ java -jar mud-client.jar
```


