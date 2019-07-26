# Changelog

## 3.3.0.2

- Fix potential issue around loading java path on fresh installs
- Fix legacy resources not installing
- Don't copy files from downloadable if not needed (hashes match)
- Add Sentry breadcrumbs for instance launching
- Fix packs using caseallfiles not working correctly
- Remove some logging of stack traces that are unecessary
- Add in limiting to only send errors once
