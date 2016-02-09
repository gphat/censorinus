Censorinus is a StatsD client with multiple personalities.

# Features

* Asynchronous

# Example

```scala
import github.gphat.censorinus.Client

val c = new Client()
```

# Asynchronous

Metrics are locally queued up and emptied out periodically. By default any
pending metrics are emptied out every 100ms. You can change this to another
delay:

```scala
val c = new Client(flushInterval = 50)
```
