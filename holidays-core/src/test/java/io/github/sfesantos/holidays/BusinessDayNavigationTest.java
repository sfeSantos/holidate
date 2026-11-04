package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

class BusinessDayNavigationTest {

    private static final HolidayCalendar SPAIN = HolidayCalendar.builder()
            .layer(Layer.named("ES")
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
                    .build())
            .build();

    private static final LocalDate CHRISTMAS_EVE_2026 = LocalDate.of(2026, 12, 24);
    private static final LocalDate MONDAY_AFTER_CHRISTMAS_2026 = LocalDate.of(2026, 12, 28);

    @Test
    void nextBusinessDaySkipsHolidaysAndWeekends() {
        assertEquals(MONDAY_AFTER_CHRISTMAS_2026, SPAIN.nextBusinessDay(CHRISTMAS_EVE_2026));
        assertEquals(MONDAY_AFTER_CHRISTMAS_2026, SPAIN.nextBusinessDay(LocalDate.of(2026, 12, 25)));
        assertEquals(LocalDate.of(2026, 12, 29), SPAIN.nextBusinessDay(MONDAY_AFTER_CHRISTMAS_2026));
    }

    @Test
    void nextOrSameKeepsABusinessDay() {
        assertEquals(MONDAY_AFTER_CHRISTMAS_2026, SPAIN.nextOrSameBusinessDay(MONDAY_AFTER_CHRISTMAS_2026));
        assertEquals(MONDAY_AFTER_CHRISTMAS_2026, SPAIN.nextOrSameBusinessDay(LocalDate.of(2026, 12, 25)));
    }

    @Test
    void previousBusinessDaySkipsHolidaysAndWeekends() {
        assertEquals(CHRISTMAS_EVE_2026, SPAIN.previousBusinessDay(MONDAY_AFTER_CHRISTMAS_2026));
        assertEquals(CHRISTMAS_EVE_2026, SPAIN.previousOrSameBusinessDay(LocalDate.of(2026, 12, 27)));
        assertEquals(CHRISTMAS_EVE_2026, SPAIN.previousOrSameBusinessDay(CHRISTMAS_EVE_2026));
    }

    @Test
    void navigationCrossesYearBoundaries() {
        var lastBusinessDay2026 = LocalDate.of(2026, 12, 31);
        var firstBusinessDay2027 = LocalDate.of(2027, 1, 4);

        assertEquals(firstBusinessDay2027, SPAIN.nextBusinessDay(lastBusinessDay2026));
        assertEquals(lastBusinessDay2026, SPAIN.previousBusinessDay(firstBusinessDay2027));
        assertEquals(lastBusinessDay2026, SPAIN.previousBusinessDay(LocalDate.of(2027, 1, 1)));
    }

    @Test
    void addBusinessDaysMatchesStepping() {
        var start = LocalDate.of(2026, 3, 11);

        assertEquals(start, SPAIN.addBusinessDays(start, 0));
        assertEquals(SPAIN.nextBusinessDay(start), SPAIN.addBusinessDays(start, 1));
        assertEquals(SPAIN.previousBusinessDay(start), SPAIN.addBusinessDays(start, -1));
        assertEquals(LocalDate.of(2026, 12, 30), SPAIN.addBusinessDays(CHRISTMAS_EVE_2026, 3));

        var forward = start;
        var backward = start;

        for (int n = 1; n <= 800; n++) {
            forward = SPAIN.nextBusinessDay(forward);
            backward = SPAIN.previousBusinessDay(backward);

            assertEquals(forward, SPAIN.addBusinessDays(start, n), "forward " + n);
            assertEquals(backward, SPAIN.addBusinessDays(start, -n), "backward " + n);
        }
    }

    @Test
    void addZeroKeepsANonBusinessDay() {
        var christmas = LocalDate.of(2026, 12, 25);

        assertEquals(christmas, SPAIN.addBusinessDays(christmas, 0));
    }

    @Test
    void businessDaysBetweenIsHalfOpen() {
        var monday = LocalDate.of(2026, 12, 21);

        assertEquals(4, SPAIN.businessDaysBetween(monday, MONDAY_AFTER_CHRISTMAS_2026));
        assertEquals(5, SPAIN.businessDaysBetween(monday, LocalDate.of(2026, 12, 29)));
        assertEquals(0, SPAIN.businessDaysBetween(monday, monday));
        assertEquals(-4, SPAIN.businessDaysBetween(MONDAY_AFTER_CHRISTMAS_2026, monday));
    }

    @Test
    void businessDaysBetweenAcrossYearsMatchesCounting() {
        var from = LocalDate.of(2025, 11, 14);
        var to = LocalDate.of(2028, 2, 9);
        long expected = 0;

        for (var day = from; day.isBefore(to); day = day.plusDays(1)) {
            if (SPAIN.isBusinessDay(day)) {
                expected++;
            }
        }

        assertEquals(expected, SPAIN.businessDaysBetween(from, to));
        assertEquals(-expected, SPAIN.businessDaysBetween(to, from));
        assertEquals(expected, SPAIN.businessDaysBetween(from, SPAIN.addBusinessDays(from, (int) expected)));
    }

    @Test
    void fridaySaturdayWeekend() {
        var gulf = HolidayCalendar.builder()
                .weekend(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
                .build();
        var thursday = LocalDate.of(2026, 3, 5);

        assertTrue(gulf.isBusinessDay(thursday.plusDays(3)));
        assertFalse(gulf.isBusinessDay(thursday.plusDays(1)));
        assertEquals(thursday.plusDays(3), gulf.nextBusinessDay(thursday));
        assertEquals(5, gulf.businessDaysBetween(thursday.plusDays(3), thursday.plusDays(10)));
    }

    @Test
    void calendarWithoutBusinessDaysFailsInsteadOfLooping() {
        var never = HolidayCalendar.builder()
                .weekend(DayOfWeek.values())
                .build();
        var date = LocalDate.of(2026, 3, 5);

        assertThrows(IllegalStateException.class, () -> never.nextBusinessDay(date));
        assertThrows(IllegalStateException.class, () -> never.previousBusinessDay(date));
        assertThrows(IllegalStateException.class, () -> never.addBusinessDays(date, 1));
        assertThrows(IllegalStateException.class, () -> never.addBusinessDays(date, -1));
        assertEquals(0, never.businessDaysBetween(date, date.plusYears(1)));
    }

    @Test
    void adjustersMoveTheDateAndKeepTheTime() {
        var eveningBeforeChristmas = LocalDateTime.of(2026, 12, 24, 18, 45);
        var zoned = ZonedDateTime.of(eveningBeforeChristmas, ZoneId.of("Europe/Madrid"));

        assertEquals(MONDAY_AFTER_CHRISTMAS_2026, CHRISTMAS_EVE_2026.with(SPAIN.nextBusinessDayAdjuster()));
        assertEquals(MONDAY_AFTER_CHRISTMAS_2026.atTime(18, 45), eveningBeforeChristmas.with(SPAIN.nextBusinessDayAdjuster()));
        assertEquals(MONDAY_AFTER_CHRISTMAS_2026, zoned.with(SPAIN.nextBusinessDayAdjuster()).toLocalDate());
        assertEquals(CHRISTMAS_EVE_2026, CHRISTMAS_EVE_2026.with(SPAIN.nextOrSameBusinessDayAdjuster()));
        assertEquals(CHRISTMAS_EVE_2026, MONDAY_AFTER_CHRISTMAS_2026.with(SPAIN.previousBusinessDayAdjuster()));
        assertEquals(CHRISTMAS_EVE_2026, LocalDate.of(2026, 12, 26).with(SPAIN.previousOrSameBusinessDayAdjuster()));
        assertEquals(LocalDate.of(2026, 12, 30), CHRISTMAS_EVE_2026.with(SPAIN.addBusinessDaysAdjuster(3)));
    }

    @Test
    void rejectsNullDates() {
        assertThrows(NullPointerException.class, () -> SPAIN.nextBusinessDay((LocalDate) null));
        assertThrows(NullPointerException.class, () -> SPAIN.nextOrSameBusinessDay(null));
        assertThrows(NullPointerException.class, () -> SPAIN.previousBusinessDay(null));
        assertThrows(NullPointerException.class, () -> SPAIN.previousOrSameBusinessDay(null));
        assertThrows(NullPointerException.class, () -> SPAIN.addBusinessDays((LocalDate) null, 1));
        assertThrows(NullPointerException.class, () -> SPAIN.businessDaysBetween(null, LocalDate.EPOCH));
        assertThrows(NullPointerException.class, () -> SPAIN.businessDaysBetween(LocalDate.EPOCH, null));
    }
}
