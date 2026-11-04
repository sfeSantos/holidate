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
