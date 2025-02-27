package jr;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.jr.ob.JSON;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecordDeser167Test
{
    record FoundDependency(String id, String g, String a, String v, String timestamp) {}

    // [jackson-jr#167]
    @Test
    public void testRecordDeserOrder167() throws Exception
    {
        final String input = """
            {
              "id": "org.apache.maven:maven-core:3.9.8",
              "g": "org.apache.maven",
              "a": "maven-core",
              "v": "3.9.8",
              "p": "jar",
              "timestamp": 1718267050000,
              "ec": [
                "-cyclonedx.json",
                "-sources.jar",
                "-cyclonedx.xml",
                ".pom",
                "-javadoc.jar",
                ".jar"
              ],
              "tags": [
                "core",
                "maven",
                "classes"
              ]
            }
            """;
        final var expected = new FoundDependency("org.apache.maven:maven-core:3.9.8", "org.apache.maven", "maven-core", "3.9.8", "1718267050000");
        final var actual = JSON.std.beanFrom(FoundDependency.class, input);
        assertEquals(expected, actual);
    }
}
