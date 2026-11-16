package io.github.sfesantos.holidays.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.github.sfesantos.holidays.Holiday;
import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.HolidayType;
import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.ObservanceRule;

class ICalendarTest {

    private static final Layer SPAIN = Layer.named("ES")
            .add(Holiday.fixed("Año Nuevo", Month.JANUARY, 1),
                    Holiday.fixed("Epifanía del Señor", Month.JANUARY, 6),
                    Holiday.easterRelative("Viernes Santo", -2),
                    Holiday.fixed("Fiesta del Trabajo", Month.MAY, 1),
                    Holiday.fixed("Asunción de la Virgen", Month.AUGUST, 15),
                    Holiday.fixed("Fiesta Nacional de España", Month.OCTOBER, 12),
                    Holiday.fixed("Todos los Santos", Month.NOVEMBER, 1),
                    Holiday.fixed("Día de la Constitución", Month.DECEMBER, 6),
                    Holiday.fixed("Inmaculada Concepción", Month.DECEMBER, 8),
                    Holiday.fixed("Navidad", Month.DECEMBER, 25))
            .build();

    private static final Layer ANDALUCIA = Layer.named("ES-AN")
            .add(Holiday.fixed("Día de Andalucía", Month.FEBRUARY, 28),
                    Holiday.easterRelative("Jueves Santo", -3))
            .observance(ObservanceRule.sundayToMonday())
            .build();

    private static final Layer MALAGA = Layer.named("ES-AN-MA")
            .add(Holiday.fixed("Incorporación de Málaga a la Corona de Castilla", Month.AUGUST, 19),
                    Holiday.fixed("Virgen de la Victoria", Month.SEPTEMBER, 8))
            .build();

    private static final HolidayCalendar MALAGA_CALENDAR = HolidayCalendar.builder()
            .weekend(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            .layers(SPAIN, ANDALUCIA, MALAGA)
            .build();

    private static final Year Y2026 = Year.of(2026);
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-16T10:15:30Z"), ZoneOffset.UTC);

    private static String malaga2026() {
        return ICalendar.write(MALAGA_CALENDAR, "ES-AN-MA", Y2026, Y2026, CLOCK);
    }

    private static HolidayCalendar calendarOf(Holiday... holidays) {
        return HolidayCalendar.builder()
                .layers(Layer.named("test").add(holidays).build())
                .build();
    }

    private static List<String> physicalLines(String ics) {
        return List.of(ics.split("\r\n"));
    }

    private static List<String> contentLines(String ics) {
        return physicalLines(ics.replace("\r\n ", ""));
    }

    private static List<String> valuesOf(String ics, String property) {
        return contentLines(ics).stream()
                .filter(line -> line.startsWith(property + ":"))
                .map(line -> line.substring(property.length() + 1))
                .toList();
    }

    private static void assertEveryLineFits(String ics) {
        for (var line : physicalLines(ics)) {
            assertTrue(line.getBytes(StandardCharsets.UTF_8).length <= 75, line);
            assertFalse(line.contains("\n"), line);
            assertFalse(Character.isHighSurrogate(line.charAt(line.length() - 1)), line);
        }
    }

    @Test
    void malaga2026HasFourteenEventsInsideOneCalendar() {
        var ics = malaga2026();
        var lines = physicalLines(ics);

        assertEquals(List.of("BEGIN:VCALENDAR", "VERSION:2.0", "PRODID:-//sfesantos//holidate//EN", "CALSCALE:GREGORIAN",
                "X-WR-CALNAME:ES-AN-MA", "BEGIN:VEVENT"), lines.subList(0, 6));
        assertEquals("END:VCALENDAR", lines.getLast());
        assertTrue(ics.endsWith("\r\n"));
        assertEquals(14, lines.stream().filter("BEGIN:VEVENT"::equals).count());
        assertEquals(14, lines.stream().filter("END:VEVENT"::equals).count());
        assertEveryLineFits(ics);
    }

    @Test
    void eventIsAllDayOnTheObservedDate() {
        var lines = contentLines(malaga2026());
        var summary = lines.indexOf("SUMMARY:Navidad");

        assertEquals(List.of(
                "BEGIN:VEVENT",
                "UID:ES-AN-MA-ES-Navidad-20261225@holidate",
                "DTSTAMP:20261116T101530Z",
                "DTSTART;VALUE=DATE:20261225",
                "DTEND;VALUE=DATE:20261226",
                "SUMMARY:Navidad",
                "CATEGORIES:PUBLIC",
                "DESCRIPTION:Type: PUBLIC\\nLayer: ES",
                "TRANSP:TRANSPARENT",
                "END:VEVENT"), lines.subList(summary - 5, summary + 5));
    }

    @Test
    void movedOccurrenceCarriesItsOriginalDate() {
        var lines = contentLines(malaga2026());
        var summary = lines.indexOf("SUMMARY:Todos los Santos");

        assertEquals("DTSTART;VALUE=DATE:20261102", lines.get(summary - 2));
        assertEquals("DTEND;VALUE=DATE:20261103", lines.get(summary - 1));
        assertEquals("DESCRIPTION:Type: PUBLIC\\nLayer: ES\\nOriginal date: 2026-11-01", lines.get(summary + 2));
        assertEquals(2, lines.stream().filter(line -> line.contains("Original date")).count());
    }

    @Test
    void endDateRollsOverTheYear() {
        var ics = ICalendar.write(calendarOf(Holiday.fixed("Nochevieja", Month.DECEMBER, 31)), "test", Y2026, Y2026, CLOCK);

        assertTrue(ics.contains("DTSTART;VALUE=DATE:20261231\r\nDTEND;VALUE=DATE:20270101\r\n"));
    }

    @Test
    void categoriesCarryTheHolidayType() {
        var halfDay = Holiday.fixed("Nochebuena", Month.DECEMBER, 24).ofType(HolidayType.HALF_DAY);
        var ics = ICalendar.write(calendarOf(halfDay), "test", Y2026, Y2026, CLOCK);

        assertEquals(List.of("HALF_DAY"), valuesOf(ics, "CATEGORIES"));
        assertEquals(List.of("Type: HALF_DAY\\nLayer: test"), valuesOf(ics, "DESCRIPTION"));
    }

    @Test
    void escapesCommaSemicolonBackslashAndNewline() {
        var calendar = calendarOf(
                Holiday.fixed("Incorporación de Málaga a la Corona de Castilla, 1487", Month.AUGUST, 19),
                Holiday.fixed("Fiesta; puente \\ local", Month.SEPTEMBER, 8),
                Holiday.fixed("Dos\nlíneas\r\ny tres\rcuatro", Month.OCTOBER, 1));
        var ics = ICalendar.write(calendar, "Málaga, capital; 2026", Y2026, Y2026, CLOCK);

        assertEquals(List.of(
                "Incorporación de Málaga a la Corona de Castilla\\, 1487",
                "Fiesta\\; puente \\\\ local",
                "Dos\\nlíneas\\ny tres\\ncuatro"), valuesOf(ics, "SUMMARY"));
        assertEquals(List.of("Málaga\\, capital\\; 2026"), valuesOf(ics, "X-WR-CALNAME"));
        assertEveryLineFits(ics);
    }

    @Test
    void uidKeepsOnlyLettersAndDigitsOfEachPart() {
        var calendar = calendarOf(Holiday.fixed(" (Incorporación de Málaga, 1487) ", Month.AUGUST, 19));
        var ics = ICalendar.write(calendar, "Málaga, capital", Y2026, Y2026, CLOCK);

        assertEquals(List.of("Málaga-capital-test-Incorporación-de-Málaga-1487-20260819@holidate"), valuesOf(ics, "UID"));
    }

    @Test
    void foldsLongMultiByteLinesWithoutSplittingACharacter() {
        var name = "Año de la Incorporación de Málaga a la Corona de Castilla 𐍈 ".repeat(6).strip();
        var ics = ICalendar.write(calendarOf(Holiday.fixed(name, Month.AUGUST, 19)), "test", Y2026, Y2026, CLOCK);

        assertTrue(ics.contains("\r\n "));
        assertEveryLineFits(ics);
        assertEquals(List.of(name), valuesOf(ics, "SUMMARY"));
        assertTrue(physicalLines(ics).stream().anyMatch(line -> line.getBytes(StandardCharsets.UTF_8).length == 75));
    }

    @Test
    void foldCountsOctetsNotCharacters() {
        // "SUMMARY:" is 8 octets, so 33 two-octet letters make 74 and the 34th would make 76.
        var name = "ñ".repeat(40);
        var ics = ICalendar.write(calendarOf(Holiday.fixed(name, Month.AUGUST, 19)), "test", Y2026, Y2026, CLOCK);

        assertTrue(ics.contains("SUMMARY:" + "ñ".repeat(33) + "\r\n " + "ñ".repeat(7) + "\r\n"));
    }

    @Test
    void uidsDoNotDependOnTheClock() {
        var later = Clock.fixed(Instant.parse("2027-01-02T03:04:05Z"), ZoneId.of("Europe/Madrid"));
        var again = ICalendar.write(MALAGA_CALENDAR, "ES-AN-MA", Y2026, Y2026, later);
        var uids = valuesOf(malaga2026(), "UID");

        assertEquals(14, new HashSet<>(uids).size());
        assertEquals(uids, valuesOf(again, "UID"));
        assertEquals(List.of("20261116T101530Z"), valuesOf(malaga2026(), "DTSTAMP").stream().distinct().toList());
        assertEquals(List.of("20270102T030405Z"), valuesOf(again, "DTSTAMP").stream().distinct().toList());
    }

    @Test
    void uidTellsApartTheSameHolidayFromTwoLayers() {
        var spain = HolidayCalendar.builder().layers(SPAIN).build();
        var office = HolidayCalendar.builder()
                .layers(Layer.named("office").add(Holiday.fixed("Navidad", Month.DECEMBER, 25)).build())
                .build();
        var ics = ICalendar.write(HolidayCalendar.union(spain, office), "group", Y2026, Y2026, CLOCK);
        var christmas = valuesOf(ics, "UID").stream()
                .filter(uid -> uid.contains("Navidad"))
                .toList();

        assertEquals(List.of("group-ES-Navidad-20261225@holidate", "group-office-Navidad-20261225@holidate"), christmas);
    }

    @Test
    void uidCarriesTheCalendarName() {
        var other = ICalendar.write(MALAGA_CALENDAR, "ES-AN", Y2026, Y2026, CLOCK);

        assertNotEquals(valuesOf(malaga2026(), "UID"), valuesOf(other, "UID"));
    }

    @Test
    void listsEveryYearOfTheRange() {
        var ics = ICalendar.write(MALAGA_CALENDAR, "ES-AN-MA", Y2026, Year.of(2028), CLOCK);
        var starts = valuesOf(ics, "DTSTART;VALUE=DATE");

        assertEquals(42, starts.size());
        assertEquals("20260101", starts.getFirst());
        assertEquals("20281225", starts.getLast());
    }

    @Test
    void writesToAWriterAndToAFile(@TempDir Path dir) throws IOException {
        var file = dir.resolve("malaga.ics");
        var writer = new StringWriter();

        ICalendar.write(MALAGA_CALENDAR, "ES-AN-MA", Y2026, Y2026, CLOCK, writer);
        ICalendar.write(MALAGA_CALENDAR, "ES-AN-MA", Y2026, Y2026, CLOCK, file);

        assertEquals(malaga2026(), writer.toString());
        assertEquals(malaga2026(), Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void wrapsIoFailures(@TempDir Path dir) {
        var broken = new Writer() {

            @Override
            public void write(char[] buffer, int offset, int length) throws IOException {
                throw new IOException("disk full");
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        };

        assertThrows(UncheckedIOException.class,
                () -> ICalendar.write(MALAGA_CALENDAR, "ES-AN-MA", Y2026, Y2026, CLOCK, broken));
        assertThrows(UncheckedIOException.class,
                () -> ICalendar.write(MALAGA_CALENDAR, "ES-AN-MA", Y2026, Y2026, CLOCK, dir));
    }

    @Test
    void rejectsNullArguments() {
        assertEquals("calendar must not be null", assertThrows(NullPointerException.class,
                () -> ICalendar.write(null, "ES", Y2026, Y2026, CLOCK)).getMessage());
        assertEquals("name must not be null", assertThrows(NullPointerException.class,
                () -> ICalendar.write(MALAGA_CALENDAR, null, Y2026, Y2026, CLOCK)).getMessage());
        assertEquals("from must not be null", assertThrows(NullPointerException.class,
                () -> ICalendar.write(MALAGA_CALENDAR, "ES", null, Y2026, CLOCK)).getMessage());
        assertEquals("until must not be null", assertThrows(NullPointerException.class,
                () -> ICalendar.write(MALAGA_CALENDAR, "ES", Y2026, null, CLOCK)).getMessage());
        assertEquals("clock must not be null", assertThrows(NullPointerException.class,
                () -> ICalendar.write(MALAGA_CALENDAR, "ES", Y2026, Y2026, null)).getMessage());
        assertEquals("writer must not be null", assertThrows(NullPointerException.class,
                () -> ICalendar.write(MALAGA_CALENDAR, "ES", Y2026, Y2026, CLOCK, (Writer) null)).getMessage());
        assertEquals("file must not be null", assertThrows(NullPointerException.class,
                () -> ICalendar.write(MALAGA_CALENDAR, "ES", Y2026, Y2026, CLOCK, (Path) null)).getMessage());
    }

    @Test
    void rejectsAnInvertedRange() {
        var thrown = assertThrows(IllegalArgumentException.class,
                () -> ICalendar.write(MALAGA_CALENDAR, "ES", Year.of(2027), Y2026, CLOCK));

        assertEquals("until must not be before from", thrown.getMessage());
    }
}
