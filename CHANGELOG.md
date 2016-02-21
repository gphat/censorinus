# Changelog

## [1.0.1] - Unreleased

### Fixed
* Double values being emitted as scientific notation and therefore being invalid StatsD datagrams
* Shutdown client in test suite so unclosed DatagramSockets don't pile up
* Adjust DogStatsD encoder to only add pipe delimiters when necessary

### Added
* Test to ensure DogStatsD datagrams are correctly formatted. Fixes #3
* Added optional prefix argument for clients to add to the names of metrics. Fixes #1

## [1.0.0] - 2016-02-11

* First release!
