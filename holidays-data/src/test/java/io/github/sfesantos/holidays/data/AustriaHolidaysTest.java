package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Austria's holidays under § 7 Abs. 2 Arbeitsruhegesetz, as consolidated in
 * the RIS. 2026 and 2027 match the list the City of Vienna publishes; 2025
 * is derived from the law. The version in force since 22 March 2019 does
 * not list Good Friday, and a holiday that falls on a Sunday is not moved.
 */
class AustriaHolidaysTest {

    @Test
    void arbeitsruhegesetz2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 6), d(2025, 4, 21), d(2025, 5, 1), d(2025, 5, 29), d(2025, 6, 9),
                d(2025, 6, 19), d(2025, 8, 15), d(2025, 10, 26), d(2025, 11, 1), d(2025, 12, 8), d(2025, 12, 25),
                d(2025, 12, 26)),
                Observed.datesIn("AT", 2025));
    }

    @Test
    void arbeitsruhegesetz2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 6), d(2026, 4, 6), d(2026, 5, 1), d(2026, 5, 14), d(2026, 5, 25),
                d(2026, 6, 4), d(2026, 8, 15), d(2026, 10, 26), d(2026, 11, 1), d(2026, 12, 8), d(2026, 12, 25),
                d(2026, 12, 26)),
                Observed.datesIn("AT", 2026));
    }

    @Test
    void arbeitsruhegesetz2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 6), d(2027, 3, 29), d(2027, 5, 1), d(2027, 5, 6), d(2027, 5, 17),
                d(2027, 5, 27), d(2027, 8, 15), d(2027, 10, 26), d(2027, 11, 1), d(2027, 12, 8), d(2027, 12, 25),
                d(2027, 12, 26)),
                Observed.datesIn("AT", 2027));
    }

    @Test
    void goodFridayIsABusinessDay() {
        var austria = Observed.calendar("AT");

        assertFalse(austria.isHoliday(d(2026, 4, 3)));
        assertTrue(austria.isBusinessDay(d(2026, 4, 3)));
    }
}
