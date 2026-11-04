package io.github.sfesantos.holidays.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.Holiday;
import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.HolidayOccurrence;
import io.github.sfesantos.holidays.HolidayType;
import io.github.sfesantos.holidays.Layer;

/**
 * The README example for the city of Rio de Janeiro. Brazil never moves a
 * holiday, so no observance rule is set. Carnival is a national "ponto
 * facultativo" (optional) and a state holiday on Tuesday in Rio de Janeiro.
 */
class RioDeJaneiroExampleTest {

    static final Layer BRAZIL = Layer.named("BR")
            .add(Holiday.fixed("Confraternização Universal", Month.JANUARY, 1),
                    Holiday.easterRelative("Segunda-feira de Carnaval", -48).ofType(HolidayType.OBSERVANCE),
                    Holiday.easterRelative("Terça-feira de Carnaval", -47).ofType(HolidayType.OBSERVANCE),
                    Holiday.easterRelative("Sexta-feira Santa", -2),
                    Holiday.fixed("Tiradentes", Month.APRIL, 21),
                    Holiday.fixed("Dia do Trabalho", Month.MAY, 1),
                    Holiday.easterRelative("Corpus Christi", 60).ofType(HolidayType.OBSERVANCE),
                    Holiday.fixed("Independência do Brasil", Month.SEPTEMBER, 7),
                    Holiday.fixed("Nossa Senhora Aparecida", Month.OCTOBER, 12),
                    Holiday.fixed("Finados", Month.NOVEMBER, 2),
                    Holiday.fixed("Proclamação da República", Month.NOVEMBER, 15),
                    Holiday.fixed("Dia da Consciência Negra", Month.NOVEMBER, 20).validFrom(Year.of(2024)),
                    Holiday.fixed("Natal", Month.DECEMBER, 25))
            .build();

    static final Layer RIO_STATE = Layer.named("BR-RJ")
            .add(Holiday.fixed("São Jorge", Month.APRIL, 23))
            .override(Holiday.easterRelative("Terça-feira de Carnaval", -47))
            .override(Holiday.fixed("Dia da Consciência Negra", Month.NOVEMBER, 20).validFrom(Year.of(2002)))
            .build();

    static final Layer RIO_CITY = Layer.named("BR-RJ-RIO")
            .add(Holiday.fixed("São Sebastião", Month.JANUARY, 20),
                    Holiday.easterRelative("Quarta-feira de Cinzas", -46).ofType(HolidayType.HALF_DAY))
            .build();

    static final HolidayCalendar RIO = HolidayCalendar.builder()
            .layers(BRAZIL, RIO_STATE, RIO_CITY)
            .build();

    @Test
    void holidaysOf2026() {
        var dates = RIO.holidaysIn(Year.of(2026))
                .stream()
                .map(HolidayOccurrence::observed)
                .toList();

        assertEquals(List.of(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 20),
                LocalDate.of(2026, 2, 16),
                LocalDate.of(2026, 2, 17),
                LocalDate.of(2026, 2, 18),
                LocalDate.of(2026, 4, 3),
                LocalDate.of(2026, 4, 21),
                LocalDate.of(2026, 4, 23),
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 6, 4),
                LocalDate.of(2026, 9, 7),
                LocalDate.of(2026, 10, 12),
                LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 12, 25)), dates);
    }

    @Test
    void carnivalFollowsEaster() {
        assertTrue(RIO.isHoliday(LocalDate.of(2026, 2, 17)));
        assertTrue(RIO.isHoliday(LocalDate.of(2027, 2, 9)));
        assertTrue(RIO.isHoliday(LocalDate.of(2028, 2, 29)));
    }

    @Test
    void carnivalTuesdayIsAStateHolidayButMondayIsOptional() {
        var monday = LocalDate.of(2026, 2, 16);
        var tuesday = LocalDate.of(2026, 2, 17);

        assertEquals(HolidayType.OBSERVANCE, RIO.holidayOn(monday).orElseThrow().type());
        assertTrue(RIO.isBusinessDay(monday));

        var carnival = RIO.holidayOn(tuesday).orElseThrow();

        assertEquals(HolidayType.PUBLIC, carnival.type());
        assertEquals("BR-RJ", carnival.layer());
        assertFalse(RIO.isBusinessDay(tuesday));
    }

    @Test
    void ashWednesdayIsAHalfDay() {
        var ashWednesday = LocalDate.of(2026, 2, 18);

        assertEquals(HolidayType.HALF_DAY, RIO.holidayOn(ashWednesday).orElseThrow().type());
        assertTrue(RIO.isBusinessDay(ashWednesday));
        assertTrue(RIO.isHalfDay(ashWednesday));
        assertTrue(RIO.isOpen(ashWednesday.atTime(11, 59)));
        assertFalse(RIO.isOpen(ashWednesday.atTime(12, 0)));
    }

    @Test
    void blackAwarenessDayWasAStateHolidayBeforeBecomingNational() {
        var brazilOnly = HolidayCalendar.builder().layer(BRAZIL).build();

        assertFalse(brazilOnly.isHoliday(LocalDate.of(2023, 11, 20)));
        assertTrue(brazilOnly.isHoliday(LocalDate.of(2024, 11, 20)));
        assertTrue(RIO.isHoliday(LocalDate.of(2023, 11, 20)));
        assertEquals("BR-RJ", RIO.holidayOn(LocalDate.of(2026, 11, 20)).orElseThrow().layer());
    }
}
