# Changelog

## [2.1.14] - Pending

* Add test for handling floating point timer encoding
* Remove unnecessary deps and move some to test target. Thanks [travisbrown](https://github.com/travisbrown)!

## [2.1.13] - 2018-04-01

* Clients may specify a `maxBatchSize` to allow batching multiple metrics, separated by newlines. Be sure and choose a value smaller than the relevant MTU! Thanks [tixxit](https://github.com/tixxit)!
* Clean up some linter warnings and such.

## [2.1.12] - 2018-03-22

* Fixed constant DEFAULT_VALID_METRIC_REGEX to allow 0-9

## [2.1.11] - 2018-03-20

### Changed
* Add constant `github.gphat.censorinus.DogStatsDClient.DEFAULT_VALID_METRIC_REGEX` for use with metricRegex option

## [2.1.10] - 2018-03-20

### Changed
* Decrease allocations when sending metrics. Thanks [@johnynek](https://github.com/johnynek)

## [2.1.7] - 2018-03-19

### Changed
* Add metricRegex as optional arg to clients for converting incoming metric names

## [2.1.6] - 2017-06-05

### Changed
* Publish for Scala 2.12 and bump version deps.
* Removed Scala 2.10 from targets.

## [2.1.4] - 2016-10-19

## Fixed
* Newlines in events and service check are now properly escaped.

## [2.1.3] - 2016-07-28

### Changed
* Exceptions that could've been emitted by ending UDP datagrams are now swallowed by default.
* Added `allowExceptions` as a client instantiation option to control exception handling.
* "Invalid" gauge values `Double.NaN` as well as infinite values are now silently dropped.

## [2.1.2] - 2016-07-06

### Fixed
* Fixed some `toString` warnings. Thanks [@jpellerin](https://github.com/jpellerin)!
* Improved docs for asynchronous features.

### Changed
* Updated error messages for when `maxQueueSize` is hit.
* Cleaned up code around emptying queue to be simpler.

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
