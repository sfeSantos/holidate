package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Spain's national holidays as the BOE publishes them each October:
 * Resolución de 23 de octubre de 2024 (2025), de 10 de octubre de 2025
 * (2026) and de 8 de octubre de 2026 (2027). The national list keeps a
 * holiday on its date even when it falls on a Sunday; moving it is up to
 * each autonomous community.
 */
class BoeNationalHolidaysTest {

    @Test
    void boe2025() {
        assertEquals(List.of(
                d(2025, 1, 1), d(2025, 1, 6), d(2025, 4, 18), d(2025, 5, 1), d(2025, 8, 15),
                d(2025, 10, 12), d(2025, 11, 1), d(2025, 12, 6), d(2025, 12, 8), d(2025, 12, 25)),
                Observed.datesIn("ES", 2025));
    }

    @Test
    void boe2026() {
        assertEquals(List.of(
                d(2026, 1, 1), d(2026, 1, 6), d(2026, 4, 3), d(2026, 5, 1), d(2026, 8, 15),
                d(2026, 10, 12), d(2026, 11, 1), d(2026, 12, 6), d(2026, 12, 8), d(2026, 12, 25)),
                Observed.datesIn("ES", 2026));
    }

    @Test
    void boe2027() {
        assertEquals(List.of(
                d(2027, 1, 1), d(2027, 1, 6), d(2027, 3, 26), d(2027, 5, 1), d(2027, 8, 15),
                d(2027, 10, 12), d(2027, 11, 1), d(2027, 12, 6), d(2027, 12, 8), d(2027, 12, 25)),
                Observed.datesIn("ES", 2027));
    }
}
