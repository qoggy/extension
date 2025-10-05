# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-10-06

### Added
- `ExtensionContext` extension point manager
- `Matcher<T>` interface and `Priority` interface for extension matching and ordering
- `ExtensionImpl` wrapper class for extension implementations
- Extension scope management with try-with-resources support
- Spring Boot integration: `@Extension` and `@ExtensionInject` annotations

