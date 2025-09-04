package jr;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public final class TestClasses {

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
}
