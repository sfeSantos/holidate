package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Andalucía as the BOJA publishes it: Decreto 99/2024 (2025), Decreto
 * 103/2025 (2026) and Decreto 84/2026 (2027) for the regional calendar,
 * plus the yearly resolution of local holidays, where Málaga keeps 19
 * August and 8 September. Andalucía moves a Sunday holiday to Monday.
 */
class BojaAndaluciaHolidaysTest {

    @Test
    void boja2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 6), d(2025, 2, 28), d(2025, 4, 17), d(2025, 4, 18), d(2025, 5, 1),
                d(2025, 8, 15), d(2025, 10, 13), d(2025, 11, 1), d(2025, 12, 6), d(2025, 12, 8), d(2025, 12, 25)),
                Observed.datesIn("ES-AN", 2025));
    }

    @Test
    void boja2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 6), d(2026, 2, 28), d(2026, 4, 2), d(2026, 4, 3), d(2026, 5, 1),
                d(2026, 8, 15), d(2026, 10, 12), d(2026, 11, 2), d(2026, 12, 7), d(2026, 12, 8), d(2026, 12, 25)),
                Observed.datesIn("ES-AN", 2026));
    }

    @Test
    void boja2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 6), d(2027, 3, 1), d(2027, 3, 25), d(2027, 3, 26), d(2027, 5, 1),
                d(2027, 8, 16), d(2027, 10, 12), d(2027, 11, 1), d(2027, 12, 6), d(2027, 12, 8), d(2027, 12, 25)),
                Observed.datesIn("ES-AN", 2027));
    }

    @Test
    void malaga2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 6), d(2025, 2, 28), d(2025, 4, 17), d(2025, 4, 18), d(2025, 5, 1),
                d(2025, 8, 15), d(2025, 8, 19), d(2025, 9, 8), d(2025, 10, 13), d(2025, 11, 1), d(2025, 12, 6),
                d(2025, 12, 8), d(2025, 12, 25)),
                Observed.datesIn("ES-AN-MA", 2025));
    }

    @Test
    void malaga2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 6), d(2026, 2, 28), d(2026, 4, 2), d(2026, 4, 3), d(2026, 5, 1),
                d(2026, 8, 15), d(2026, 8, 19), d(2026, 9, 8), d(2026, 10, 12), d(2026, 11, 2), d(2026, 12, 7),
                d(2026, 12, 8), d(2026, 12, 25)),
                Observed.datesIn("ES-AN-MA", 2026));
    }

    @Test
    void malaga2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 6), d(2027, 3, 1), d(2027, 3, 25), d(2027, 3, 26), d(2027, 5, 1),
                d(2027, 8, 16), d(2027, 8, 19), d(2027, 9, 8), d(2027, 10, 12), d(2027, 11, 1), d(2027, 12, 6),
                d(2027, 12, 8), d(2027, 12, 25)),
                Observed.datesIn("ES-AN-MA", 2027));
    }
}
