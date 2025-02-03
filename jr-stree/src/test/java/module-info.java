// Jackson-jr Simple Tree module Test artifact descriptor
module tools.jackson.jr.stree
{
    // Since we are not split from Main artifact, will not
    // need to depend on Main artifact -- but need its dependencies
    requires tools.jackson.core;
    requires tools.jackson.jr.ob;

    // Additional test lib/framework dependencies
    requires org.junit.jupiter.api; // JUnit 5

    // Further, need to open up test packages for JUnit et al
    opens tools.jackson.jr.stree;
    opens tools.jackson.jr.stree.util;
}
