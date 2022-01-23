# Server

This project provides the server for the project. Once you have a Mud server
up-and-running, clients can connect and interact with the dungeon and each
other.


## Building and Running

From this directory, run the following to build a standalone JAR file with all
of the server code and dependencies.

```shell
$ cd projects/server
$ clj -X:uberjar
```

The JAR file will be created and you may then invoke it from the terminal. The
command below will display the usage information for the tool.

```shell
$ java -jar mud-server.jar -h
```

A sample configuration file is provided. You may copy the `config.edn.sample`
file to `config.edn` and then customize the entries to match your requirements.

The application can be started from the terminal by invoking the JAR without any
arguments.

```shell
$ java -jar mud-server.jar
