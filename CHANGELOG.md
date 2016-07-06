# Changelog

## [2.1.2] - UNRELEASED

### Fixed
* Fixed some `toString` warnings. Thanks [@jpellerin](https://github.com/jpellerin)!


## [2.1.1] - 2016-05-17

### Fixed
* Fixed bug in encoding of events that caused the `alert_type` field to be dropped. Thanks [@jpellerin](https://github.com/jpellerin)!

## [2.1.0] - 2016-04-25

### Added
* DogStatsD client has new `event` and `serviceCheck` methods.
* `DogStatsDClient.SERVICE_CHECK_*`, `DogStatsDClient.EVENT_PRIORITY_*`, and `DogStatsDClient.EVENT_ALERT_TYPE_*` added to support the aforementioned new methods.

### Changed
* Under the hood, the metrics supplied are now represented as things like `GaugeMetric`, etc. A number of traits are used to simplify logic in choosing how to encode these metric types for output. This is transparent to the end user, as the client interfaces are unchanged.
* There is no longer a `floatFormat` parameter for the clients. It's unlikely you used this anyway.
* With the number formatting now contained in a single thread, the use of a DecimalFormatter allows for better looking numeric output, meaning we don't spew `.000000` unnecessarily on things, saving some bytes.

### Fixed
* Corrected some minor documentation nits.

## [2.0.4] - 2016-04-25

### Fixed
* Swallow exceptions that would otherwise cause no more metrics to be sent. Thanks [bkirwi](https://github.com/bkirwi)!

## [2.0.2] - 2016-03-23

### Changed
* The Datagram socket is now lazy, so it will not be created until first use. Fixes #7.

## [2.0.1] - 2016-03-23

### Changed
* Use of prefixes now makes use of `StringBuilder` instead of Scala string interpolation.
* Clients may now be instantiated with a `maxQueueSize` to prevent unbounded growth of metrics. Thanks [tixxit](https://github.com/tixxit)!
* Minor documentation improvements.
* Upgrade to Scala 2.11.8 and SBT 0.13.11
* Use `take` when polling in async mode, since it's the only thing the thread does.

## Added
* Added scalastyle rules and cleaned up a few things as a result.

## [2.0.0] - 2016-03-01

### Changed
* Move packages around to make publishing in non-github-repo-of-gphat's easier. The package is now `github.gphat`. Thanks [tixxit](https://github.com/tixxit)!
* Use a BlockingQueue to simplify the asynchronous mode. Thanks [tixxit](https://github.com/tixxit)!
* Speed up metric emission by using a `StringBuilder` instead of Scala string interpolation. Thanks [tixxit](https://github.com/tixxit)!
* Removed the `histogram` method from the `StatsDClient` as it's not a supported metric type.

# Fixed
* Datadog histograms and timers now emit sample rates. Previously they did not, which was a bug!
* Small doc fixes.

## [1.0.2] - 2016-02-22

## Fixed
* Stopped `println`ing every Metric send via UDP. So chatty!
* Ahem… so client-side sampling wasn't really working. Sampling now works 100% better. There's a test to prove it!

## Added
* Ability to bypass the client-side sampler, but still send a sample rate. This is useful for folks doing their own sampler.

## [1.0.1] - 2016-02-20

### Fixed
* Double values being emitted as scientific notation and therefore being invalid StatsD datagrams
* Shutdown client in test suite so unclosed DatagramSockets don't pile up
* Adjust DogStatsD encoder to only add pipe delimiters when necessary

### Added
* Test to ensure DogStatsD datagrams are correctly formatted. Fixes #3
* Added optional prefix argument for clients to add to the names of metrics. Fixes #1

## [1.0.0] - 2016-02-11

* First release!
