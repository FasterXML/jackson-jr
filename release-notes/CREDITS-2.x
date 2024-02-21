Here are people who have contributed to the development of Jackson JSON processor
jackson-jr component(s), version 2.x
(version numbers in brackets indicate release in which the problem was fixed)

Tatu Saloranta, tatu.saloranta@iki.fi: author

Contributors:

Michael Dombrowski (@MikeDombo)

* Contributed #80: Case-insensitive property, enum deserialization
  should be supported
 (2.13.0)
* Contributed #81: `JsrValue` should implement `equals()`
 (2.13.0)
* Contributed #83: Support `@JsonProperty` annotation on enum values
 (2.13.0)
* Contributed #84: Public static fields are included in serialized output
 (2.13.0)

Jonas Konrad (@yawkat)

* Suggested #88: Make `jr-stree` dependency to `jr-objects` optional
 (2.13.0)

Nikolay Chashnikov (@chashnikov)

* Requested #93: Skip serialization of `groovy.lang.MetaClass`
  values to avoid `StackOverflowError`
 (2.17.0)

Gerben Oolbekkink (@github)

* Reported #98: `module-info.java` of `jr-stree` refers to module `com.fasterxml.jackson.jr.ob.api`,
  which is not defined
 (2.13.5)

Reed Passaretti (@reed53)

* Reported #107: Cannot deserialize `byte[]` from JSON `null` value
 (2.15.3)

Julian Honnen (@jhonnen)

* Reported #112: `overrideStandardValueWriter` only applied to first `java.nio.file.Path`
  valued field of bean
 (2.17.0)

@Shounaks

* Contributed fix for #93: Skip serialization of `groovy.lang.MetaClass` values
  to avoid `StackOverflowError`
 (2.17.0)
* Contributed impl for #100: Add support for `java.time` (Java 8 date/time) types
 (2.17.0)
