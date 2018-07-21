MUD - Multi User Dungeon
==============================

I don't have a lot of free time, but when I do, I spend it on reimplementing
early 1990s era technology. The project aims to provide a MUD server and a
command line client, both of which are easy to extend with additional features.

While it has been many years since I actively participated in a MUD, I do miss
the odd mixture of gaming and chatting that was a hallmark of the genre. This
may not appeal to many, but it's enough to keep me busy on vacation or during
long weekends.

Current Status
-----------------

Right now we have a client and a server, both compile and run cleanly. The
server can accept connections from multiple clients and clients can connect.
When a client sends data to the server, it's broadcast to all connected clients.

As for the fun parts, none of that stuff has been implemented yet.

Developing
------------

This project is implemented in Clojure and (tentatively) backed by RethinkDB.
You can get an idea for how slowly this project is progressing by the fact that
when I started, RethinkDB as both promising and new.

Developing with Docker
---------------------------

The easiest way to get started is to spin up a docker instance with the included
script, it's in the root of the project directory.

    ./docker-run
    
This will download my handle Emacs/Javascript/Clojure image, map in the project
files and bring up a new Emacs window. From there you can browse through the
source code and start up a new REPL session with Cider (Control-c, Meta-j).

Building
----------

We use Leiningen to manage this project. To build the project, invoke "lein".

    lein uberjar
    
This will pull down all of the dependencies, compile the project and assemble
everything into one large JAR file.

Running
---------

The standalone JAR file in the project directory contains the code for both the
client and the server. It's built to startup the client, to start the server we
need to pass in the name of the server bootstrap class.

    java -classpath target/mud-0.1-SNAPSHOT-standalone.jar cmiles74.mud.server.cli 
    
This will start up the server, it will listen on port 18083 on your local
machine for new client connections.

Starting up the client is similar...
    
    java -Djava.awt.headless=true -jar target/mud-0.1-SNAPSHOT-standalone.jar
    
Here we are telling Java to start up in "headless" mode. If we don't specify
headless we will be provided with a Swing based terminal. This is nice when
testing but it doesn't behave entirely like a real terminal session.

To use a different port, you can copy the sample configuration files and edit
them. When you start the client and server, pass in the "-c" flag and the path
to your configuration file.

    java -classpath target/mud-0.1-SNAPSHOT-standalone.jar cmiles74.mud.server.cli \
    -c mud-server.yml
    
You can call either one with the "-h" flag to see a list of options, right now
setting the configuration file is the only one.
