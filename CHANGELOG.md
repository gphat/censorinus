# Changelog

## [1.0.2] - Unreleased

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
