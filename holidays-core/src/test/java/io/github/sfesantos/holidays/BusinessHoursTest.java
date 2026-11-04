package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class BusinessHoursTest {

    private static final LocalTime NINE = LocalTime.of(9, 0);
    private static final LocalTime HALF_PAST_FIVE = LocalTime.of(17, 30);
    private static final LocalTime ONE = LocalTime.of(13, 0);

    private static final Layer SPAIN = Layer.named("ES")
            .add(Holiday.fixed("Navidad", Month.DECEMBER, 25),
                    Holiday.fixed("Nochebuena", Month.DECEMBER, 24).closingAt(ONE),
                    Holiday.fixed("Nochevieja", Month.DECEMBER, 31).ofType(HolidayType.HALF_DAY))
            .build();

    private static final HolidayCalendar OFFICE = HolidayCalendar.builder()
            .businessHours(NINE, HALF_PAST_FIVE)
            .layer(SPAIN)
            .build();

    private static final HolidayCalendar ALL_DAY = HolidayCalendar.builder().layer(SPAIN).build();

    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 12, 23);
    private static final LocalDate CHRISTMAS_EVE = LocalDate.of(2026, 12, 24);
    private static final LocalDate CHRISTMAS = LocalDate.of(2026, 12, 25);
    private static final LocalDate SATURDAY = LocalDate.of(2026, 12, 26);
    private static final LocalDate NEW_YEARS_EVE = LocalDate.of(2026, 12, 31);

    @Test
    void hoursValidate() {
        assertThrows(IllegalArgumentException.class, () -> BusinessHours.of(HALF_PAST_FIVE, NINE));
        assertThrows(IllegalArgumentException.class, () -> BusinessHours.of(NINE, NINE));
        assertThrows(NullPointerException.class, () -> BusinessHours.of(null, NINE));
        assertTrue(BusinessHours.allDay().isAllDay());
        assertEquals("BusinessHours[09:00 to 17:30]", BusinessHours.of(NINE, HALF_PAST_FIVE).toString());
    }

    @Test
    void hoursAreOpenInclusiveCloseExclusive() {
        var hours = BusinessHours.of(NINE, HALF_PAST_FIVE);

        assertTrue(hours.contains(NINE));
        assertTrue(hours.contains(LocalTime.of(17, 29)));
        assertFalse(hours.contains(HALF_PAST_FIVE));
        assertFalse(hours.contains(LocalTime.of(8, 59)));
        assertTrue(BusinessHours.allDay().contains(LocalTime.MIDNIGHT));
        assertTrue(BusinessHours.allDay().contains(LocalTime.MAX));
    }

    @Test
    void defaultIsOpenAllDayOnBusinessDays() {
        assertEquals(BusinessHours.allDay(), ALL_DAY.businessHours());
        assertEquals(Optional.of(BusinessHours.allDay()), ALL_DAY.businessHoursOn(WEDNESDAY));
        assertTrue(ALL_DAY.isOpen(WEDNESDAY.atStartOfDay()));
        assertTrue(ALL_DAY.isOpen(WEDNESDAY.atTime(23, 59)));
        assertFalse(ALL_DAY.isOpen(CHRISTMAS.atTime(10, 0)));
        assertFalse(ALL_DAY.isOpen(SATURDAY.atTime(10, 0)));
    }

    @Test
    void configuredHoursApplyOnBusinessDays() {
        assertEquals(Optional.of(BusinessHours.of(NINE, HALF_PAST_FIVE)), OFFICE.businessHoursOn(WEDNESDAY));
        assertTrue(OFFICE.isOpen(WEDNESDAY.atTime(9, 0)));
        assertFalse(OFFICE.isOpen(WEDNESDAY.atTime(8, 59)));
        assertFalse(OFFICE.isOpen(WEDNESDAY.atTime(17, 30)));
        assertEquals(Optional.empty(), OFFICE.businessHoursOn(CHRISTMAS));
        assertEquals(Optional.empty(), OFFICE.businessHoursOn(SATURDAY));
    }

    @Test
    void halfDayClosesEarly() {
        assertEquals(Optional.of(BusinessHours.of(NINE, ONE)), OFFICE.businessHoursOn(CHRISTMAS_EVE));
        assertTrue(OFFICE.isBusinessDay(CHRISTMAS_EVE));
        assertTrue(OFFICE.isHalfDay(CHRISTMAS_EVE));
        assertFalse(OFFICE.isHalfDay(WEDNESDAY));
        assertFalse(OFFICE.isHalfDay(CHRISTMAS));
        assertTrue(OFFICE.isOpen(CHRISTMAS_EVE.atTime(12, 59)));
        assertFalse(OFFICE.isOpen(CHRISTMAS_EVE.atTime(13, 0)));
    }

    @Test
    void halfDayDefaultsToNoon() {
        assertEquals(Optional.of(BusinessHours.of(NINE, LocalTime.NOON)), OFFICE.businessHoursOn(NEW_YEARS_EVE));
        assertEquals(Optional.of(BusinessHours.of(LocalTime.MIDNIGHT, LocalTime.NOON)), ALL_DAY.businessHoursOn(NEW_YEARS_EVE));
        assertTrue(ALL_DAY.isHalfDay(NEW_YEARS_EVE));
        assertEquals(LocalTime.NOON, OFFICE.holidayOn(NEW_YEARS_EVE).orElseThrow().closesAt());
        assertTrue(OFFICE.holidayOn(NEW_YEARS_EVE).orElseThrow().isHalfDay());
    }

    @Test
    void halfDayClosingBeforeOpeningIsClosed() {
        var closedEve = HolidayCalendar.builder()
                .businessHours(NINE, HALF_PAST_FIVE)
                .layer(Layer.named("x").add(Holiday.fixed("Eve", Month.DECEMBER, 24).closingAt(LocalTime.of(8, 0))).build())
                .build();

        assertEquals(Optional.empty(), closedEve.businessHoursOn(CHRISTMAS_EVE));
        assertFalse(closedEve.isHalfDay(CHRISTMAS_EVE));
        assertTrue(closedEve.isBusinessDay(CHRISTMAS_EVE));
    }

    @Test
    void halfDayOnAWeekendIsClosed() {
        assertEquals(Optional.empty(), OFFICE.businessHoursOn(LocalDate.of(2027, 12, 25)));
        assertFalse(OFFICE.isHalfDay(LocalDate.of(2028, 12, 24)));
    }

    @Test
    void twoHalfDaysOnTheSameDateTakeTheEarlierClose() {
        var calendar = HolidayCalendar.builder()
                .businessHours(NINE, HALF_PAST_FIVE)
                .layers(Layer.named("a").add(Holiday.fixed("Late", Month.MARCH, 3).closingAt(LocalTime.of(15, 0))).build(),
                        Layer.named("b").add(Holiday.fixed("Early", Month.MARCH, 3).closingAt(LocalTime.of(11, 0))).build())
                .build();

        assertEquals(Optional.of(BusinessHours.of(NINE, LocalTime.of(11, 0))), calendar.businessHoursOn(LocalDate.of(2026, 3, 3)));
    }

    @Test
    void halfDayMadeNonWorkingIsClosed() {
        var strict = HolidayCalendar.builder()
                .businessHours(NINE, HALF_PAST_FIVE)
                .nonWorkingTypes(HolidayType.PUBLIC, HolidayType.HALF_DAY)
                .layer(SPAIN)
                .build();

        assertFalse(strict.isBusinessDay(CHRISTMAS_EVE));
        assertEquals(Optional.empty(), strict.businessHoursOn(CHRISTMAS_EVE));
    }

    @Test
    void instantsAreReadInTheGivenZone() {
        var halfPastEightUtc = Instant.parse("2026-12-23T08:30:00Z");

        assertTrue(OFFICE.isOpen(halfPastEightUtc, ZoneId.of("Europe/Madrid")));
        assertFalse(OFFICE.isOpen(halfPastEightUtc, ZoneId.of("Europe/London")));
    }

    @Test
    void unionHoursAreTheIntersection() {
        var late = HolidayCalendar.builder().businessHours(LocalTime.of(10, 0), LocalTime.of(18, 0)).build();
        var union = HolidayCalendar.union(OFFICE, late);
        var night = HolidayCalendar.builder().businessHours(LocalTime.of(20, 0), LocalTime.of(23, 0)).build();

        assertEquals(BusinessHours.of(LocalTime.of(10, 0), HALF_PAST_FIVE), union.businessHours());
        assertEquals(Optional.of(BusinessHours.of(LocalTime.of(10, 0), ONE)), union.businessHoursOn(CHRISTMAS_EVE));
        assertEquals(BusinessHours.of(NINE, HALF_PAST_FIVE), HolidayCalendar.union(OFFICE, ALL_DAY).businessHours());
        assertThrows(IllegalStateException.class, () -> HolidayCalendar.union(OFFICE, night).businessHours());
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> OFFICE.businessHoursOn(null));
        assertThrows(NullPointerException.class, () -> OFFICE.isOpen((LocalDateTime) null));
        assertThrows(NullPointerException.class, () -> OFFICE.isOpen(Instant.EPOCH, null));
        assertThrows(NullPointerException.class, () -> HolidayCalendar.builder().businessHours(null));
        assertThrows(NullPointerException.class, () -> Holiday.fixed("x", Month.MAY, 1).closingAt(null));
    }
}
