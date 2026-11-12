package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

class HolidayPeriodQueriesTest {

    private static final HolidayCalendar CALENDAR = HolidayCalendar.builder()
            .layer(Layer.named("x")
                    .add(Holiday.fixed("New Year", Month.JANUARY, 1),
                            Holiday.fixed("Epiphany", Month.JANUARY, 6),
                            Holiday.fixed("Labour Day", Month.MAY, 1),
                            Holiday.fixed("Month End", Month.MAY, 31),
                            Holiday.fixed("Christmas", Month.DECEMBER, 25))
                    .observance(ObservanceRule.sundayToMonday())
                    .build())
            .build();

    private static final HolidayCalendar OTHER = HolidayCalendar.builder()
            .layer(Layer.named("y")
                    .add(Holiday.fixed("Tiradentes", Month.APRIL, 21), Holiday.fixed("Natal", Month.DECEMBER, 25))
                    .build())
            .build();

    private static final LocalDate DEC_25 = LocalDate.of(2026, 12, 25);

    @Test
    void aMonthListsWhatIsObservedInIt() {
        assertEquals(List.of("New Year", "Epiphany"), names(CALENDAR.holidaysInMonth(YearMonth.of(2026, 1))));
        assertEquals(List.of("Christmas"), names(CALENDAR.holidaysInMonth(YearMonth.of(2026, 12))));
        assertEquals(List.of(), CALENDAR.holidaysInMonth(YearMonth.of(2026, 3)));
    }

    @Test
    void aHolidayMovedIntoTheNextMonthBelongsToThatMonth() {
        // 31 May 2026 is a Sunday, observed on Monday 1 June
        assertEquals(List.of("Labour Day"), names(CALENDAR.holidaysInMonth(YearMonth.of(2026, 5))));

        var june = CALENDAR.holidaysInMonth(YearMonth.of(2026, 6));

        assertEquals(List.of("Month End"), names(june));
        assertEquals(LocalDate.of(2026, 5, 31), june.get(0).original());
        assertEquals(LocalDate.of(2026, 6, 1), june.get(0).observed());
    }

    @Test
    void nextMonthsStartOnTheDateAndStopBeforeTheSameDayLater() {
        assertEquals(List.of("Christmas", "New Year", "Epiphany"), names(CALENDAR.holidaysInNextMonths(DEC_25, 1)));
        assertEquals(List.of("New Year", "Epiphany"), names(CALENDAR.holidaysInNextMonths(DEC_25.plusDays(1), 1)));
        assertEquals(List.of("Christmas", "New Year"), names(CALENDAR.holidaysInNextMonths(LocalDate.of(2026, 12, 6), 1)));
        assertEquals(List.of(), CALENDAR.holidaysInNextMonths(DEC_25, 0));
    }

    @Test
    void nextYearsCrossYearBoundariesInObservedOrder() {
        var observed = CALENDAR.holidaysInNextYears(LocalDate.of(2026, 5, 2), 2)
                .stream()
                .map(HolidayOccurrence::observed)
                .toList();

        assertEquals(List.of(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 12, 25),
                LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 6),
                LocalDate.of(2027, 5, 1),
                LocalDate.of(2027, 5, 31),
                LocalDate.of(2027, 12, 25),
                LocalDate.of(2028, 1, 1),
                LocalDate.of(2028, 1, 6),
                LocalDate.of(2028, 5, 1)), observed);
        assertEquals(List.of(), CALENDAR.holidaysInNextYears(DEC_25, 0));
    }

    @Test
    void theSameAnswerAsHolidaysBetween() {
        var from = LocalDate.of(2026, 3, 15);

        assertEquals(CALENDAR.holidaysBetween(LocalDate.of(2026, 12, 1), LocalDate.of(2027, 1, 1)),
                CALENDAR.holidaysInMonth(YearMonth.of(2026, 12)));
        assertEquals(CALENDAR.holidaysBetween(from, from.plusMonths(18)), CALENDAR.holidaysInNextMonths(from, 18));
        assertEquals(CALENDAR.holidaysBetween(from, from.plusYears(5)), CALENDAR.holidaysInNextYears(from, 5));
    }

    @Test
    void theWindowStartsOnTheClockDate() {
        var lateChristmasUtc = Instant.parse("2026-12-25T23:30:00Z");
        var utc = Clock.fixed(lateChristmasUtc, ZoneOffset.UTC);
        var madrid = Clock.fixed(lateChristmasUtc, ZoneId.of("Europe/Madrid"));

        assertEquals(List.of("Christmas", "New Year", "Epiphany"), names(CALENDAR.holidaysInNextMonths(utc, 1)));
        assertEquals(List.of("New Year", "Epiphany"), names(CALENDAR.holidaysInNextMonths(madrid, 1)));
        assertEquals(DEC_25, CALENDAR.holidaysInNextYears(utc, 1).get(0).observed());
        assertEquals(LocalDate.of(2027, 1, 1), CALENDAR.holidaysInNextYears(madrid, 1).get(0).observed());
    }

    @Test
    void aUnionListsEveryMember() {
        var both = HolidayCalendar.union(CALENDAR, OTHER);

        assertEquals(List.of("Tiradentes"), names(both.holidaysInMonth(YearMonth.of(2026, 4))));
        assertEquals(List.of("Christmas", "Natal"), names(both.holidaysInMonth(YearMonth.of(2026, 12))));
        assertEquals(List.of("Christmas", "Natal", "New Year", "Epiphany"), names(both.holidaysInNextMonths(DEC_25, 1)));
    }

    @Test
    void rejectsNullsAndNegativeCounts() {
        assertThrows(NullPointerException.class, () -> CALENDAR.holidaysInMonth(null));
        assertThrows(NullPointerException.class, () -> CALENDAR.holidaysInNextMonths((LocalDate) null, 1));
        assertThrows(NullPointerException.class, () -> CALENDAR.holidaysInNextYears((LocalDate) null, 1));
        assertThrows(NullPointerException.class, () -> CALENDAR.holidaysInNextMonths((Clock) null, 1));
        assertThrows(NullPointerException.class, () -> CALENDAR.holidaysInNextYears((Clock) null, 1));
        assertThrows(IllegalArgumentException.class, () -> CALENDAR.holidaysInNextMonths(DEC_25, -1));
        assertThrows(IllegalArgumentException.class, () -> CALENDAR.holidaysInNextYears(DEC_25, -1));
    }

    private static List<String> names(List<HolidayOccurrence> occurrences) {
        return occurrences.stream()
                .map(HolidayOccurrence::name)
                .toList();
    }
}
