# Changelog

Notable changes will be documented in this file.

## [Unreleased]
## [0.3.1]
### Added 
- Filter tags messages that are not related to MongoDB as "_mongoguardium_skip_not_mongodb" (not removed from pipeline).

### Fixed
- Filter plugin now skips and removes messages/events that are not authCheck and authenticate from logstash pipeline, to prevent unnecessary JSON parse errors.

## [0.3.0]
###  Added
- Support for IPv6

### Changed
- Edited README.md to better specify required fields for Logstash Filter
- Classes Construct, Sentence, SentenceObject moved into com.ibm.guardium.connector.structures package. 


## [0.2.0]
- Add support for Authentication and Authorization errors.

## [0.0.1]
- Initial Java filter plugin to parse mongodb syslog messages into Construct JSON. 
  - Using google/gson