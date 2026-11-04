package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class ClockQueriesTest {

    private static final HolidayCalendar CALENDAR = HolidayCalendar.builder()
            .layer(Layer.named("x")
                    .add(Holiday.fixed("New Year", Month.JANUARY, 1), Holiday.fixed("Christmas", Month.DECEMBER, 25))
                    .build())
            .build();

    private static final Clock CHRISTMAS_MORNING = Clock.fixed(Instant.parse("2026-12-25T09:00:00Z"), ZoneOffset.UTC);

    @Test
    void todayComesFromTheClock() {
        assertTrue(CALENDAR.isHolidayToday(CHRISTMAS_MORNING));
        assertEquals("Christmas", CALENDAR.holidayToday(CHRISTMAS_MORNING).orElseThrow().name());
        assertFalse(CALENDAR.isBusinessDayToday(CHRISTMAS_MORNING));
    }

    @Test
    void navigationFromTheClock() {
        assertEquals(LocalDate.of(2027, 1, 1), CALENDAR.nextHoliday(CHRISTMAS_MORNING).orElseThrow().observed());
        assertEquals(7, CALENDAR.daysUntilNextHoliday(CHRISTMAS_MORNING).orElseThrow());
        assertEquals(LocalDate.of(2026, 12, 28), CALENDAR.nextBusinessDay(CHRISTMAS_MORNING));
        assertEquals(LocalDate.of(2026, 12, 30), CALENDAR.addBusinessDays(CHRISTMAS_MORNING, 3));
    }

    @Test
    void theClockZoneDecidesWhichDayItIs() {
        var lateChristmasUtc = Instant.parse("2026-12-25T23:30:00Z");

        assertTrue(CALENDAR.isHolidayToday(Clock.fixed(lateChristmasUtc, ZoneOffset.UTC)));
        assertFalse(CALENDAR.isHolidayToday(Clock.fixed(lateChristmasUtc, ZoneId.of("Europe/Madrid"))));
    }

    @Test
    void rejectsNullClock() {
        assertThrows(NullPointerException.class, () -> CALENDAR.isHolidayToday(null));
        assertThrows(NullPointerException.class, () -> CALENDAR.nextBusinessDay((Clock) null));
    }
}
