package io.github.sfesantos.holidays.internal.json;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.github.sfesantos.holidays.format.HolidayFormatException;

/**
 * A parsed JSON tree. Every node remembers where it started so that errors
 * found while interpreting the tree can point at the file.
 */
public sealed interface JsonValue {

    int line();

    int column();

    default HolidayFormatException error(String message) {
        return new HolidayFormatException(message, line(), column());
    }

    default JsonObject asObject() {
        if (this instanceof JsonObject object) {
            return object;
        }

        throw error("expected an object but found " + kind());
    }

    default JsonArray asArray() {
        if (this instanceof JsonArray array) {
            return array;
        }

        throw error("expected an array but found " + kind());
    }

    default String asString() {
        if (this instanceof JsonString string) {
            return string.value();
        }

        throw error("expected a string but found " + kind());
    }

    default int asInt() {
        if (this instanceof JsonNumber number) {
            return number.intValue();
        }

        throw error("expected an integer but found " + kind());
    }

    default String kind() {
        return switch (this) {
            case JsonObject o -> "an object";
            case JsonArray a -> "an array";
            case JsonString s -> "a string";
            case JsonNumber n -> "a number";
            case JsonBoolean b -> "a boolean";
            case JsonNull n -> "null";
        };
    }

    /** Members in file order; duplicate keys are rejected by the reader. */
    record JsonObject(Map<String, JsonValue> members, int line, int column) implements JsonValue {

        public Optional<JsonValue> get(String key) {
            return Optional.ofNullable(members.get(key));
        }

        public JsonValue require(String key) {
            var value = members.get(key);

            if (value == null) {
                throw error("missing required member \"" + key + "\"");
            }

            return value;
        }

        /** Fails on the first member whose key is not in the allowed list. */
        public void allowOnly(String... keys) {
            var allowed = List.of(keys);

            for (var entry : members.entrySet()) {
                if (!allowed.contains(entry.getKey())) {
                    throw entry.getValue().error("unknown member \"" + entry.getKey() + "\"; expected one of " + allowed);
                }
            }
        }
    }

    record JsonArray(List<JsonValue> elements, int line, int column) implements JsonValue {}

    record JsonString(String value, int line, int column) implements JsonValue {}

    /** Keeps the lexeme; callers convert to the numeric type they need. */
    record JsonNumber(String lexeme, int line, int column) implements JsonValue {

        public int intValue() {
            try {
                return Integer.parseInt(lexeme);
            } catch (NumberFormatException e) {
                throw error("expected an integer but found " + lexeme);
            }
        }
    }

    record JsonBoolean(boolean value, int line, int column) implements JsonValue {}

    record JsonNull(int line, int column) implements JsonValue {}
}
