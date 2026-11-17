package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayType;

/**
 * Portugal's mandatory holidays under Código do Trabalho, artigo 234.º, as
 * amended by Lei 8/2016. No yearly list is published, so the dates are
 * derived from the law, which names Easter Sunday and never moves a holiday.
 * Lei 23/2012, artigo 10.º, took Corpo de Deus, 5 de Outubro, 1 de Novembro
 * and 1 de Dezembro off the list from 1 January 2013; Lei 8/2016 put them
 * back from 2 April 2016, so they are missing in 2013, 2014 and 2015 only.
 * Artigo 235.º makes Carnival Tuesday and the municipal holiday optional:
 * Carnival is kept as {@code OBSERVANCE}, and Lisboa (Santo António, 13
 * June) and Porto (São João, 24 June) ship as examples of a municipal layer.
 */
class PortugalHolidaysTest {

    @Test
    void national2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 3, 4), d(2025, 4, 18), d(2025, 4, 20), d(2025, 4, 25), d(2025, 5, 1),
                d(2025, 6, 10), d(2025, 6, 19), d(2025, 8, 15), d(2025, 10, 5), d(2025, 11, 1), d(2025, 12, 1),
                d(2025, 12, 8), d(2025, 12, 25)),
                Observed.datesIn("PT", 2025));
    }

    @Test
    void national2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 17), d(2026, 4, 3), d(2026, 4, 5), d(2026, 4, 25), d(2026, 5, 1),
                d(2026, 6, 4), d(2026, 6, 10), d(2026, 8, 15), d(2026, 10, 5), d(2026, 11, 1), d(2026, 12, 1),
                d(2026, 12, 8), d(2026, 12, 25)),
                Observed.datesIn("PT", 2026));
    }

    @Test
    void national2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 9), d(2027, 3, 26), d(2027, 3, 28), d(2027, 4, 25), d(2027, 5, 1),
                d(2027, 5, 27), d(2027, 6, 10), d(2027, 8, 15), d(2027, 10, 5), d(2027, 11, 1), d(2027, 12, 1),
                d(2027, 12, 8), d(2027, 12, 25)),
                Observed.datesIn("PT", 2027));
    }

    @Test
    void lisboa2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 3, 4), d(2025, 4, 18), d(2025, 4, 20), d(2025, 4, 25), d(2025, 5, 1),
                d(2025, 6, 10), d(2025, 6, 13), d(2025, 6, 19), d(2025, 8, 15), d(2025, 10, 5), d(2025, 11, 1),
                d(2025, 12, 1), d(2025, 12, 8), d(2025, 12, 25)),
                Observed.datesIn("PT-11-LIS", 2025));
    }

    @Test
    void lisboa2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 17), d(2026, 4, 3), d(2026, 4, 5), d(2026, 4, 25), d(2026, 5, 1),
                d(2026, 6, 4), d(2026, 6, 10), d(2026, 6, 13), d(2026, 8, 15), d(2026, 10, 5), d(2026, 11, 1),
                d(2026, 12, 1), d(2026, 12, 8), d(2026, 12, 25)),
                Observed.datesIn("PT-11-LIS", 2026));
    }

    @Test
    void lisboa2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 9), d(2027, 3, 26), d(2027, 3, 28), d(2027, 4, 25), d(2027, 5, 1),
                d(2027, 5, 27), d(2027, 6, 10), d(2027, 6, 13), d(2027, 8, 15), d(2027, 10, 5), d(2027, 11, 1),
                d(2027, 12, 1), d(2027, 12, 8), d(2027, 12, 25)),
                Observed.datesIn("PT-11-LIS", 2027));
    }

    @Test
    void porto2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 3, 4), d(2025, 4, 18), d(2025, 4, 20), d(2025, 4, 25), d(2025, 5, 1),
                d(2025, 6, 10), d(2025, 6, 19), d(2025, 6, 24), d(2025, 8, 15), d(2025, 10, 5), d(2025, 11, 1),
                d(2025, 12, 1), d(2025, 12, 8), d(2025, 12, 25)),
                Observed.datesIn("PT-13-PRT", 2025));
    }

    @Test
    void porto2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 2, 17), d(2026, 4, 3), d(2026, 4, 5), d(2026, 4, 25), d(2026, 5, 1),
                d(2026, 6, 4), d(2026, 6, 10), d(2026, 6, 24), d(2026, 8, 15), d(2026, 10, 5), d(2026, 11, 1),
                d(2026, 12, 1), d(2026, 12, 8), d(2026, 12, 25)),
                Observed.datesIn("PT-13-PRT", 2026));
    }

    @Test
    void porto2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 2, 9), d(2027, 3, 26), d(2027, 3, 28), d(2027, 4, 25), d(2027, 5, 1),
                d(2027, 5, 27), d(2027, 6, 10), d(2027, 6, 24), d(2027, 8, 15), d(2027, 10, 5), d(2027, 11, 1),
                d(2027, 12, 1), d(2027, 12, 8), d(2027, 12, 25)),
                Observed.datesIn("PT-13-PRT", 2027));
    }

    @Test
    void carnivalTuesdayStaysABusinessDay() {
        var portugal = Observed.calendar("PT");

        assertEquals(HolidayType.OBSERVANCE, portugal.holidayOn(d(2026, 2, 17)).orElseThrow().type());
        assertTrue(portugal.isBusinessDay(d(2026, 2, 17)));
        assertFalse(portugal.isBusinessDay(d(2026, 6, 4)));
    }

    @Test
    void aWeekendHolidayIsNotMoved() {
        var lisboa = Observed.calendar("PT-11-LIS");

        assertTrue(lisboa.isHoliday(d(2026, 6, 13)));
        assertTrue(lisboa.isBusinessDay(d(2026, 6, 15)));
        assertTrue(lisboa.isBusinessDay(d(2026, 11, 2)));
    }

    @Test
    void aMunicipalHolidayStaysInItsCity() {
        assertFalse(Observed.calendar("PT").isHoliday(d(2026, 6, 24)));
        assertFalse(Observed.calendar("PT-11-LIS").isHoliday(d(2026, 6, 24)));
        assertTrue(Observed.calendar("PT-13-PRT").isHoliday(d(2026, 6, 24)));
    }

    @Test
    void fourHolidaysWereSuspendedFrom2013To2015() {
        var portugal = Observed.calendar("PT");

        assertEquals(List.of("Corpo de Deus", "Implantação da República", "Todos os Santos", "Restauração da Independência"),
                List.of(d(2012, 6, 7), d(2012, 10, 5), d(2012, 11, 1), d(2012, 12, 1))
                        .stream()
                        .map(date -> portugal.holidayOn(date).orElseThrow().name())
                        .toList());

        for (var year = 2013; year <= 2015; year++) {
            assertFalse(portugal.isHoliday(d(year, 10, 5)), "5 de Outubro " + year);
            assertFalse(portugal.isHoliday(d(year, 11, 1)), "1 de Novembro " + year);
            assertFalse(portugal.isHoliday(d(year, 12, 1)), "1 de Dezembro " + year);
            assertEquals(10, Observed.datesIn("PT", year).size(), "holidays in " + year);
        }

        assertFalse(portugal.isHoliday(d(2013, 5, 30)));
        assertFalse(portugal.isHoliday(d(2014, 6, 19)));
        assertFalse(portugal.isHoliday(d(2015, 6, 4)));
        assertEquals("Corpo de Deus", portugal.holidayOn(d(2016, 5, 26)).orElseThrow().name());
        assertEquals("Implantação da República", portugal.holidayOn(d(2016, 10, 5)).orElseThrow().name());
        assertEquals("Todos os Santos", portugal.holidayOn(d(2016, 11, 1)).orElseThrow().name());
        assertEquals("Restauração da Independência", portugal.holidayOn(d(2016, 12, 1)).orElseThrow().name());
        assertEquals(14, Observed.datesIn("PT", 2012).size());
        assertEquals(14, Observed.datesIn("PT", 2016).size());
    }
}
