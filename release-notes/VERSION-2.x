Project: jackson-jr
Major version: 2

Modules:
  jackson-jr-annotation-support
  jackson-jr-objects
  jackson-jr-retrofit2
  jackson-jr-stree

------------------------------------------------------------------------
=== Releases ===
------------------------------------------------------------------------

2.13.0 (30-Sep-2021)

#79: Reuse of ClassKey in ValueWriterLocator not working
#80: Case-insensitive property, enum deserialization should be supported
 (contributed by Mike D)
#81: `JsrValue` should implement `equals()`
 (contributed by Mike D)
#83: Support `@JsonProperty` annotation on enum values
 (contributed by Mike D)
#84: Public static fields are included in serialized output
 (contributed by Mike D)
#88: Make `jr-stree` dependency to `jr-objects` optional
 (suggested by Jonas K)
- Update retrofit dependency to 2.7.2 (and okhttp to 3.14.9)

2.12.5 (27-Aug-2021)
2.12.4 (06-Jul-2021)
2.12.3 (12-Apr-2021)
2.12.2 (03-Mar-2021)

No changes since 2.12.1

2.12.1 (08-Jan-2021)

#76: Annotation-based introspector does not include super-class fields

2.12.0 (29-Nov-2020)

- Add Gradle Module Metadata (https://blog.gradle.org/alignment-with-gradle-module-metadata)

2.11.4 (12-Dec-2020)
2.11.3 (02-Oct-2020)

No changes since 2.11.2

2.11.2 (02-Aug-2020)

- Remove `maven-bundle-plugin` version override (was pinned to old 2.5.3)

2.11.1 (25-Jun-2020)

#72: Duplicate classes from `com.fasterxml.jackson.jr.ob` and
  `com.fasterxml.jackson.jr.type` in 2.11.0
  (reported by CListery@github)
#74: jackson-jr-stree-2.11.1.jar is missing util package classes
 (reported by Turloch O'T)

2.11.0 (26-Apr-2020)

#32: Add support for subset of jackson annotations
 (suggested by Baron H)
#64: Indicate mismatching property if failing on unknown property
#70: Add extension point (`ReaderWriterModifier`) to allow more customization of
  POJO readers, writers

2.10.5 (21-Jul-2020)

#73: Allow for reading `null` fields when reading simple objects
 (reported by Jozsef B)

2.10.4 (03-May-2020)
2.10.3 (03-Mar-2020)

No changes since 2.10.2

2.10.2 (05-Jan-2020)

#71: Jackson-jr 2.10 accidentally uses `UncheckedIOException` only available on JDK 8

2.10.1 (09-Nov-2019)

No changes since 2.10.0

2.10.0 (26-Sep-2019)

#60: Add support for reading "root value" streams (linefeed separated/concatenated)
#63: Change default for `JSON.Feature.USE_FIELDS` to `true` (from false) in 2.10
#65: Allow registration of custom readers, writers (to support 3rd party, custom types)
#66: Add `Json.mapOfFrom(Class)` to support binding POJO-valued maps
 (suggested by ocind@github)
- Add `JrsNull` node type for `jr-stree` package
- Add JDK9+ `module-info.class` using Moditect
- Update Retrofit2 version (2.0.0 -> 2.5.0)

2.9.10 (21-Sep-2019)

No changes since 2.9.9

2.9.9 (16-May-2019)

- Fix an issue with Maps-of-Lists, Lists-of-Maps

2.9.8 (15-Dec-2018)
2.9.7 (19-Sep-2018)
2.9.6 (12-Jun-2018)
2.9.5 (26-Mar-2018)
2.9.4 (24-Jan-2018)
2.9.3 (09-Dec-2017)
2.9.2 (14-Oct-2017)
2.9.1 (07-Sep-2017)
2.9.0 (30-Jul-2017)

No changes since 2.8

2.8.11 (24-Dec-2017)

No changes since 2.8.10

2.8.10 (24-Aug-2017)

#53: `java.io.File` is not a valid source for anyFrom()/mapFrom()
 (reported by CoreyTeffetalor@github)

2.8.9 (12-Jun-2017)

#50: Duplicate key detection does not work
 (reported by inorick@github)

2.8.8 (05-Apr-2017)
2.8.7 (21-Feb-2017)
2.8.6 (12-Jan-2017)
2.8.5 (14-Nov-2016)

No changes since 2.8.4

2.8.4 (14-Oct-2016)

#49: ArrayIndexOutOfBoundsException when parsing large Map
 (reported by Michael J)

2.8.3 (17-Sep-2016)
2.8.2 (30-Aug-2016)
2.8.1 (19-Jul-2016)

No changes since 2.8.0

2.8.0 (04-Jul-2016)

#26: Allow use of public fields for getting/setting values
#43: Add convenience read method for reading trees via `JSON`

2.7.9 (not yet released)

2.7.8 (26-Sep-2016)
2.7.7 (27-Aug-2016)
2.7.6 (23-Jul-2016)

No changes since 2.7.5.

2.7.5 (11-Jun-2016)

#42: Incorrect `jackson-core` dependency form parent pom leads to inclusion
  of non-shaded core jar in `jr-all`
 (reported by Adam V)

2.7.4 (29-Apr-2016)

No changes since 2.7.3.

2.7.3 (16-Mar-2016)

#37: Update Jackson Jr Retrofit 2 Converter for Retrofit 2.0.0
 (contributed by GulajavaMinistudio@github)
#38: PRETTY_PRINT_OUTPUT with composer doesn't work
 (reported by weizhu-us@github)

2.7.2 (26-Feb-2016)
2.7.1 (02-Feb-2016)

No changes since 2.7.0

2.7.0 (10-Jan-2016)

#28: Remove misspelled `JSON.Feature.USE_IS_SETTERS`
#29: Add `JSON.Feature.WRITE_DATES_AS_TIMESTAMP`, enabling of which allows
 serialization of `java.util.Date` as long
 (suggested by Giulio P (gpiancastelli@github))
#30: Add initial version of jackson-jr - based Retrofit2 Converter
 (contributed by GulajavaMinistudio@github)
#31: Fix failure writing UUID, URL and URI
 (reported by Mitsunori K (komamitsu@github))
#34: Add basic read-only (immutable) tree model impementation (stree)

2.6.6 (05-Apr-2016)

#40: Cannot read empty or singleton arrays with JSON.arrayOfFrom
 (reported by Giulio P)

2.6.5 (19-Jan-2015)
2.6.4 (07-Dec-2015)

#27: JSON.Feature.READ_JSON_ARRAYS_AS_JAVA_ARRAYS does not work
- Minor fix to resolution of self-referential types (fix #28 from java-classmate)

2.6.3 (12-Oct-2015)
2.6.2 (14-Sep-2015)
2.6.1 (09-Aug-2015)

No changes since 2.6.0

2.6.0 (19-Jul-2015)

#24: String/byte[] composers can not write POJOs (ObjectCodec not linked)
- Minor performance optimizations, using new jackson-core 2.6 methods

2.5.3 (24-Apr-2015)
2.5.2 (29-Mar-2015)
2.5.1 (06-Feb-2015)
2.5.0 (01-Jan-2015)

No changes since 2.4.

2.4.4 (24-Nov-2014)
2.4.3 (04-Oct-2014)

No changes since 2.4.2

2.4.2 (13-Aug-2014)

#15: Problem with Number to Long conversions
 (reported by "gsmiro@github")
#16: Error serializing POJO-valued Maps
 (reported by Zac M (ZacWolf@github))

2.4.1 (17-Jun-2014)

No changes since 2.4.0

2.4.0 (29-May-2014)

#9: Support lazily materialized Maps with `JSON.Feature.USE_DEFERRED_MAPS`
#11: Replace use of `java.bean.Introspector` since Android SDK doesn't have it.

2.3.3 (10-Apr-2014)

No changes, but depends on Jackson 2.3.3 core.

2.3.1 (26-Mar-2014)

#6: Support reading of `Enum` values from String representation
#8: Handle generic Collection and Map types properly

2.3.0 (17-Mar-2014)

The very first publicly available release!
