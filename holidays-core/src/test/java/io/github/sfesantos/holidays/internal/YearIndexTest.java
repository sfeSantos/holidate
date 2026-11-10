package io.github.sfesantos.holidays.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.Holiday;
import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.HolidayCalendarBuilder;
import io.github.sfesantos.holidays.HolidayOccurrence;
import io.github.sfesantos.holidays.HolidayRule;
import io.github.sfesantos.holidays.HolidayType;
import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.ObservanceRule;

class YearIndexTest {

    private static final Holiday NEW_YEAR = Holiday.fixed("New Year", Month.JANUARY, 1);
    private static final Holiday CHRISTMAS = Holiday.fixed("Christmas", Month.DECEMBER, 25);
    private static final Holiday BOXING_DAY = Holiday.fixed("Boxing Day", Month.DECEMBER, 26);

    private static CompiledCalendar compile(HolidayCalendarBuilder builder) {
        return assertInstanceOf(CompiledCalendar.class, builder.build());
    }

    private static CompiledCalendar calendarWith(ObservanceRule observance, Holiday... holidays) {
        return compile(HolidayCalendar.builder()
                .layer(Layer.named("test").add(holidays).build())
                .observance(observance));
    }

    @Test
    void fixedHolidaySetsBitsByDayAndOccurrences() {
        var index = calendarWith(ObservanceRule.none(), CHRISTMAS).index(2026);
        int christmas = LocalDate.of(2026, 12, 25).getDayOfYear();

        assertTrue(index.isHoliday(christmas));
        assertTrue(index.isNonWorkingHoliday(christmas));
        assertFalse(index.isHoliday(christmas - 1));
        assertNull(index.holidayOn(christmas - 1));
        assertEquals("Christmas", index.holidayOn(christmas).name());
        assertEquals(List.of(new HolidayOccurrence("Christmas", LocalDate.of(2026, 12, 25),
                LocalDate.of(2026, 12, 25), HolidayType.PUBLIC, "test")), index.occurrences());
    }

    @Test
    void observanceTypeIsAHolidayButNotNonWorking() {
        var valentines = Holiday.fixed("Valentine's Day", Month.FEBRUARY, 14).ofType(HolidayType.OBSERVANCE);
        var index = calendarWith(ObservanceRule.none(), valentines).index(2026);
        int day = LocalDate.of(2026, 2, 14).getDayOfYear();

        assertTrue(index.isHoliday(day));
        assertFalse(index.isNonWorkingHoliday(day));
    }

    @Test
    void holidayObservanceOverridesTheCalendarRule() {
        var pinned = Holiday.fixed("Pinned", Month.DECEMBER, 25).withObservance(ObservanceRule.none());
        var index = calendarWith(ObservanceRule.weekendToMonday(), pinned).index(2021);

        assertEquals(LocalDate.of(2021, 12, 25), index.occurrences().get(0).observed());
    }

    @Test
    void collidingHolidaysChainForward() {
        var index = calendarWith(ObservanceRule.weekendToMonday(), CHRISTMAS, BOXING_DAY).index(2021);

        assertEquals(LocalDate.of(2021, 12, 27), index.occurrences().get(0).observed());
        assertEquals(LocalDate.of(2021, 12, 28), index.occurrences().get(1).observed());
        assertEquals("Christmas", index.holidayOn(361).name());
        assertEquals("Boxing Day", index.holidayOn(362).name());
    }

    @Test
    void spilloverIntoThePreviousYearIsIndexedThere() {
        var calendar = calendarWith(ObservanceRule.saturdayToFridaySundayToMonday(), NEW_YEAR);
        var previous = calendar.index(2021);
        var current = calendar.index(2022);

        assertTrue(previous.isNonWorkingHoliday(365));
        assertEquals(LocalDate.of(2022, 1, 1), previous.holidayOn(365).original());
        assertFalse(current.isHoliday(1));
        assertEquals(LocalDate.of(2021, 12, 31), current.occurrences().get(0).observed());
    }

    @Test
    void shiftIntoTheNextYearBelongsToTheYearOfTheRule() {
        var dayAfterNewYearsEve = Holiday.of("Day after New Year's Eve", HolidayRule.fixed(Month.DECEMBER, 31).plusDays(1));
        var calendar = calendarWith(ObservanceRule.none(), dayAfterNewYearsEve);
        var occurrences = calendar.index(2026).occurrences();

        assertEquals(1, occurrences.size());
        assertEquals(LocalDate.of(2027, 1, 1), occurrences.get(0).original());
        assertTrue(calendar.index(2027).isHoliday(1));
        assertEquals(LocalDate.of(2027, 1, 1), calendar.index(2027).holidayOn(1).original());
        assertEquals(List.of(LocalDate.of(2028, 1, 1)), calendar.index(2027).occurrences().stream().map(HolidayOccurrence::original).toList());
    }

    @Test
    void businessDayPrefixSumsSkipWeekendsAndNonWorkingHolidays() {
        var index = calendarWith(ObservanceRule.none(), NEW_YEAR, CHRISTMAS).index(2026);

        assertEquals(0, index.businessDaysBefore(1));
        assertEquals(0, index.businessDaysBefore(2));
        assertEquals(1, index.businessDaysBefore(3));
        assertEquals(259, index.businessDaysInYear());
    }

    @Test
    void businessDayCountsAndRanksAgreeOnEveryDayOfTheYear() {
        var index = calendarWith(ObservanceRule.none(), NEW_YEAR, CHRISTMAS, BOXING_DAY).index(2024);
        int seen = 0;

        for (int day = 1; day <= index.length(); day++) {
            assertEquals(seen, index.businessDaysBefore(day), "before day " + day);

            if (index.isBusinessDay(day)) {
                seen++;
                assertEquals(day, index.nthBusinessDay(seen), "rank " + seen);
            }
        }

        assertEquals(seen, index.businessDaysBefore(index.length() + 1));
        assertEquals(seen, index.businessDaysInYear());
    }

    @Test
    void holidayOnFindsOnlyObservedDays() {
        var index = calendarWith(ObservanceRule.none(), NEW_YEAR, CHRISTMAS, BOXING_DAY).index(2026);

        assertEquals("New Year", index.holidayOn(1).name());
        assertNull(index.holidayOn(2));
        assertNull(index.holidayOn(358));
        assertEquals("Christmas", index.holidayOn(359).name());
        assertEquals("Boxing Day", index.holidayOn(360).name());
        assertNull(index.holidayOn(365));
    }

    @Test
    void emptyCalendarOnlyKnowsWeekends() {
        var index = compile(HolidayCalendar.builder()).index(2026);

        assertEquals(List.of(), index.occurrences());
        assertEquals(0, index.byObserved().length);
        assertEquals(261, index.businessDaysInYear());
    }

    @Test
    void leapYearHasThreeHundredSixtySixDays() {
        assertEquals(366, compile(HolidayCalendar.builder()).index(2028).length());
        assertEquals(365, compile(HolidayCalendar.builder()).index(2027).length());
    }
}
