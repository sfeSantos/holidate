package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Luxembourg's eleven statutory holidays under the Code du travail, as the
 * government publishes them: the Ministère de la Fonction publique
 * circular "Jours fériés 2026" of 23 October 2025 and the list for 2026 and
 * 2027 on luxembourg.public.lu; 2025 is derived from the law. The Journée de
 * l'Europe was added by the law of 25 April 2019. A holiday on a Sunday
 * gives a compensatory day the employee picks, so nothing is moved.
 */
class LuxembourgHolidaysTest {

    @Test
    void joursFeries2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 4, 21), d(2025, 5, 1), d(2025, 5, 9), d(2025, 5, 29), d(2025, 6, 9),
                d(2025, 6, 23), d(2025, 8, 15), d(2025, 11, 1), d(2025, 12, 25), d(2025, 12, 26)),
                Observed.datesIn("LU", 2025));
    }

    @Test
    void joursFeries2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 4, 6), d(2026, 5, 1), d(2026, 5, 9), d(2026, 5, 14), d(2026, 5, 25),
                d(2026, 6, 23), d(2026, 8, 15), d(2026, 11, 1), d(2026, 12, 25), d(2026, 12, 26)),
                Observed.datesIn("LU", 2026));
    }

    @Test
    void joursFeries2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 3, 29), d(2027, 5, 1), d(2027, 5, 6), d(2027, 5, 9), d(2027, 5, 17),
                d(2027, 6, 23), d(2027, 8, 15), d(2027, 11, 1), d(2027, 12, 25), d(2027, 12, 26)),
                Observed.datesIn("LU", 2027));
    }

    @Test
    void europeDayIsAHolidayFrom2019() {
        var luxembourg = Observed.calendar("LU");

        assertFalse(luxembourg.isHoliday(d(2018, 5, 9)));
        assertTrue(luxembourg.isHoliday(d(2019, 5, 9)));
    }
}
