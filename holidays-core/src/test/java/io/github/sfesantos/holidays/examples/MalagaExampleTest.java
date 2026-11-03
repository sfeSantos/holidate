package io.github.sfesantos.holidays.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.Holiday;
import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.HolidayOccurrence;
import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.ObservanceRule;

/** The README example for Málaga, checked against the 2026 BOE and BOJA calendars. */
class MalagaExampleTest {

    static final Layer SPAIN = Layer.named("ES")
            .add(Holiday.fixed("Año Nuevo", Month.JANUARY, 1),
                    Holiday.fixed("Epifanía del Señor", Month.JANUARY, 6),
                    Holiday.easterRelative("Viernes Santo", -2),
                    Holiday.fixed("Fiesta del Trabajo", Month.MAY, 1),
                    Holiday.fixed("Asunción de la Virgen", Month.AUGUST, 15),
                    Holiday.fixed("Fiesta Nacional de España", Month.OCTOBER, 12),
                    Holiday.fixed("Todos los Santos", Month.NOVEMBER, 1),
                    Holiday.fixed("Día de la Constitución", Month.DECEMBER, 6),
                    Holiday.fixed("Inmaculada Concepción", Month.DECEMBER, 8),
                    Holiday.fixed("Navidad", Month.DECEMBER, 25))
            .build();

    static final Layer ANDALUCIA = Layer.named("ES-AN")
            .add(Holiday.fixed("Día de Andalucía", Month.FEBRUARY, 28),
                    Holiday.easterRelative("Jueves Santo", -3))
            .observance(ObservanceRule.sundayToMonday())
            .build();

    static final Layer MALAGA = Layer.named("ES-AN-MA")
            .add(Holiday.fixed("Incorporación de Málaga a la Corona de Castilla", Month.AUGUST, 19),
                    Holiday.fixed("Virgen de la Victoria", Month.SEPTEMBER, 8))
            .build();

    static final HolidayCalendar MALAGA_CALENDAR = HolidayCalendar.builder()
            .weekend(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            .layers(SPAIN, ANDALUCIA, MALAGA)
            .build();

    @Test
    void observedDatesIn2026MatchThePublishedCalendar() {
        var observed = MALAGA_CALENDAR.holidaysIn(Year.of(2026))
                .stream()
                .map(HolidayOccurrence::observed)
                .toList();

        assertEquals(List.of(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 6),
                LocalDate.of(2026, 2, 28),
                LocalDate.of(2026, 4, 2),
                LocalDate.of(2026, 4, 3),
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 9, 8),
                LocalDate.of(2026, 10, 12),
                LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 12, 7),
                LocalDate.of(2026, 12, 8),
                LocalDate.of(2026, 12, 25)), observed);
    }

    @Test
    void sundayHolidaysMoveToMondayInAndalucia() {
        var allSaints = MALAGA_CALENDAR.holidayOn(LocalDate.of(2026, 11, 2)).orElseThrow();

        assertEquals("Todos los Santos", allSaints.name());
        assertEquals(LocalDate.of(2026, 11, 1), allSaints.original());
        assertEquals("ES", allSaints.layer());
        assertFalse(MALAGA_CALENDAR.isHoliday(LocalDate.of(2026, 11, 1)));
    }

    @Test
    void saturdayHolidaysStayOnSaturday() {
        assertTrue(MALAGA_CALENDAR.isHoliday(LocalDate.of(2026, 2, 28)));
        assertFalse(MALAGA_CALENDAR.isHoliday(LocalDate.of(2026, 3, 2)));
    }

    @Test
    void businessDays() {
        assertFalse(MALAGA_CALENDAR.isBusinessDay(LocalDate.of(2026, 4, 2)));
        assertFalse(MALAGA_CALENDAR.isBusinessDay(LocalDate.of(2026, 4, 4)));
        assertTrue(MALAGA_CALENDAR.isBusinessDay(LocalDate.of(2026, 4, 6)));
    }
}
