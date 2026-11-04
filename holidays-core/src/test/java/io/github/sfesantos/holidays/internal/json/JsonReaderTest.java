package io.github.sfesantos.holidays.internal.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.github.sfesantos.holidays.format.HolidayFormatException;
import io.github.sfesantos.holidays.internal.json.JsonValue.JsonArray;
import io.github.sfesantos.holidays.internal.json.JsonValue.JsonBoolean;
import io.github.sfesantos.holidays.internal.json.JsonValue.JsonNull;
import io.github.sfesantos.holidays.internal.json.JsonValue.JsonNumber;
import io.github.sfesantos.holidays.internal.json.JsonValue.JsonObject;

class JsonReaderTest {

    @Test
    void parsesNestedStructuresAndKeepsMemberOrder() {
        var root = JsonReader.parse("""
                {
                  "layers": [ { "id": "ES", "weekend": ["SATURDAY", "SUNDAY"], "from": 1978, "on": true, "x": null } ],
                  "empty": {}, "none": []
                }
                """).asObject();
        var layer = root.require("layers").asArray().elements().get(0).asObject();

        assertEquals(List.of("layers", "empty", "none"), List.copyOf(root.members().keySet()));
        assertEquals(List.of("id", "weekend", "from", "on", "x"), List.copyOf(layer.members().keySet()));
        assertEquals("ES", layer.require("id").asString());
        assertEquals(1978, layer.require("from").asInt());
        assertEquals(2, layer.require("weekend").asArray().elements().size());
        assertTrue(assertInstanceOf(JsonBoolean.class, layer.require("on")).value());
        assertInstanceOf(JsonNull.class, layer.require("x"));
        assertTrue(root.require("empty").asObject().members().isEmpty());
        assertTrue(root.require("none").asArray().elements().isEmpty());
        assertTrue(layer.get("missing").isEmpty());
    }

    @Test
    void decodesEscapes() {
        var value = JsonReader.parse("\"a\\\"b\\\\c\\/d\\n\\t\\u00e9\\u20ac\"").asString();

        assertEquals("a\"b\\c/d\n\té€", value);
    }

    @ParameterizedTest
    @CsvSource({"0", "-1", "42", "3.25", "-0.5", "1e3", "2E-2", "10.5e+1"})
    void acceptsNumbers(String lexeme) {
        assertEquals(lexeme, assertInstanceOf(JsonNumber.class, JsonReader.parse(lexeme)).lexeme());
    }

    @Test
    void integersAreCheckedOnConversion() {
        assertEquals(-2, JsonReader.parse("-2").asInt());

        var fraction = assertThrows(HolidayFormatException.class, () -> JsonReader.parse("2.5").asInt());

        assertTrue(fraction.getMessage().contains("expected an integer"));
        assertThrows(HolidayFormatException.class, () -> JsonReader.parse("\"2\"").asInt());
    }

    @Test
    void nodesRememberTheirPosition() {
        var root = JsonReader.parse("{\n  \"a\": 1,\n  \"b\": [\n    true\n  ]\n}").asObject();

        assertEquals(1, root.line());
        assertEquals(1, root.column());
        assertEquals(2, root.require("a").line());
        assertEquals(8, root.require("a").column());
        assertEquals(4, root.require("b").asArray().elements().get(0).line());
        assertEquals(5, root.require("b").asArray().elements().get(0).column());
    }

    @Test
    void typeMismatchesPointAtTheNode() {
        var error = assertThrows(HolidayFormatException.class, () -> JsonReader.parse("{\"a\": [1]}").asObject().require("a").asString());

        assertEquals("line 1, column 7: expected a string but found an array", error.getMessage());
        assertEquals(1, error.line());
        assertEquals(7, error.column());
    }

    @Test
    void unknownMembersAreRejectedWhenAsked() {
        var object = JsonReader.parse("{\"id\": \"x\",\n \"colour\": 1}").asObject();

        object.allowOnly("id", "colour");

        var error = assertThrows(HolidayFormatException.class, () -> object.allowOnly("id"));

        assertEquals(2, error.line());
        assertTrue(error.getMessage().contains("unknown member \"colour\""));
        assertTrue(assertThrows(HolidayFormatException.class, () -> object.require("name")).getMessage().contains("missing required member \"name\""));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "[1,]              | 1 | 4 | unexpected character ']'",
        "{\"a\": 1,}        | 1 | 9 | expected a string",
        "{\"a\": 1 \"b\": 2} | 1 | 9 | expected ','",
        "{\"a\": 1, \"a\": 2} | 1 | 10 | duplicate member",
        "\"abc             | 1 | 5 | unterminated string",
        "\"a\\x\"            | 1 | 3 | invalid escape",
        "[1] 2             | 1 | 5 | unexpected content after the top-level value",
        "tru               | 1 | 1 | unexpected character 't'",
        "01                | 1 | 2 | unexpected content after the top-level value",
        "1.                | 1 | 3 | digit expected after decimal point",
        "// c              | 1 | 1 | unexpected character '/'",
        "{\"a\":             | 1 | 6 | unexpected end of input"})
    void syntaxErrorsCarryLineAndColumn(String text, int line, int column, String message) {
        var error = assertThrows(HolidayFormatException.class, () -> JsonReader.parse(text));

        assertEquals(line, error.line(), error.getMessage());
        assertEquals(column, error.column(), error.getMessage());
        assertTrue(error.getMessage().contains(message), error.getMessage());
    }

    @Test
    void emptyInputIsRejected() {
        assertEquals(1, assertThrows(HolidayFormatException.class, () -> JsonReader.parse("")).column());
        assertEquals(4, assertThrows(HolidayFormatException.class, () -> JsonReader.parse("   ")).column());
    }

    @Test
    void controlCharactersInStringsAreRejected() {
        var error = assertThrows(HolidayFormatException.class, () -> JsonReader.parse("\"a\nb\""));

        assertTrue(error.getMessage().contains("control character"));
    }

    @Test
    void errorsOnLaterLinesReportTheRightLine() {
        var error = assertThrows(HolidayFormatException.class, () -> JsonReader.parse("{\n  \"a\": 1,\n  \"b\": tru\n}"));

        assertEquals(3, error.line());
        assertEquals(8, error.column());
    }

    @Test
    void writerRoundTripsAndPrettyPrints() {
        var text = """
                {
                  "name": "Año \\"Nuevo\\"\\n",
                  "list": [
                    1,
                    -2.5,
                    true,
                    null,
                    {}
                  ],
                  "empty": []
                }
                """;
        var value = JsonReader.parse(text);

        assertEquals(text, JsonWriter.write(value));
        assertEquals(value, JsonReader.parse(JsonWriter.write(value)));
    }

    @Test
    void writerEscapesControlCharacters() {
        var value = new JsonValue.JsonString("tab\tbell\u0007", 0, 0);

        assertEquals("\"tab\\tbell\\u0007\"\n", JsonWriter.write(value));
        assertInstanceOf(JsonArray.class, JsonReader.parse("[]"));
        assertInstanceOf(JsonObject.class, JsonReader.parse(" {} "));
    }
}
