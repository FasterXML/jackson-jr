package tools.jackson.jr.ob.record;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class TestClasses
{
    private TestClasses() {
    }


    public record Cow(String message, Map<String, String> object) {
    }

    public record WrapperRecord(Cow cow, String hello) {
    }

    public record RecordWithWrapper(Cow cow, Wrapper nested, int someInt) {
    }

    // [jackson-jr#171]: Whether to serialize Records in declaration or alphabetical order
    public record RecordNonAlphabetic171(int c, int b, int a) {
    }

    record SnakeCaseRecord(
            @JsonProperty("first_name") String firstName
    ) {}

    record SnakeCaseRecordWithIgnore(
            @JsonIgnore int x,
            @JsonProperty("value") int value
    ) {}

    record NonAlphabeticWithAliases(
            @JsonIgnore int x,
            @JsonProperty("z") int d,
            @JsonProperty("c") int c,
            String b,
            int a
    ) {
        public NonAlphabeticWithAliases clearX() {
            return new NonAlphabeticWithAliases(0, d, c, b, a);
        }
    }
}
