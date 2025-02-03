// Jackson-jr Annotation-support module Test artifact Module descriptor
module tools.jackson.jr.annotationsupport
{
    // Since we are not split from Main artifact, will not
    // need to depend on Main artifact -- but need its dependencies
    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.core;
    requires tools.jackson.jr.ob;

    // Additional test lib/framework dependencies
    requires org.junit.jupiter.api; // JUnit 5

    // Further, need to open up test packages for JUnit et al
    opens tools.jackson.jr.annotationsupport;
}
