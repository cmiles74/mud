# Mud: (Another) Multi-User Dungeon

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

## Developing

This project is written in Clojure, we're using the Polylith tool to manage
dependencies, building, etc.

### Clojure Command Line Tools

Even though their kind of meh and not strong on the cross-platform front, we're
using the Clojure Command Line tool for this project. You will need to get this
tool installed and working, [documentation for MacOS and Linux is available on
the Clojure website](ccl-tool). If you're on Windows you'll need to suffer with
the maybe someday supported, similar tool [CLJ-Windows][cclw-tool] that runs
under Powershell. I am using Windows myself and have found the Windows tool to
be good enough. :wink:

* [Clojure Command Line Tool][ccl-tool]
* [Clojure Powershell Windows Tool][cclw-tool]

### Polylith Tool

The Polylith documentation can be found here:

- The [high-level documentation](https://polylith.gitbook.io/polylith)
- The [Polylith Tool documentation](https://github.com/polyfy/polylith)
- The [RealWorld example app documentation](https://github.com/furkan3ayraktar/clojure-polylith-realworld-example-app)

You can also get in touch with the Polylith Team via our [forum](https://polylith.freeflarum.com) or on [Slack](https://clojurians.slack.com/archives/C013B7MQHJQ).

[ccl-tool]: https://clojure.org/guides/getting_started
[cclw-tool]: https://github.com/clojure/tools.deps.alpha/wiki/clj-on-Windows
