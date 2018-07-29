MUD - Multi User Dungeon
==============================

I don't have a lot of free time, but when I do, I spend it on reimplementing
early 1990s era technology. The project aims to provide a MUD server and a
command line client, both of which are easy to extend with additional features.

While it has been many years since I actively participated in a MUD, I do miss
the odd mixture of gaming and chatting that was a hallmark of the genre. This
may not appeal to many, but it's enough to keep me busy on vacation or during
long weekends.

This project is far from finished and it's not at the point where there's a real
game to play, but some things are working. The server runs, clients can connect
and talk to each other, and movement has been implemented.

![Screenshot](https://raw.githubusercontent.com/cmiles74/mud/master/documentation/screenshot.png)

Developing
------------

This project is implemented in Clojure, right now everything is stored in
memory. At this point my thinking is that I wait on coding up persistence until
I have a better idea of what the data structures look like (what is the best way
to model a room, account, inventory, etc.)

This is a personal project, I work on it over the weekend sometimes or while I'm
on vacation. It's progressing pretty slowly but every so often I find some time
and add more features.

How Does It Work?
-------------------

A lot of the MUD implementations out there use their own wacky network protocol.
In contrast, this project uses
[websockets](https://en.wikipedia.org/wiki/WebSocket) and
[JSON](https://en.wikipedia.org/wiki/JSON) for communication. I was interested
in what it might take to code up a console based Java application, so that's
what the current client uses. If you start the client up in "headless" mode, it
will work with most any terminal, if you start it up regular style it will use a
Swing based terminal which is kind of okay but has some quirks.

Developing with Docker
---------------------------

The easiest way to get started is to spin up a docker instance with the included
script, it's in the root of the project directory.

    ./docker-run
    
This will download my Emacs/Javascript/Clojure image, map in the project files
and bring up a new Emacs window. From there you can browse through the source
code and start up a new REPL session with Cider (Control-c, Meta-j).

Right now Emacs in the Docker image pins the Clojure tools to the version in
MELPA stable. Most of the time this is good, but there can be problems when some
of these tools are newer then others. If you run into problems, open the
Spacemacs configuration at `/developer/.spacemacs` and comment out the version
pinning for these tools and then restart. 90% of the time, this will fix the
issue.

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
machine for new client connections. You can also use the script `start-server`.

Starting up the client is similar...
    
    java -Djava.awt.headless=true -jar target/mud-0.1-SNAPSHOT-standalone.jar
    
Here we are telling Java to start up in "headless" mode. If we don't specify
headless we will be provided with a Swing based terminal. This is nice when
testing but it doesn't behave entirely like a real terminal session. You can
also use the script `start-client-local`.

To use a different port, you can copy the sample configuration files and edit
them. When you start the client and server, pass in the "-c" flag and the path
to your configuration file.

    java -classpath target/mud-0.1-SNAPSHOT-standalone.jar cmiles74.mud.server.cli \
    -c mud-server.yml
    
You can call either one with the "-h" flag to see a list of options, right now
setting the configuration file is the only one.

Test Server
------------

There's a public test server running at http://mud.nervestaple.com, it also
supports SSL. If you run the client without specifying a configuration file or
with the `start-client` script, it will connect to this test server.
