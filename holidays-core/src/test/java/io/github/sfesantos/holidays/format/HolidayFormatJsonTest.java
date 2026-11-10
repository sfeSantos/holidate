package io.github.sfesantos.holidays.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.github.sfesantos.holidays.Holiday;
import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.HolidayRule;
import io.github.sfesantos.holidays.HolidayType;
import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.ObservanceRule;
import io.github.sfesantos.holidays.Weekend;
import io.github.sfesantos.holidays.easter.EasterAlgorithm;
import io.github.sfesantos.holidays.hebrew.HebrewMonth;

class HolidayFormatJsonTest {

    private static final HolidayFormat JSON = HolidayFormat.json();

    private static List<Layer> spainFile() {
        try (var in = HolidayFormatJsonTest.class.getResourceAsStream("ES.json")) {
            return JSON.parse(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void readsTheRoadmapExample() {
        var layers = spainFile();

        assertEquals(List.of("ES", "ES-AN", "ES-CT"), layers.stream().map(Layer::name).toList());

        var spain = layers.get(0);
        var names = spain.additions().stream().map(Holiday::name).toList();

        assertEquals(Weekend.standard(), spain.weekend().orElseThrow());
        assertTrue(spain.parent().isEmpty());
        assertEquals(8, names.size());
        assertEquals(HolidayRule.fixed(Month.JANUARY, 1), spain.additions().get(0).rule());
        assertEquals(HolidayRule.easterRelative(-2), spain.additions().get(1).rule());
        assertEquals(Year.of(1978), spain.additions().get(2).validity().from());
        assertEquals(HolidayRule.nthWeekdayOfMonth(4, DayOfWeek.THURSDAY, Month.NOVEMBER), spain.additions().get(3).rule());

        var election = spain.additions().get(4);

        assertEquals(HolidayType.OBSERVANCE, election.type());
        assertEquals(Year.of(2030), election.validity().until());
        assertEquals(HolidayRule.weekdayAfter(DayOfWeek.MONDAY, java.time.MonthDay.of(11, 1)), election.rule());
        assertEquals(HolidayType.HALF_DAY, spain.additions().get(5).type());
        assertEquals(LocalTime.of(13, 0), spain.additions().get(5).closesAt());
        assertEquals(HolidayRule.hijri(10, 1), spain.additions().get(6).rule());
        assertEquals(HolidayRule.hebrew(HebrewMonth.NISAN, 15), spain.additions().get(7).rule());
        assertEquals(HolidayType.BANK, spain.additions().get(7).type());

        var andalucia = layers.get(1);

        assertEquals("ES", andalucia.parent().orElseThrow());
        assertEquals(ObservanceRule.sundayToMonday(), andalucia.observance());

        var catalunya = layers.get(2);

        assertEquals(List.of("Constitución"), List.copyOf(catalunya.removals()));
        assertEquals(ObservanceRule.none(), catalunya.overrides().get("Thanksgiving").observance());
    }

    @Test
    void parsedLayersBuildTheSameCalendarAsJava() {
        var layers = spainFile();
        var fromFile = HolidayCalendar.builder().layers(layers.get(0), layers.get(1)).build();
        var fromJava = HolidayCalendar.builder()
                .layers(Layer.named("ES")
                                .add(Holiday.fixed("Año Nuevo", Month.JANUARY, 1),
                                        Holiday.easterRelative("Viernes Santo", -2),
                                        Holiday.fixed("Constitución", Month.DECEMBER, 6).validFrom(Year.of(1978)),
                                        Holiday.of("Thanksgiving", HolidayRule.nthWeekdayOfMonth(4, DayOfWeek.THURSDAY, Month.NOVEMBER)),
                                        Holiday.of("Election Day", HolidayRule.weekdayAfter(DayOfWeek.MONDAY, java.time.MonthDay.of(11, 1)))
                                                .ofType(HolidayType.OBSERVANCE).validUntil(Year.of(2030)),
                                        Holiday.fixed("Nochebuena", Month.DECEMBER, 24).closingAt(LocalTime.of(13, 0)),
                                        Holiday.hijri("Eid al-Fitr", 10, 1),
                                        Holiday.hebrew("Pesach", HebrewMonth.NISAN, 15).ofType(HolidayType.BANK))
                                .build(),
                        Layer.named("ES-AN")
                                .add(Holiday.fixed("Día de Andalucía", Month.FEBRUARY, 28), Holiday.easterRelative("Jueves Santo", -3))
                                .observance(ObservanceRule.sundayToMonday())
                                .build())
                .build();

        for (int year = 2024; year <= 2031; year++) {
            assertEquals(fromJava.holidaysIn(Year.of(year)), fromFile.holidaysIn(Year.of(year)), "year " + year);
        }

        assertEquals(fromJava.businessHoursOn(LocalDate.of(2026, 12, 24)), fromFile.businessHoursOn(LocalDate.of(2026, 12, 24)));
    }

    @Test
    void writesWhatItReads() {
        var layers = spainFile();
        var written = JSON.write(layers);
        var reparsed = JSON.parse(written);

        assertEquals(written, JSON.write(reparsed));
        assertEquals(layers.size(), reparsed.size());

        var original = HolidayCalendar.builder().layers(layers.get(0), layers.get(2)).build();
        var copy = HolidayCalendar.builder().layers(reparsed.get(0), reparsed.get(2)).build();

        assertEquals(original.holidaysIn(Year.of(2026)), copy.holidaysIn(Year.of(2026)));
        assertEquals(original.weekend(), copy.weekend());
    }

    @Test
    void writerLeavesDefaultsOut() {
        var layer = Layer.named("GB")
                .extending("EU")
                .weekend(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
                .observance(ObservanceRule.weekendToMonday())
                .add(Holiday.fixed("Christmas Day", Month.DECEMBER, 25).withObservance(ObservanceRule.none()),
                        Holiday.of("Summer Bank Holiday", HolidayRule.nthWeekdayOfMonth(-1, DayOfWeek.MONDAY, Month.AUGUST)))
                .remove("Epiphany")
                .build();

        assertEquals("""
                {
                  "layers": [
                    {
                      "id": "GB",
                      "extends": "EU",
                      "weekend": [
                        "SATURDAY",
                        "SUNDAY"
                      ],
                      "observance": "WEEKEND_TO_MONDAY",
                      "holidays": [
                        {
                          "name": "Christmas Day",
                          "rule": {
                            "fixed": "12-25"
                          },
                          "observance": "NONE"
                        },
                        {
                          "name": "Summer Bank Holiday",
                          "rule": {
                            "nth": -1,
                            "weekday": "MONDAY",
                            "month": "AUGUST"
                          }
                        }
                      ],
                      "remove": [
                        "Epiphany"
                      ]
                    }
                  ]
                }
                """, JSON.write(List.of(layer)));
    }

    @Test
    void everyRuleKindRoundTrips() {
        var layer = Layer.named("all")
                .add(Holiday.of("a", HolidayRule.weekdayBefore(DayOfWeek.FRIDAY, java.time.MonthDay.of(12, 25))),
                        Holiday.hijri("b", 12, 10),
                        Holiday.hebrew("c", HebrewMonth.ADAR_I, 14),
                        Holiday.fixed("d", Month.FEBRUARY, 29).validBetween(Year.of(2000), Year.of(2100)),
                        Holiday.on("e", LocalDate.of(2011, 4, 29)),
                        Holiday.of("f", HolidayRule.easterRelative(-2).plusDays(-7)))
                .build();
        var reparsed = JSON.parse(JSON.write(List.of(layer))).get(0);

        for (int i = 0; i < 6; i++) {
            assertEquals(layer.additions().get(i), reparsed.additions().get(i));
        }
    }

    @Test
    void readsAndWritesADateRule() {
        var text = "{\"layers\": [{\"id\": \"UK\", \"holidays\": [{\"name\": \"Royal Wedding\", \"rule\": {\"date\": \"2011-04-29\"}}]}]}";
        var holiday = JSON.parse(text).get(0).additions().get(0);

        assertEquals(HolidayRule.on(LocalDate.of(2011, 4, 29)), holiday.rule());
        assertTrue(JSON.write(JSON.parse(text)).contains("\"date\": \"2011-04-29\""));
    }

    @Test
    void readsAndWritesAShiftedRule() {
        var text = "{\"layers\": [{\"id\": \"US\", \"holidays\": [{\"name\": \"Day after Thanksgiving\", "
                + "\"rule\": {\"nth\": 4, \"weekday\": \"THURSDAY\", \"month\": \"NOVEMBER\", \"shift\": 1}}]}]}";
        var holiday = JSON.parse(text).get(0).additions().get(0);

        assertEquals(HolidayRule.nthWeekdayOfMonth(4, DayOfWeek.THURSDAY, Month.NOVEMBER).plusDays(1), holiday.rule());
        assertEquals(Optional.of(LocalDate.of(2026, 11, 27)), holiday.dateIn(Year.of(2026), EasterAlgorithm.gregorian()));
        assertTrue(JSON.write(JSON.parse(text)).contains("\"month\": \"NOVEMBER\",\n            \"shift\": 1\n"));
    }

    @Test
    void aShiftOfZeroReadsAsThePlainRule() {
        var text = "{\"layers\": [{\"id\": \"x\", \"holidays\": [{\"name\": \"h\", \"rule\": {\"fixed\": \"12-25\", \"shift\": 0}}]}]}";

        assertEquals(HolidayRule.fixed(Month.DECEMBER, 25), JSON.parse(text).get(0).additions().get(0).rule());
    }

    @Test
    void nestedShiftsAreWrittenAsOne() {
        var christmas = HolidayRule.fixed(Month.DECEMBER, 25);
        var layer = Layer.named("x")
                .add(Holiday.of("nested", christmas.plusDays(1).plusDays(2)), Holiday.of("cancelled", christmas.plusDays(2).plusDays(-2)))
                .build();
        var written = JSON.write(List.of(layer));
        var reparsed = JSON.parse(written).get(0);

        assertEquals(christmas.plusDays(3), reparsed.additions().get(0).rule());
        assertEquals(christmas, reparsed.additions().get(1).rule());
        assertEquals(1, written.split("\"shift\"", -1).length - 1);
    }

    @Test
    void parsesARuleOnItsOwn() {
        assertEquals(HolidayRule.fixed(Month.JANUARY, 1), JSON.parseRule("{\"fixed\": \"01-01\"}"));
        assertEquals(HolidayRule.nthWeekdayOfMonth(4, DayOfWeek.THURSDAY, Month.NOVEMBER).plusDays(1),
                JSON.parseRule("{\"nth\": 4, \"weekday\": \"THURSDAY\", \"month\": \"NOVEMBER\", \"shift\": 1}"));
    }

    @Test
    void aBadRuleOnItsOwnReportsItsPosition() {
        var unknownKind = assertThrows(HolidayFormatException.class, () -> JSON.parseRule("{\"fixd\": \"01-01\"}"));
        var badDay = assertThrows(HolidayFormatException.class, () -> JSON.parseRule("{\"fixed\": \"1 January\"}"));

        assertTrue(unknownKind.getMessage().contains("exactly one of"));
        assertEquals(1, badDay.line());
        assertEquals(11, badDay.column());
        assertThrows(HolidayFormatException.class, () -> JSON.parseRule("\"01-01\""));
        assertThrows(NullPointerException.class, () -> JSON.parseRule(null));
    }

    @Test
    void customRulesAndObservancesCannotBeWritten() {
        var customRule = Layer.named("x").add(Holiday.custom("Odd", year -> Optional.empty())).build();
        var customObservance = Layer.named("y").observance(ObservanceRule.custom(date -> date)).build();
        var customHolidayObservance = Layer.named("z")
                .add(Holiday.fixed("May", Month.MAY, 1).withObservance(ObservanceRule.custom(date -> date)))
                .build();

        var shiftedCustomRule = Layer.named("w").add(Holiday.of("Odd", HolidayRule.custom(year -> Optional.empty()).plusDays(1))).build();

        assertTrue(assertThrows(HolidayFormatException.class, () -> JSON.write(List.of(customRule))).getMessage().contains("'Odd' uses a custom rule"));
        assertTrue(assertThrows(HolidayFormatException.class, () -> JSON.write(List.of(shiftedCustomRule))).getMessage().contains("'Odd' uses a custom rule"));
        assertTrue(assertThrows(HolidayFormatException.class, () -> JSON.write(List.of(customObservance))).getMessage().contains("layer y"));
        assertTrue(assertThrows(HolidayFormatException.class, () -> JSON.write(List.of(customHolidayObservance))).getMessage().contains("'May'"));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "[]                                                                   | 1 | 1 | expected an object",
        "{}                                                                   | 1 | 1 | missing required member \"layers\"",
        "{\"layers\": [], \"colour\": 1}                                        | 1 | 26 | unknown member \"colour\"",
        "{\"layers\": [{\"name\": \"ES\"}]}                                      | 1 | 22 | unknown member \"name\"",
        "{\"layers\": [{\"id\": \"\"}]}                                          | 1 | 20 | layer name must not be blank",
        "{\"layers\": [{\"id\": \"ES\", \"extends\": \"ES\"}]}                     | 1 | 37 | cannot extend itself",
        "{\"layers\": [{\"id\": \"ES\", \"weekend\": [\"SATURDAY\", \"CAturday\"]}]} | 1 | 50 | unknown DayOfWeek \"CAturday\"",
        "{\"layers\": [{\"id\": \"ES\", \"observance\": \"INHERIT\"}]}              | 1 | 40 | cannot declare observance INHERIT",
        "{\"layers\": [{\"id\": \"ES\", \"observance\": \"MONDAY\"}]}               | 1 | 40 | unknown observance \"MONDAY\"",
        "{\"layers\": [{\"id\": \"ES\", \"holidays\": [{\"name\": \"x\"}]}]}         | 1 | 39 | missing required member \"rule\"",
        "{\"layers\": [{\"id\": \"ES\", \"holidays\": [{\"name\": \"x\", \"rule\": {}}]}]} | 1 | 61 | exactly one of",
        "{\"layers\": [{\"id\": \"ES\", \"holidays\": [{\"name\": \"x\", \"rule\": {\"fixed\": \"01-01\", \"easter\": 1}}]}]} | 1 | 61 | exactly one of",
        "{\"layers\": [{\"id\": \"ES\", \"holidays\": [{\"name\": \"x\", \"rule\": {\"fixed\": \"13-01\"}}]}]} | 1 | 71 | Invalid value for MonthOfYear",
        "{\"layers\": [{\"id\": \"ES\", \"holidays\": [{\"name\": \"x\", \"rule\": {\"fixed\": \"1-1\"}}]}]} | 1 | 71 | expected a day as \"MM-DD\"",
        "{\"layers\": [{\"id\": \"ES\", \"holidays\": [{\"name\": \"x\", \"rule\": {\"date\": \"2011-4-29\"}}]}]} | 1 | 70 | expected a date as \"YYYY-MM-DD\"",
        "{\"layers\": [{\"id\": \"ES\", \"holidays\": [{\"name\": \"x\", \"rule\": {\"date\": \"2011-02-30\"}}]}]} | 1 | 70 | Invalid date",
        "{\"layers\": [{\"id\": \"ES\", \"holidays\": [{\"name\": \"x\", \"rule\": {\"nth\": 4, \"weekday\": \"THURSDAY\"}}]}]} | 1 | 61 | missing required member \"month\"",
        "{\"layers\": [{\"id\": \"ES\", \"holidays\": [{\"name\": \"x\", \"rule\": {\"fixed\": \"01-01\", \"base\": \"01-01\"}}]}]} | 1 | 88 | unknown member \"base\"",
        "{\"layers\": [{\"id\": \"ES\", \"holidays\": [{\"name\": \"x\", \"rule\": {\"shift\": 1}}]}]} | 1 | 61 | exactly one of",
        "{\"layers\": [{\"id\": \"ES\", \"holidays\": [{\"name\": \"x\", \"rule\": {\"fixed\": \"01-01\", \"shift\": 1.5}}]}]} | 1 | 89 | expected an integer but found 1.5",
        "{\"layers\": [{\"id\": \"ES\", \"holidays\": [{\"name\": \"x\", \"rule\": {\"fixed\": \"01-01\", \"shift\": \"1\"}}]}]} | 1 | 89 | expected an integer but found a string",
        "{\"layers\": [{\"id\": \"ES\", \"holidays\": [{\"name\": \"x\", \"rule\": {\"fixed\": \"01-01\", \"shift\": 400}}]}]} | 1 | 89 | the total shift must be within +/-366 days: 400",
        "{\"layers\": [{\"id\": \"ES\", \"holidays\": [{\"name\": \"x\", \"rule\": {\"hebrew\": \"ADAR-14-1\"}}]}]} | 1 | 72 | expected a Hebrew day",
        "{\"layers\": [{\"id\": \"ES\", \"holidays\": [{\"name\": \"x\", \"rule\": {\"hebrew\": \"ADR-14\"}}]}]} | 1 | 72 | unknown HebrewMonth \"ADR\"",
        "{\"layers\": [{\"id\": \"ES\", \"holidays\": [{\"name\": \"x\", \"rule\": {\"fixed\": \"01-01\"}, \"type\": \"PUBLIC\", \"closesAt\": \"13:00\"}]}]} | 1 | 111 | closesAt needs type HALF_DAY",
        "{\"layers\": [{\"id\": \"ES\", \"holidays\": [{\"name\": \"x\", \"rule\": {\"fixed\": \"01-01\"}, \"from\": 2030, \"until\": 2020}]}]} | 1 | 104 | is after until",
        "{\"layers\": [{\"id\": \"ES\", \"holidays\": [{\"name\": \"x\", \"rule\": {\"fixed\": \"01-01\"}}, {\"name\": \"x\", \"rule\": {\"fixed\": \"01-02\"}}]}]} | 1 | 13 | declares 'x' more than once"})
    void dataErrorsPointAtTheMember(String text, int line, int column, String message) {
        var error = assertThrows(HolidayFormatException.class, () -> JSON.parse(text));

        assertTrue(error.getMessage().contains(message), error.getMessage());
        assertEquals(line, error.line(), error.getMessage());
        assertEquals(column, error.column(), error.getMessage());
    }

    @Test
    void closesAtWithHalfDayTypeIsAccepted() {
        var layer = JSON.parse("{\"layers\": [{\"id\": \"x\", \"holidays\": [{\"name\": \"h\", \"rule\": {\"fixed\": \"12-24\"}, \"type\": \"HALF_DAY\", \"closesAt\": \"14:30\"}]}]}").get(0);

        assertEquals(LocalTime.of(14, 30), layer.additions().get(0).closesAt());
        assertEquals(HolidayType.HALF_DAY, layer.additions().get(0).type());
    }

    @Test
    void readsAndWritesFiles(@TempDir Path dir) {
        var file = dir.resolve("ES.json");
        var layers = spainFile();

        JSON.write(layers, file);

        var reparsed = JSON.parse(file);

        assertEquals(JSON.write(layers), JSON.write(reparsed));

        var writer = new StringWriter();
        JSON.write(layers, writer);

        assertEquals(JSON.write(layers), writer.toString());
        assertEquals(3, JSON.parse(new StringReader(writer.toString())).size());
        assertInstanceOf(UncheckedIOException.class, assertThrows(RuntimeException.class, () -> JSON.parse(dir.resolve("missing.json"))));
    }

    @Test
    void emptyLayerListRoundTrips() {
        assertEquals("{\n  \"layers\": []\n}\n", JSON.write(List.of()));
        assertEquals(List.of(), JSON.parse("{\"layers\": []}"));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> JSON.parse((String) null));
        assertThrows(NullPointerException.class, () -> JSON.write(null));
        assertFalse(HolidayFormat.json().toString().isEmpty());
    }

    @Test
    void schemaShipsInTheJar() throws IOException {
        try (var in = HolidayFormat.class.getResourceAsStream("/META-INF/holidays/holidays.schema.json")) {
            var text = new String(in.readAllBytes(), StandardCharsets.UTF_8);

            assertTrue(text.contains("\"$schema\""));
            assertTrue(text.contains("weekdayBefore"));
        }
    }
}
