// Jackson-jr Objects module Test artifact Module descriptor
module tools.jackson.jr.ob
{
    // Since we are not split from Main artifact, will not
    // need to depend on Main artifact -- but need its dependencies
    requires transitive tools.jackson.core;

    // Additional test lib/framework dependencies
    requires org.junit.jupiter.api; // JUnit 5

    // Further, need to open up test packages for JUnit et al
    opens tools.jackson.jr.ob;
    opens tools.jackson.jr.ob.impl;
    opens tools.jackson.jr.ob.record;
    opens tools.jackson.jr.testutil.failure;
    opens tools.jackson.jr.tofix;
}
