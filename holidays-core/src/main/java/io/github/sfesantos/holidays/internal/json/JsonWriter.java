package io.github.sfesantos.holidays.internal.json;

import java.util.Map;

/**
 * Pretty-prints a {@link JsonValue} tree with two-space indentation and a
 * trailing newline. Strings are escaped as RFC 8259 requires.
 */
public final class JsonWriter {

    private static final String INDENT = "  ";

    private final StringBuilder out = new StringBuilder();

    private JsonWriter() {}

    public static String write(JsonValue value) {
        var writer = new JsonWriter();
        writer.writeValue(value, 0);
        writer.out.append('\n');

        return writer.out.toString();
    }

    private void writeValue(JsonValue value, int depth) {
        switch (value) {
            case JsonValue.JsonObject o -> writeObject(o.members(), depth);
            case JsonValue.JsonArray a -> writeArray(a, depth);
            case JsonValue.JsonString s -> writeString(s.value());
            case JsonValue.JsonNumber n -> out.append(n.lexeme());
            case JsonValue.JsonBoolean b -> out.append(b.value());
            case JsonValue.JsonNull n -> out.append("null");
        }
    }

    private void writeObject(Map<String, JsonValue> members, int depth) {
        if (members.isEmpty()) {
            out.append("{}");

            return;
        }

        out.append("{\n");
        var first = true;

        for (var entry : members.entrySet()) {
            if (!first) {
                out.append(",\n");
            }

            first = false;
            indent(depth + 1);
            writeString(entry.getKey());
            out.append(": ");
            writeValue(entry.getValue(), depth + 1);
        }

        out.append('\n');
        indent(depth);
        out.append('}');
    }

    private void writeArray(JsonValue.JsonArray array, int depth) {
        if (array.elements().isEmpty()) {
            out.append("[]");

            return;
        }

        out.append("[\n");
        var first = true;

        for (var element : array.elements()) {
            if (!first) {
                out.append(",\n");
            }

            first = false;
            indent(depth + 1);
            writeValue(element, depth + 1);
        }

        out.append('\n');
        indent(depth);
        out.append(']');
    }

    private void writeString(String value) {
        out.append('"');

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }

        out.append('"');
    }

    private void indent(int depth) {
        out.append(INDENT.repeat(depth));
    }
}
