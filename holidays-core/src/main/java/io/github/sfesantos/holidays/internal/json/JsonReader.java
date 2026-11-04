package io.github.sfesantos.holidays.internal.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.sfesantos.holidays.format.HolidayFormatException;
import io.github.sfesantos.holidays.internal.json.JsonValue.JsonArray;
import io.github.sfesantos.holidays.internal.json.JsonValue.JsonBoolean;
import io.github.sfesantos.holidays.internal.json.JsonValue.JsonNull;
import io.github.sfesantos.holidays.internal.json.JsonValue.JsonNumber;
import io.github.sfesantos.holidays.internal.json.JsonValue.JsonObject;
import io.github.sfesantos.holidays.internal.json.JsonValue.JsonString;

/**
 * A strict RFC 8259 parser producing a {@link JsonValue} tree. No comments,
 * no trailing commas, no duplicate keys, nothing after the top-level value.
 * Positions are 1-based line and column.
 */
public final class JsonReader {

    private final String text;
    private int pos;
    private int line = 1;
    private int column = 1;

    private JsonReader(String text) {
        this.text = text;
    }

    public static JsonValue parse(String text) {
        var reader = new JsonReader(text);
        reader.skipWhitespace();
        var value = reader.readValue();
        reader.skipWhitespace();

        if (!reader.atEnd()) {
            throw reader.error("unexpected content after the top-level value");
        }

        return value;
    }

    private JsonValue readValue() {
        if (atEnd()) {
            throw error("unexpected end of input");
        }

        return switch (peek()) {
            case '{' -> readObject();
            case '[' -> readArray();
            case '"' -> readString();
            case 't' -> readLiteral("true", new JsonBoolean(true, line, column));
            case 'f' -> readLiteral("false", new JsonBoolean(false, line, column));
            case 'n' -> readLiteral("null", new JsonNull(line, column));
            default -> readNumber();
        };
    }

    private JsonObject readObject() {
        int startLine = line;
        int startColumn = column;
        var members = new LinkedHashMap<String, JsonValue>();
        advance();
        skipWhitespace();

        if (peekIs('}')) {
            advance();

            return new JsonObject(Map.of(), startLine, startColumn);
        }

        while (true) {
            skipWhitespace();
            var key = readString();
            skipWhitespace();
            expect(':');
            skipWhitespace();

            if (members.put(key.value(), readValue()) != null) {
                throw key.error("duplicate member \"" + key.value() + "\"");
            }

            skipWhitespace();

            if (peekIs('}')) {
                advance();

                return new JsonObject(Collections.unmodifiableMap(members), startLine, startColumn);
            }

            expect(',');
        }
    }

    private JsonArray readArray() {
        int startLine = line;
        int startColumn = column;
        var elements = new ArrayList<JsonValue>();
        advance();
        skipWhitespace();

        if (peekIs(']')) {
            advance();

            return new JsonArray(List.of(), startLine, startColumn);
        }

        while (true) {
            skipWhitespace();
            elements.add(readValue());
            skipWhitespace();

            if (peekIs(']')) {
                advance();

                return new JsonArray(List.copyOf(elements), startLine, startColumn);
            }

            expect(',');
        }
    }

    private JsonString readString() {
        if (!peekIs('"')) {
            throw error(atEnd() ? "unexpected end of input" : "expected a string");
        }

        int startLine = line;
        int startColumn = column;
        var out = new StringBuilder();
        advance();

        while (true) {
            if (atEnd()) {
                throw error("unterminated string");
            }

            char c = text.charAt(pos);

            if (c == '"') {
                advance();

                return new JsonString(out.toString(), startLine, startColumn);
            }

            if (c == '\\') {
                readEscape(out);
            } else if (c < 0x20) {
                throw error("control character in string");
            } else {
                out.append(c);
                advance();
            }
        }
    }

    private void readEscape(StringBuilder out) {
        int escapeLine = line;
        int escapeColumn = column;
        advance();

        if (atEnd()) {
            throw error("unterminated string");
        }

        char c = text.charAt(pos);

        switch (c) {
            case '"', '\\', '/' -> out.append(c);
            case 'b' -> out.append('\b');
            case 'f' -> out.append('\f');
            case 'n' -> out.append('\n');
            case 'r' -> out.append('\r');
            case 't' -> out.append('\t');
            case 'u' -> {
                out.append(readHex4());

                return;
            }
            default -> throw new HolidayFormatException("invalid escape \\" + c, escapeLine, escapeColumn);
        }

        advance();
    }

    private char readHex4() {
        if (pos + 5 > text.length()) {
            throw error("incomplete \\u escape");
        }

        var hex = text.substring(pos + 1, pos + 5);

        try {
            var value = (char) Integer.parseInt(hex, 16);

            for (int i = 0; i < 5; i++) {
                advance();
            }

            return value;
        } catch (NumberFormatException e) {
            throw error("invalid \\u escape \\u" + hex);
        }
    }

    private JsonNumber readNumber() {
        int startLine = line;
        int startColumn = column;
        int start = pos;

        if (peekIs('-')) {
            advance();
        }

        if (peekIs('0')) {
            advance();
        } else if (!digits()) {
            throw new HolidayFormatException("unexpected character '" + peek() + "'", startLine, startColumn);
        }

        if (peekIs('.')) {
            advance();

            if (!digits()) {
                throw error("digit expected after decimal point");
            }
        }

        if (peekIs('e') || peekIs('E')) {
            advance();

            if (peekIs('+') || peekIs('-')) {
                advance();
            }

            if (!digits()) {
                throw error("digit expected in exponent");
            }
        }

        return new JsonNumber(text.substring(start, pos), startLine, startColumn);
    }

    private boolean digits() {
        int start = pos;

        while (!atEnd() && Character.isDigit(peek()) && peek() < 128) {
            advance();
        }

        return pos > start;
    }

    private JsonValue readLiteral(String literal, JsonValue value) {
        if (!text.startsWith(literal, pos)) {
            throw error("unexpected character '" + peek() + "'");
        }

        for (int i = 0; i < literal.length(); i++) {
            advance();
        }

        return value;
    }

    private void expect(char c) {
        if (!peekIs(c)) {
            throw error(atEnd() ? "unexpected end of input, expected '" + c + "'" : "expected '" + c + "' but found '" + peek() + "'");
        }

        advance();
    }

    private void skipWhitespace() {
        while (!atEnd()) {
            char c = peek();

            if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
                return;
            }

            advance();
        }
    }

    private boolean atEnd() {
        return pos >= text.length();
    }

    private char peek() {
        return text.charAt(pos);
    }

    private boolean peekIs(char c) {
        return !atEnd() && text.charAt(pos) == c;
    }

    private void advance() {
        if (text.charAt(pos) == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }

        pos++;
    }

    private HolidayFormatException error(String message) {
        return new HolidayFormatException(message, line, column);
    }
}
