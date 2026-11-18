package io.github.sfesantos.holidays.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

class TextCalendarTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-18T10:15:30Z"), ZoneOffset.UTC);
    private static final TextCalendar CALENDAR = TextCalendar.of(CLOCK);

    private static final String NOVEMBER_2026 = """
               November 2026
            Mo Tu We Th Fr Sa Su
                               1
             2  3  4  5  6  7  8
             9 10 11 12 13 14 15
            16 17 18 19 20 21 22
            23 24 25 26 27 28 29
            30
            """;

    private static final String NOVEMBER_2026_ON_THE_18TH = """
               November 2026
            Mo Tu We Th Fr Sa Su
                               1
             2  3  4  5  6  7  8
             9 10 11 12 13 14 15
            16 17[18]19 20 21 22
            23 24 25 26 27 28 29
            30
            """;

    private static final String YEAR_2026 = """
                                          2026

                  January               February               March
            Mo Tu We Th Fr Sa Su  Mo Tu We Th Fr Sa Su  Mo Tu We Th Fr Sa Su
                      1  2  3  4                     1                     1
             5  6  7  8  9 10 11   2  3  4  5  6  7  8   2  3  4  5  6  7  8
            12 13 14 15 16 17 18   9 10 11 12 13 14 15   9 10 11 12 13 14 15
            19 20 21 22 23 24 25  16 17 18 19 20 21 22  16 17 18 19 20 21 22
            26 27 28 29 30 31     23 24 25 26 27 28     23 24 25 26 27 28 29
                                                        30 31

                   April                  May                   June
            Mo Tu We Th Fr Sa Su  Mo Tu We Th Fr Sa Su  Mo Tu We Th Fr Sa Su
                   1  2  3  4  5               1  2  3   1  2  3  4  5  6  7
             6  7  8  9 10 11 12   4  5  6  7  8  9 10   8  9 10 11 12 13 14
            13 14 15 16 17 18 19  11 12 13 14 15 16 17  15 16 17 18 19 20 21
            20 21 22 23 24 25 26  18 19 20 21 22 23 24  22 23 24 25 26 27 28
            27 28 29 30           25 26 27 28 29 30 31  29 30

                    July                 August              September
            Mo Tu We Th Fr Sa Su  Mo Tu We Th Fr Sa Su  Mo Tu We Th Fr Sa Su
                   1  2  3  4  5                  1  2      1  2  3  4  5  6
             6  7  8  9 10 11 12   3  4  5  6  7  8  9   7  8  9 10 11 12 13
            13 14 15 16 17 18 19  10 11 12 13 14 15 16  14 15 16 17 18 19 20
            20 21 22 23 24 25 26  17 18 19 20 21 22 23  21 22 23 24 25 26 27
            27 28 29 30 31        24 25 26 27 28 29 30  28 29 30
                                  31

                  October               November              December
            Mo Tu We Th Fr Sa Su  Mo Tu We Th Fr Sa Su  Mo Tu We Th Fr Sa Su
                      1  2  3  4                     1      1  2  3  4  5  6
             5  6  7  8  9 10 11   2  3  4  5  6  7  8   7  8  9 10 11 12 13
            12 13 14 15 16 17 18   9 10 11 12 13 14 15  14 15 16 17 18 19 20
            19 20 21 22 23 24 25  16 17 18 19 20 21 22  21 22 23 24 25 26 27
            26 27 28 29 30 31     23 24 25 26 27 28 29  28 29 30 31
                                  30
            """;

    private static List<String> lines(String text) {
        return text.lines().toList();
    }

    private static long count(String text, char wanted) {
        return text.chars()
                .filter(character -> character == wanted)
                .count();
    }

    /** The grid of one month cut out of the year layout: its weekday header and week rows, trailing spaces removed. */
    private static List<String> monthInsideYear(String year, int month) {
        var lines = lines(year);
        var bands = new ArrayList<List<String>>();
        var band = new ArrayList<String>();

        for (var line : lines.subList(2, lines.size())) {
            if (line.isEmpty()) {
                bands.add(band);
                band = new ArrayList<>();
                continue;
            }

            band.add(line);
        }

        bands.add(band);
        var ownBand = bands.get((month - 1) / 3);
        var start = (month - 1) % 3 * 22;
        var grid = new ArrayList<String>();

        for (var line : ownBand.subList(1, ownBand.size())) {
            var cut = line.length() <= start ? "" : line.substring(start, Math.min(line.length(), start + 20));

            if (!cut.isBlank()) {
                grid.add(cut.stripTrailing());
            }
        }

        return grid;
    }

    @Test
    void yearMonthAndDayPrintThatMonthWithTheDayInBrackets() {
        assertEquals(NOVEMBER_2026_ON_THE_18TH, CALENDAR.year(2026).month(11).day(18).print());
    }

    @Test
    void nothingGivenPrintsTheClockMonthWithTodayInBrackets() {
        assertEquals(NOVEMBER_2026_ON_THE_18TH, CALENDAR.print());
    }

    @Test
    void todayFollowsTheZoneOfTheClock() {
        var newYearInMadrid = Clock.fixed(Instant.parse("2026-12-31T23:30:00Z"), ZoneId.of("Europe/Madrid"));

        assertEquals("""
                    January 2027
                Mo Tu We Th Fr Sa Su
                           [ 1] 2  3
                 4  5  6  7  8  9 10
                11 12 13 14 15 16 17
                18 19 20 21 22 23 24
                25 26 27 28 29 30 31
                """, TextCalendar.of(newYearInMadrid).print());
    }

    @Test
    void monthAlonePrintsThatMonthOfTheClockYearWithNoDayMarked() {
        assertEquals(NOVEMBER_2026, CALENDAR.month(11).print());
        assertEquals("""
                   February 2026
                Mo Tu We Th Fr Sa Su
                                   1
                 2  3  4  5  6  7  8
                 9 10 11 12 13 14 15
                16 17 18 19 20 21 22
                23 24 25 26 27 28
                """, CALENDAR.month(2).print());
    }

    @Test
    void dayAloneMarksOnlyThatDayOfTheClockMonth() {
        var text = CALENDAR.day(5).print();

        assertEquals("""
                   November 2026
                Mo Tu We Th Fr Sa Su
                                   1
                 2  3  4[ 5] 6  7  8
                 9 10 11 12 13 14 15
                16 17 18 19 20 21 22
                23 24 25 26 27 28 29
                30
                """, text);
        assertEquals(1, count(text, '['));
        assertEquals(1, count(text, ']'));
    }

    @Test
    void yearAndMonthPrintThatMonthWithNoDayMarked() {
        assertEquals(NOVEMBER_2026, CALENDAR.year(2026).month(11).print());
        assertEquals("""
                   February 2024
                Mo Tu We Th Fr Sa Su
                          1  2  3  4
                 5  6  7  8  9 10 11
                12 13 14 15 16 17 18
                19 20 21 22 23 24 25
                26 27 28 29
                """, CALENDAR.year(2024).month(2).print());
    }

    @Test
    void monthAndDayPrintThatMonthOfTheClockYearWithTheDayMarked() {
        assertEquals("""
                     April 2026
                Mo Tu We Th Fr Sa Su
                       1  2  3  4  5
                 6  7  8  9 10 11 12
                13 14 15 16 17 18 19
                20 21 22 23 24 25 26
                27 28 29[30]
                """, CALENDAR.month(4).day(30).print());
    }

    @Test
    void yearAndDayPrintTheClockMonthOfThatYear() {
        assertEquals("""
                   November 2027
                Mo Tu We Th Fr Sa Su
                 1  2  3  4  5  6  7
                 8  9 10 11 12 13 14
                15 16 17[18]19 20 21
                22 23 24 25 26 27 28
                29 30
                """, CALENDAR.year(2027).day(18).print());
    }

    @Test
    void yearAlonePrintsTwelveMonthsThreeToARow() {
        assertEquals(YEAR_2026, CALENDAR.year(2026).print());
    }

    @Test
    void everyMonthOfTheYearLayoutIsTheGridOfThatMonth() {
        for (var year : new int[] {2023, 2024, 2026}) {
            var text = CALENDAR.year(year).print();

            for (var month = 1; month <= 12; month++) {
                var alone = lines(CALENDAR.year(year).month(month).print());

                assertEquals(alone.subList(1, alone.size()), monthInsideYear(text, month), year + "-" + month);
            }
        }
    }

    @Test
    void noLineEndsInASpaceAndEveryLineEndsInANewline() {
        for (var text : List.of(CALENDAR.print(), CALENDAR.day(30).print(), CALENDAR.year(2024).print())) {
            assertTrue(text.endsWith("\n"));
            assertFalse(text.contains("\r"));

            for (var line : lines(text)) {
                assertFalse(line.endsWith(" "), "[" + line + "]");
            }
        }
    }

    @Test
    void dayInTheFirstColumnMovesTheMonthOneColumnToTheRight() {
        assertEquals("""
                    November 2026
                 Mo Tu We Th Fr Sa Su
                                    1
                  2  3  4  5  6  7  8
                  9 10 11 12 13 14 15
                [16]17 18 19 20 21 22
                 23 24 25 26 27 28 29
                 30
                """, CALENDAR.day(16).print());
    }

    @Test
    void dayInTheLastColumnClosesItsBracketAfterTheRow() {
        assertEquals("16 17 18 19 20 21[22]", lines(CALENDAR.day(22).print()).get(5));
    }

    @Test
    void firstDayOfTheMonthOpensItsBracketInTheBlankBeforeIt() {
        assertEquals("                 [ 1]", lines(CALENDAR.day(1).print()).get(2));
    }

    @Test
    void monthOutsideOneToTwelveIsRejectedWithItsValue() {
        assertEquals("month must be between 1 and 12: 0",
                assertThrows(IllegalArgumentException.class, () -> CALENDAR.month(0)).getMessage());
        assertEquals("month must be between 1 and 12: 13",
                assertThrows(IllegalArgumentException.class, () -> CALENDAR.month(13)).getMessage());
    }

    @Test
    void dayOutsideOneToThirtyOneIsRejectedWithItsValue() {
        assertEquals("day must be between 1 and 31: 0",
                assertThrows(IllegalArgumentException.class, () -> CALENDAR.day(0)).getMessage());
        assertEquals("day must be between 1 and 31: 32",
                assertThrows(IllegalArgumentException.class, () -> CALENDAR.day(32)).getMessage());
    }

    @Test
    void dayTheResolvedMonthDoesNotHaveIsRejectedWhenPrinting() {
        var april31 = CALENDAR.month(4).day(31);
        var february29 = CALENDAR.year(2026).month(2).day(29);
        var november31 = CALENDAR.day(31);

        assertEquals("day does not exist in 2026-04: 31",
                assertThrows(IllegalArgumentException.class, april31::print).getMessage());
        assertEquals("day does not exist in 2026-02: 29",
                assertThrows(IllegalArgumentException.class, february29::print).getMessage());
        assertEquals("day does not exist in 2026-11: 31",
                assertThrows(IllegalArgumentException.class, november31::print).getMessage());
        assertEquals("26 27 28[29]", lines(CALENDAR.year(2024).month(2).day(29).print()).getLast());
    }

    @Test
    void yearOutsideTheRangeOfJavaTimeIsRejectedWithItsValue() {
        var message = assertThrows(IllegalArgumentException.class, () -> CALENDAR.year(1_000_000_000)).getMessage();

        assertTrue(message.endsWith(": 1000000000"), message);
        assertThrows(IllegalArgumentException.class, () -> CALENDAR.year(-1_000_000_000));
    }

    @Test
    void titleIsCentredForYearsOfAnyWidth() {
        assertEquals("     January -1", lines(CALENDAR.year(-1).month(1).print()).getFirst());
        assertEquals("   January 10000", lines(CALENDAR.year(10000).month(1).print()).getFirst());
        assertEquals("                              -10", lines(CALENDAR.year(-10).print()).getFirst());
    }

    @Test
    void nullArgumentsAreRejected() {
        assertThrows(NullPointerException.class, () -> TextCalendar.of(null));
        assertThrows(NullPointerException.class, () -> CALENDAR.firstDayOfWeek(null));
        assertThrows(NullPointerException.class, () -> CALENDAR.locale(null));
        assertThrows(NullPointerException.class, () -> CALENDAR.printTo(null));
    }

    @Test
    void weekCanStartOnAnotherDay() {
        assertEquals("""
                   November 2026
                Su Mo Tu We Th Fr Sa
                 1  2  3  4  5  6  7
                 8  9 10 11 12 13 14
                15 16 17 18 19 20 21
                22 23 24 25 26 27 28
                29 30
                """, CALENDAR.firstDayOfWeek(DayOfWeek.SUNDAY).month(11).print());
    }

    @Test
    void localeChangesTheNamesAndNotTheFirstDayOfTheWeek() {
        var german = CALENDAR.locale(Locale.GERMAN);

        assertEquals("""
                     März 2026
                Mo Di Mi Do Fr Sa So
                                   1
                 2  3  4  5  6  7  8
                 9 10 11 12 13 14 15
                16 17 18 19 20 21 22
                23 24 25 26 27 28 29
                30 31
                """, german.month(3).print());
        assertEquals("So Mo Di Mi Do Fr Sa", lines(german.firstDayOfWeek(DayOfWeek.SUNDAY).print()).get(1));
        assertEquals(german.firstDayOfWeek(DayOfWeek.SUNDAY).print(),
                CALENDAR.firstDayOfWeek(DayOfWeek.SUNDAY).locale(Locale.GERMAN).print());
        assertEquals("Mo Di Mi Do Fr Sa So  Mo Di Mi Do Fr Sa So  Mo Di Mi Do Fr Sa So",
                lines(german.year(2026).print()).get(3));
    }

    @Test
    void settingAPartLeavesTheOriginalAsItWas() {
        var onTheFifth = CALENDAR.day(5);

        assertEquals(NOVEMBER_2026_ON_THE_18TH, CALENDAR.print());
        assertTrue(onTheFifth.print().contains("[ 5]"));
    }

    @Test
    void printToAppendsToAStringBuilderAfterWhatItHolds() {
        var out = new StringBuilder("calendar:\n");
        CALENDAR.month(11).printTo(out);
        CALENDAR.year(2026).printTo(out);

        assertEquals("calendar:\n" + NOVEMBER_2026 + YEAR_2026, out.toString());
    }

    @Test
    void printToWritesTheSameTextToAnyAppendable() {
        var writer = new StringWriter();
        CALENDAR.printTo(writer);

        assertEquals(CALENDAR.print(), writer.toString());
    }

    @Test
    void failureOfTheAppendableIsUnchecked() {
        var failure = new IOException("disk full");
        var broken = new Appendable() {

            @Override
            public Appendable append(CharSequence text) throws IOException {
                throw failure;
            }

            @Override
            public Appendable append(CharSequence text, int start, int end) throws IOException {
                throw failure;
            }

            @Override
            public Appendable append(char character) throws IOException {
                throw failure;
            }
        };

        assertEquals(failure, assertThrows(UncheckedIOException.class, () -> CALENDAR.printTo(broken)).getCause());
    }
}
