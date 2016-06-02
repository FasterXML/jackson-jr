## Overview

This package is a very simple `TreeCodec` implementation, which allows pluggability with
the main `jr-objects` package, specifically for `JSON` object:

```java
JSON json = JSON.std.with(new JacksonJrsTreeCodec());
TreeNode root = json.treeFrom(jsonContent);
// access content using `get`, `path` and `at` methods
```

As implementation this is meant as minimal package that is mostly useful for reading JSON
content as tree and simple traversal, as well as for passing resulting trees to other
processing components. Nodes are immutable which is useful for some use (caching),
and less useful for others.

While it is possible to manually construct trees by directly constructing `JrsObject`
(and other instances), it is likely more convenient to use Composer style construction
for JSON generation.
