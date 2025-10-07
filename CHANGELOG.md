# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.2] - 2025-10-07

### Fixed
- Fix Spring AOP integration issues that caused AOP features to be bypassed
  - Fixed issue where `@Transactional`, `@Cacheable`, `@PreAuthorize` and other AOP annotations were not working when called through extension framework
  - Fixed `@ExtensionInject` injection failure in Spring AOP proxied beans (e.g., beans with `@Transactional` methods)

### Changed
- Refactor `ExtensionScope` with generics and optimize scope management

## [1.0.1] - 2025-10-06

### Fixed
- Add support for inherited extension points, fix proxy creation issues when extension point interfaces inherit from other interfaces

## [1.0.0] - 2025-10-06

### Added
- `ExtensionContext` extension point manager
- `Matcher<T>` interface and `Priority` interface for extension matching and ordering
- `ExtensionImpl` wrapper class for extension implementations
- Extension scope management with try-with-resources support
- Spring Boot integration: `@Extension` and `@ExtensionInject` annotations

