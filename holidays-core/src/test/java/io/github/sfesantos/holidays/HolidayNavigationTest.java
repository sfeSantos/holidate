package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class HolidayNavigationTest {

    private static final HolidayCalendar MALAGA = HolidayCalendar.builder()
            .weekend(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            .layers(Layer.named("ES")
                            .add(Holiday.fixed("Año Nuevo", Month.JANUARY, 1),
                                    Holiday.fixed("Epifanía", Month.JANUARY, 6),
                                    Holiday.easterRelative("Viernes Santo", -2),
                                    Holiday.fixed("Fiesta del Trabajo", Month.MAY, 1),
                                    Holiday.fixed("Asunción", Month.AUGUST, 15),
                                    Holiday.fixed("Fiesta Nacional", Month.OCTOBER, 12),
                                    Holiday.fixed("Todos los Santos", Month.NOVEMBER, 1),
                                    Holiday.fixed("Constitución", Month.DECEMBER, 6),
                                    Holiday.fixed("Inmaculada", Month.DECEMBER, 8),
                                    Holiday.fixed("Navidad", Month.DECEMBER, 25))
                            .build(),
                    Layer.named("ES-AN")
                            .add(Holiday.fixed("Día de Andalucía", Month.FEBRUARY, 28))
                            .add(Holiday.easterRelative("Jueves Santo", -3))
                            .observance(ObservanceRule.sundayToMonday())
                            .build())
            .build();

    private static final HolidayCalendar EMPTY = HolidayCalendar.builder().build();

    @Test
    void nextHolidayIsExclusiveAndInclusiveVariantIsNot() {
        var newYear = LocalDate.of(2026, 1, 1);

        assertEquals("Epifanía", MALAGA.nextHoliday(newYear).orElseThrow().name());
        assertEquals("Año Nuevo", MALAGA.nextHolidayInclusive(newYear).orElseThrow().name());
    }

    @Test
    void nextHolidayCrossesIntoTheFollowingYear() {
        var next = MALAGA.nextHoliday(LocalDate.of(2026, 12, 25)).orElseThrow();

        assertEquals("Año Nuevo", next.name());
        assertEquals(LocalDate.of(2027, 1, 1), next.observed());
    }

    @Test
    void nextHolidayUsesObservedDates() {
        var sunday = LocalDate.of(2026, 11, 1);
        var next = MALAGA.nextHoliday(sunday).orElseThrow();

        assertEquals("Todos los Santos", next.name());
        assertEquals(sunday, next.original());
        assertEquals(sunday.plusDays(1), next.observed());
    }

    @Test
    void previousHolidayIsExclusiveAndCrossesYears() {
        var newYear = LocalDate.of(2027, 1, 1);

        assertEquals(LocalDate.of(2026, 12, 25), MALAGA.previousHoliday(newYear).orElseThrow().observed());
        assertEquals(newYear, MALAGA.previousHolidayInclusive(newYear).orElseThrow().observed());
    }

    @Test
    void previousHolidayReturnsTheSameOccurrenceAsHolidayOn() {
        var calendar = HolidayCalendar.builder()
                .layer(Layer.named("x")
                        .add(Holiday.fixed("First", Month.MARCH, 3), Holiday.fixed("Second", Month.MARCH, 3))
                        .build())
                .build();
        var day = LocalDate.of(2026, 3, 3);

        assertEquals(calendar.holidayOn(day), calendar.previousHoliday(day.plusDays(1)));
        assertEquals(calendar.holidayOn(day), calendar.nextHoliday(day.minusDays(1)));
    }

    @Test
    void daysUntilNextHolidayCountsFromTheGivenDate() {
        assertEquals(5, MALAGA.daysUntilNextHoliday(LocalDate.of(2026, 12, 20)).orElseThrow());
        assertEquals(7, MALAGA.daysUntilNextHoliday(LocalDate.of(2026, 12, 25)).orElseThrow());
    }

    @Test
    void emptyCalendarHasNoNeighbouringHolidays() {
        var date = LocalDate.of(2026, 6, 1);

        assertTrue(EMPTY.nextHoliday(date).isEmpty());
        assertTrue(EMPTY.nextHolidayInclusive(date).isEmpty());
        assertTrue(EMPTY.previousHoliday(date).isEmpty());
        assertTrue(EMPTY.previousHolidayInclusive(date).isEmpty());
        assertTrue(EMPTY.daysUntilNextHoliday(date).isEmpty());
        assertEquals(List.of(), EMPTY.holidaysBetween(date, date.plusYears(1)));
    }

    @Test
    void searchStopsAtTheHorizonWhenRulesNoLongerFire() {
        var calendar = HolidayCalendar.builder()
                .layer(Layer.named("x")
                        .add(Holiday.fixed("Gone", Month.JULY, 4).validUntil(Year.of(2020)))
                        .build())
                .build();
        var date = LocalDate.of(2026, 1, 1);

        assertTrue(calendar.nextHoliday(date).isEmpty());
        assertEquals(LocalDate.of(2020, 7, 4), calendar.previousHoliday(date).orElseThrow().observed());
        assertTrue(calendar.previousHoliday(LocalDate.of(2200, 1, 1)).isEmpty());
    }

    @Test
    void holidaysBetweenIsHalfOpenAndOrderedByObservedDate() {
        var from = LocalDate.of(2026, 12, 1);
        var observed = MALAGA.holidaysBetween(from, LocalDate.of(2027, 1, 2))
                .stream()
                .map(HolidayOccurrence::observed)
                .toList();

        assertEquals(List.of(LocalDate.of(2026, 12, 7), LocalDate.of(2026, 12, 8),
                LocalDate.of(2026, 12, 25), LocalDate.of(2027, 1, 1)), observed);
        assertEquals(3, MALAGA.holidaysBetween(from, LocalDate.of(2027, 1, 1)).size());
        assertEquals(List.of(), MALAGA.holidaysBetween(from, from));
        assertEquals(List.of(), MALAGA.holidaysBetween(from, from.minusDays(1)));
    }

    @Test
    void holidaysBetweenStartsOnTheFromDate() {
        var christmas = LocalDate.of(2026, 12, 25);

        assertEquals(1, MALAGA.holidaysBetween(christmas, christmas.plusDays(1)).size());
    }

    @Test
    void queriesWorkOnAnyTemporalWithADate() {
        var christmasMorning = LocalDateTime.of(2026, 12, 25, 9, 30);

        assertEquals(true, christmasMorning.query(MALAGA.isHolidayQuery()));
        assertEquals(false, christmasMorning.query(MALAGA.isBusinessDayQuery()));
        assertEquals("Navidad", christmasMorning.query(MALAGA.holidayOnQuery()).orElseThrow().name());
        assertEquals(Optional.empty(), LocalDate.of(2026, 3, 3).query(MALAGA.holidayOnQuery()));
    }

    @Test
    void rejectsNullDates() {
        assertThrows(NullPointerException.class, () -> MALAGA.nextHoliday(null));
        assertThrows(NullPointerException.class, () -> MALAGA.nextHolidayInclusive(null));
        assertThrows(NullPointerException.class, () -> MALAGA.previousHoliday(null));
        assertThrows(NullPointerException.class, () -> MALAGA.previousHolidayInclusive(null));
        assertThrows(NullPointerException.class, () -> MALAGA.daysUntilNextHoliday(null));
        assertThrows(NullPointerException.class, () -> MALAGA.holidaysBetween(null, LocalDate.EPOCH));
        assertThrows(NullPointerException.class, () -> MALAGA.holidaysBetween(LocalDate.EPOCH, null));
    }
}
