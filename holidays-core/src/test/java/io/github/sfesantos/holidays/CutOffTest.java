package io.github.sfesantos.holidays;

import static io.github.sfesantos.holidays.BusinessDayConvention.FOLLOWING;
import static io.github.sfesantos.holidays.BusinessDayConvention.PRECEDING;
import static io.github.sfesantos.holidays.BusinessDayConvention.UNADJUSTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class CutOffTest {

    private static final HolidayCalendar SPAIN = HolidayCalendar.builder()
            .layer(Layer.named("ES")
                    .add(Holiday.fixed("Año Nuevo", Month.JANUARY, 1), Holiday.fixed("Navidad", Month.DECEMBER, 25))
                    .build())
            .build();

    private static final ZoneId MADRID = ZoneId.of("Europe/Madrid");
    private static final CutOff MADRID_16H = CutOff.at(LocalTime.of(16, 0), MADRID);

    @Test
    void beforeTheCutOffBooksOnTheSameDay() {
        var wednesday = LocalDate.of(2026, 12, 23);

        assertEquals(wednesday, MADRID_16H.dateOf(wednesday.atTime(15, 59)));
        assertEquals(wednesday, MADRID_16H.dateOf(wednesday.atStartOfDay()));
    }

    @Test
    void atOrAfterTheCutOffBooksOnTheNextDay() {
        var wednesday = LocalDate.of(2026, 12, 23);

        assertEquals(wednesday.plusDays(1), MADRID_16H.dateOf(wednesday.atTime(16, 0)));
        assertEquals(wednesday.plusDays(1), MADRID_16H.dateOf(wednesday.atTime(23, 59)));
    }

    @Test
    void instantsAreReadInTheCutOffZone() {
        var halfPastThreeUtc = Instant.parse("2026-12-24T15:30:00Z");
        var london = CutOff.at(LocalTime.of(16, 0), ZoneId.of("Europe/London"));

        assertEquals(LocalDate.of(2026, 12, 25), MADRID_16H.dateOf(halfPastThreeUtc));
        assertEquals(LocalDate.of(2026, 12, 24), london.dateOf(halfPastThreeUtc));
    }

    @Test
    void calendarAppliesTheConventionToTheBookingDate() {
        var lateChristmasEve = LocalDateTime.of(2026, 12, 24, 16, 0);

        assertEquals(LocalDate.of(2026, 12, 25), SPAIN.adjust(lateChristmasEve, MADRID_16H, UNADJUSTED));
        assertEquals(LocalDate.of(2026, 12, 28), SPAIN.adjust(lateChristmasEve, MADRID_16H, FOLLOWING));
        assertEquals(LocalDate.of(2026, 12, 24), SPAIN.adjust(lateChristmasEve, MADRID_16H, PRECEDING));
        assertEquals(LocalDate.of(2026, 12, 24), SPAIN.adjust(LocalDateTime.of(2026, 12, 24, 15, 59), MADRID_16H, FOLLOWING));
    }

    @Test
    void fridayEveningSettlesOnMonday() {
        var fridayEvening = Instant.parse("2026-12-18T18:00:00Z");

        assertEquals(LocalDate.of(2026, 12, 21), SPAIN.adjust(fridayEvening, MADRID_16H, FOLLOWING));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> CutOff.at(null, MADRID));
        assertThrows(NullPointerException.class, () -> CutOff.at(LocalTime.NOON, null));
        assertThrows(NullPointerException.class, () -> MADRID_16H.dateOf((Instant) null));
        assertThrows(NullPointerException.class, () -> SPAIN.adjust(Instant.EPOCH, null, FOLLOWING));
        assertThrows(NullPointerException.class, () -> SPAIN.adjust(Instant.EPOCH, MADRID_16H, null));
    }
}
