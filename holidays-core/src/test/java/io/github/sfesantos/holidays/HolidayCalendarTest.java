package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.internal.CompiledCalendar;

class HolidayCalendarTest {

    private static final Layer SPAIN = Layer.named("ES")
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
            .build();

    private static final Layer ANDALUCIA = Layer.named("ES-AN")
            .add(Holiday.fixed("Día de Andalucía", Month.FEBRUARY, 28))
            .add(Holiday.easterRelative("Jueves Santo", -3))
            .observance(ObservanceRule.sundayToMonday())
            .build();

    private static final Layer MALAGA = Layer.named("ES-AN-MA")
            .add(Holiday.fixed("San Ciriaco y Santa Paula", Month.JUNE, 18))
            .add(Holiday.fixed("Virgen de la Victoria", Month.SEPTEMBER, 8))
            .build();

    private static final HolidayCalendar MALAGA_CALENDAR = HolidayCalendar.builder()
            .weekend(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            .layers(SPAIN, ANDALUCIA, MALAGA)
            .build();

    @Test
    void holidaysComeFromAllLayers() {
        var names = MALAGA_CALENDAR.holidaysIn(Year.of(2026))
                .stream()
                .map(HolidayOccurrence::name)
                .toList();

        assertEquals(14, names.size());
        assertEquals("Año Nuevo", names.get(0));
        assertTrue(names.contains("Jueves Santo"));
        assertTrue(names.contains("Virgen de la Victoria"));
        assertEquals("Navidad", names.get(13));
    }

    @Test
    void easterRelativeHolidaysFollowEaster() {
        assertTrue(MALAGA_CALENDAR.isHoliday(LocalDate.of(2026, 4, 2)));
        assertTrue(MALAGA_CALENDAR.isHoliday(LocalDate.of(2026, 4, 3)));
        assertFalse(MALAGA_CALENDAR.isHoliday(LocalDate.of(2026, 4, 6)));
    }

    @Test
    void sundayHolidayIsObservedOnMonday() {
        var sunday = LocalDate.of(2027, 2, 28);
        var monday = sunday.plusDays(1);

        assertFalse(MALAGA_CALENDAR.isHoliday(sunday));
        assertTrue(MALAGA_CALENDAR.isHoliday(monday));

        var occurrence = MALAGA_CALENDAR.holidayOn(monday).orElseThrow();

        assertEquals("Día de Andalucía", occurrence.name());
        assertEquals(sunday, occurrence.original());
        assertEquals(monday, occurrence.observed());
        assertEquals("ES-AN", occurrence.layer());
        assertTrue(occurrence.isMoved());
    }

    @Test
    void holidayOnIsEmptyOnOrdinaryDays() {
        assertTrue(MALAGA_CALENDAR.holidayOn(LocalDate.of(2026, 3, 3)).isEmpty());
    }

    @Test
    void businessDaysExcludeWeekendsAndHolidays() {
        assertFalse(MALAGA_CALENDAR.isBusinessDay(LocalDate.of(2026, 12, 25)));
        assertFalse(MALAGA_CALENDAR.isBusinessDay(LocalDate.of(2026, 12, 26)));
        assertTrue(MALAGA_CALENDAR.isBusinessDay(LocalDate.of(2026, 12, 28)));
        assertTrue(MALAGA_CALENDAR.isWeekend(LocalDate.of(2026, 12, 26)));
        assertFalse(MALAGA_CALENDAR.isWeekend(LocalDate.of(2026, 12, 28)));
    }

    @Test
    void observanceTypeHolidaysDoNotBlockBusinessDays() {
        var calendar = HolidayCalendar.builder()
                .layer(Layer.named("x")
                        .add(Holiday.fixed("Remembrance", Month.NOVEMBER, 11).ofType(HolidayType.OBSERVANCE))
                        .build())
                .build();
        var wednesday = LocalDate.of(2026, 11, 11);

        assertTrue(calendar.isHoliday(wednesday));
        assertTrue(calendar.isBusinessDay(wednesday));
    }

    @Test
    void emptyCalendarOnlyHasWeekends() {
        var calendar = HolidayCalendar.builder().build();

        assertEquals(List.of(), calendar.holidaysIn(Year.of(2026)));
        assertFalse(calendar.isHoliday(LocalDate.of(2026, 12, 25)));
        assertTrue(calendar.isBusinessDay(LocalDate.of(2026, 12, 25)));
    }

    @Test
    void aLunarHolidayMayOccurTwiceInAYear() {
        var calendar = HolidayCalendar.builder()
                .layer(Layer.named("x").add(Holiday.hijri("Islamic New Year", 1, 1)).build())
                .build();
        var january = LocalDate.of(2008, 1, 10);
        var december = LocalDate.of(2008, 12, 29);

        assertEquals(2, calendar.holidaysIn(Year.of(2008)).size());
        assertTrue(calendar.isHoliday(january));
        assertTrue(calendar.isHoliday(december));
        assertEquals(december, calendar.nextHoliday(january).orElseThrow().observed());
        assertEquals(1, calendar.holidaysIn(Year.of(2026)).size());
    }

    private static HolidayCalendar keepingChristmas() {
        return HolidayCalendar.builder()
                .layer(Layer.named("x")
                        .add(Holiday.fixed("Christmas Day", Month.DECEMBER, 25),
                                Holiday.fixed("Boxing Day", Month.DECEMBER, 26),
                                Holiday.fixed("New Year's Eve", Month.DECEMBER, 31))
                        .observance(ObservanceRule.weekendAddsMonday())
                        .build())
                .build();
    }

    private static List<LocalDate> observedDates(HolidayCalendar calendar, int year, String name) {
        return calendar.holidaysIn(Year.of(year))
                .stream()
                .filter(occurrence -> occurrence.name().equals(name))
                .map(HolidayOccurrence::observed)
                .toList();
    }

    @Test
    void aKeptHolidayOnSaturdayStaysAndAddsTheNextFreeWeekday() {
        var calendar = keepingChristmas();
        var christmas = LocalDate.of(2027, 12, 25);

        assertEquals(List.of(christmas, LocalDate.of(2027, 12, 27)), observedDates(calendar, 2027, "Christmas Day"));
        assertEquals(List.of(LocalDate.of(2027, 12, 26), LocalDate.of(2027, 12, 28)),
                observedDates(calendar, 2027, "Boxing Day"));
        assertTrue(calendar.isHoliday(christmas));
        assertFalse(calendar.holidayOn(christmas).orElseThrow().isMoved());
        assertEquals(christmas, calendar.holidayOn(LocalDate.of(2027, 12, 27)).orElseThrow().original());
        assertEquals("Boxing Day", calendar.holidayOn(LocalDate.of(2027, 12, 28)).orElseThrow().name());
        assertFalse(calendar.isBusinessDay(LocalDate.of(2027, 12, 28)));
        assertTrue(calendar.isBusinessDay(LocalDate.of(2027, 12, 29)));
    }

    @Test
    void theAddedDaySkipsAHolidayKeptOnAWeekday() {
        var calendar = keepingChristmas();

        assertEquals(List.of(LocalDate.of(2033, 12, 25), LocalDate.of(2033, 12, 27)),
                observedDates(calendar, 2033, "Christmas Day"));
        assertEquals(List.of(LocalDate.of(2033, 12, 26)), observedDates(calendar, 2033, "Boxing Day"));
        assertTrue(calendar.isBusinessDay(LocalDate.of(2033, 12, 28)));
    }

    @Test
    void aKeptHolidayOnAWeekdayOccursOnce() {
        var calendar = keepingChristmas();

        assertEquals(List.of(LocalDate.of(2025, 12, 25)), observedDates(calendar, 2025, "Christmas Day"));
        assertEquals(List.of(LocalDate.of(2025, 12, 26)), observedDates(calendar, 2025, "Boxing Day"));
    }

    @Test
    void theAddedDayMayFallInTheNextYear() {
        var calendar = keepingChristmas();
        var added = LocalDate.of(2023, 1, 2);

        assertEquals(List.of(LocalDate.of(2022, 12, 31), added), observedDates(calendar, 2022, "New Year's Eve"));
        assertEquals("New Year's Eve", calendar.holidayOn(added).orElseThrow().name());
        assertFalse(calendar.isBusinessDay(added));
    }

    @Test
    void aKeptHolidayThatIsNotADayOffAddsItsDayWithoutBlockingOthers() {
        var calendar = HolidayCalendar.builder()
                .layer(Layer.named("x")
                        .add(Holiday.fixed("Remembrance", Month.DECEMBER, 25).ofType(HolidayType.OBSERVANCE),
                                Holiday.fixed("Boxing Day", Month.DECEMBER, 26))
                        .observance(ObservanceRule.weekendAddsMonday())
                        .build())
                .build();

        assertEquals(2, observedDates(calendar, 2027, "Remembrance").size());
        assertEquals(List.of(LocalDate.of(2027, 12, 26), LocalDate.of(2027, 12, 27)),
                observedDates(calendar, 2027, "Boxing Day"));
    }

    @Test
    void aHolidayKeepsItsNameAcrossItsPeriods() {
        var calendar = HolidayCalendar.builder()
                .layer(Layer.named("XX")
                        .add(Holiday.of("Statehood Day",
                                HolidayPeriod.of(HolidayRule.fixed(Month.JUNE, 25)).validUntil(Year.of(2019)),
                                HolidayPeriod.of(HolidayRule.fixed(Month.MAY, 30)).validFrom(Year.of(2020))),
                                Holiday.onDates("Matariki", LocalDate.of(2022, 6, 24), LocalDate.of(2023, 7, 14)))
                        .build())
                .build();

        assertEquals("Statehood Day", calendar.holidayOn(LocalDate.of(2019, 6, 25)).orElseThrow().name());
        assertEquals("Statehood Day", calendar.holidayOn(LocalDate.of(2020, 5, 30)).orElseThrow().name());
        assertFalse(calendar.isHoliday(LocalDate.of(2020, 6, 25)));
        assertFalse(calendar.isHoliday(LocalDate.of(2019, 5, 30)));
        assertEquals("Matariki", calendar.holidayOn(LocalDate.of(2022, 6, 24)).orElseThrow().name());
        assertEquals("Matariki", calendar.holidayOn(LocalDate.of(2023, 7, 14)).orElseThrow().name());
        assertEquals(1, calendar.holidaysIn(Year.of(2024)).size());
    }

    @Test
    void typeAndObservanceFollowThePeriodOfTheYear() {
        var victory = HolidayRule.fixed(Month.MAY, 8);
        var calendar = HolidayCalendar.builder()
                .layer(Layer.named("XX")
                        .add(Holiday.of("Victory Day",
                                HolidayPeriod.of(victory).validUntil(Year.of(2025)),
                                HolidayPeriod.of(HolidayRule.on(LocalDate.of(2026, 5, 8))).ofType(HolidayType.OBSERVANCE),
                                HolidayPeriod.of(victory).validFrom(Year.of(2027)).withObservance(ObservanceRule.weekendToMonday())))
                        .build())
                .build();

        assertEquals(HolidayType.PUBLIC, calendar.holidayOn(LocalDate.of(2025, 5, 8)).orElseThrow().type());
        assertFalse(calendar.isBusinessDay(LocalDate.of(2025, 5, 8)));
        assertEquals(HolidayType.OBSERVANCE, calendar.holidayOn(LocalDate.of(2026, 5, 8)).orElseThrow().type());
        assertTrue(calendar.isBusinessDay(LocalDate.of(2026, 5, 8)));
        assertEquals(LocalDate.of(2027, 5, 8), calendar.holidayOn(LocalDate.of(2027, 5, 10)).orElseThrow().original());
    }

    @Test
    void aSuspendedHolidayIsAbsentInTheGap() {
        var allSaints = HolidayRule.fixed(Month.NOVEMBER, 1);
        var calendar = HolidayCalendar.builder()
                .layer(Layer.named("XX")
                        .add(Holiday.of("All Saints", HolidayPeriod.of(allSaints).validUntil(Year.of(2012)),
                                HolidayPeriod.of(allSaints).validFrom(Year.of(2016))))
                        .build())
                .build();

        assertTrue(calendar.isHoliday(LocalDate.of(2012, 11, 1)));
        assertFalse(calendar.isHoliday(LocalDate.of(2013, 11, 1)));
        assertFalse(calendar.isHoliday(LocalDate.of(2015, 11, 1)));
        assertTrue(calendar.isHoliday(LocalDate.of(2016, 11, 1)));
    }

    @Test
    void aFarShiftInAnyPeriodWidensTheSearch() {
        var calendar = HolidayCalendar.builder()
                .layer(Layer.named("XX")
                        .add(Holiday.of("Far", HolidayPeriod.of(HolidayRule.fixed(Month.JANUARY, 1)).validUntil(Year.of(2019)),
                                HolidayPeriod.of(HolidayRule.fixed(Month.JANUARY, 1).plusDays(-366)).validFrom(Year.of(2020))))
                        .build())
                .build();

        assertEquals("Far", calendar.holidayOn(LocalDate.of(2024, 12, 31)).orElseThrow().name());
    }

    @Test
    void warmUpCachesTheRequestedYears() {
        var calendar = assertInstanceOf(CompiledCalendar.class, HolidayCalendar.builder().layer(SPAIN).build());

        calendar.warmUp(Year.of(2025), Year.of(2027));

        assertEquals(3, calendar.cachedYears());
    }

    @Test
    void rejectsNullDates() {
        assertThrows(NullPointerException.class, () -> MALAGA_CALENDAR.isHoliday(null));
        assertThrows(NullPointerException.class, () -> MALAGA_CALENDAR.isBusinessDay(null));
        assertThrows(NullPointerException.class, () -> MALAGA_CALENDAR.holidayOn(null));
        assertThrows(NullPointerException.class, () -> MALAGA_CALENDAR.holidaysIn(null));
    }
}
