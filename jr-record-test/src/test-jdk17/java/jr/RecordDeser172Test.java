package jr;

import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.jr.ob.JSON;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecordDeser172Test
{
    record FoundDependency(String id, String g, String a, String v, long timestamp) {
        public String getDateTime() {
            return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                    .toString();
        }
    }

    @Test
    public void testRecordDeser172() throws Exception
    {
        final String input = """
            {
              "id": "org.apache.maven:maven-core:3.9.8",
              "g": "org.apache.maven",
              "a": "maven-core",
              "v": "3.9.8",
              "p": "jar",
              "timestamp": 1718267050000
            }
            """;
        final var expected = new FoundDependency("org.apache.maven:maven-core:3.9.8", "org.apache.maven", "maven-core", "3.9.8", 1718267050000L);
        final var actual = JSON.std.beanFrom(FoundDependency.class, input);
        assertEquals(expected, actual);
    }
}
