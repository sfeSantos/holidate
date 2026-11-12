package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Rules moved by about a year land two years from their base year and must still be indexed. */
class FarRuleDatesTest {

    private static final HolidayRule DEC_31 = HolidayRule.fixed(Month.DECEMBER, 31);
    private static final HolidayRule JAN_1 = HolidayRule.fixed(Month.JANUARY, 1);

    @Test
    void theLastDayOfTheYearPlus366() {
        var calendar = calendarOf(DEC_31.plusDays(366));

        assertEquals(List.of(LocalDate.of(2028, 1, 1)), observedIn(calendar, 2026));
        assertTrue(calendar.isHoliday(LocalDate.of(2028, 1, 1)));
        assertEveryOccurrenceIsIndexed(calendar);
    }

    @Test
    void theFirstDayOfTheYearMinus366() {
        var calendar = calendarOf(JAN_1.plusDays(-366));

        assertEquals(List.of(LocalDate.of(2024, 12, 31)), observedIn(calendar, 2026));
        assertTrue(calendar.isHoliday(LocalDate.of(2024, 12, 31)));
        assertEveryOccurrenceIsIndexed(calendar);
    }

    @Test
    void theWidestEasterOffsets() {
        assertEveryOccurrenceIsIndexed(calendarOf(HolidayRule.easterRelative(366)));
        assertEveryOccurrenceIsIndexed(calendarOf(HolidayRule.easterRelative(-366)));
    }

    @Test
    void theWidestEasterOffsetShiftedAsFarAgain() {
        var later = calendarOf(HolidayRule.easterRelative(366).plusDays(366));
        var earlier = calendarOf(HolidayRule.easterRelative(-366).plusDays(-366));

        assertEquals(List.of(LocalDate.of(2028, 4, 6)), observedIn(later, 2026));
        assertEquals(List.of(LocalDate.of(2024, 4, 3)), observedIn(earlier, 2026));
        assertEveryOccurrenceIsIndexed(later);
        assertEveryOccurrenceIsIndexed(earlier);
    }

    @Test
    void shiftsThatCancelOutStayWhereTheyWere() {
        var calendar = calendarOf(DEC_31.plusDays(366).plusDays(-366));

        assertEquals(List.of(LocalDate.of(2026, 12, 31)), observedIn(calendar, 2026));
        assertEveryOccurrenceIsIndexed(calendar);
    }

    @Test
    void observanceCanCarryAFarDateOneYearFurther() {
        var calendar = HolidayCalendar.builder()
                .layer(Layer.named("x")
                        .add(Holiday.of("Far", DEC_31.plusDays(366)))
                        .observance(ObservanceRule.sundayToMonday())
                        .build())
                .build();
        var moved = calendar.holidayOn(LocalDate.of(2029, 1, 1)).orElseThrow();

        assertEquals(LocalDate.of(2028, 12, 31), moved.original());
        assertEquals(moved, calendar.holidaysIn(Year.of(2027)).get(0));
        assertFalse(calendar.isHoliday(LocalDate.of(2028, 12, 31)));
        assertEveryOccurrenceIsIndexed(calendar);
    }

    @Test
    void aUnionSeesTheFarDatesOfItsMembers() {
        var union = HolidayCalendar.union(calendarOf(DEC_31.plusDays(366)), calendarOf(HolidayRule.fixed(Month.MAY, 1)));

        assertTrue(union.isHoliday(LocalDate.of(2028, 1, 1)));
        assertFalse(union.isBusinessDay(LocalDate.of(2028, 1, 1)));
        assertEquals(2, union.holidaysBetween(LocalDate.of(2028, 1, 1), LocalDate.of(2028, 5, 2)).size());
    }

    private static HolidayCalendar calendarOf(HolidayRule rule) {
        return HolidayCalendar.builder()
                .layer(Layer.named("x")
                        .add(Holiday.of("Far", rule))
                        .build())
                .build();
    }

    private static List<LocalDate> observedIn(HolidayCalendar calendar, int year) {
        return calendar.holidaysIn(Year.of(year))
                .stream()
                .map(HolidayOccurrence::observed)
                .toList();
    }

    private static void assertEveryOccurrenceIsIndexed(HolidayCalendar calendar) {
        for (int year = 2020; year <= 2030; year++) {
            var occurrences = calendar.holidaysIn(Year.of(year));

            assertFalse(occurrences.isEmpty(), "no occurrence in " + year);

            for (var occurrence : occurrences) {
                var day = occurrence.observed();

                assertTrue(calendar.isHoliday(day), day.toString());
                assertEquals(occurrence, calendar.holidayOn(day).orElseThrow());
                assertEquals(List.of(occurrence), calendar.holidaysBetween(day, day.plusDays(1)));
                assertEquals(occurrence, calendar.nextHolidayInclusive(day).orElseThrow());
                assertEquals(occurrence, calendar.nextHoliday(day.minusDays(1)).orElseThrow());
                assertEquals(occurrence, calendar.previousHoliday(day.plusDays(1)).orElseThrow());
                assertFalse(calendar.isBusinessDay(day), day.toString());
                assertEquals(0, calendar.businessDaysBetween(day, day.plusDays(1)));
            }
        }
    }
}
