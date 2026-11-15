package io.github.sfesantos.holidays.data;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Year;

import org.junit.jupiter.api.Test;

class BridgeDaysTest {

    @Test
    void malaga2026() {
        var bridges = Observed.calendar("ES-AN-MA").bridgeDays(Year.of(2026));

        assertTrue(bridges.contains(LocalDate.of(2026, 1, 2)));
        assertTrue(bridges.contains(LocalDate.of(2026, 1, 5)));
        assertFalse(bridges.contains(LocalDate.of(2026, 12, 24)));
    }

    @Test
    void oneDayBridgesAreAmongTheTwoDayOnesOnEveryShippedCalendar() {
        for (var id : Observed.REGISTRY.layerIds()) {
            var calendar = Observed.calendar(id);

            for (var year = 2024; year <= 2030; year++) {
                var single = calendar.bridgeDays(Year.of(year), 1);
                var upToTwo = calendar.bridgeDays(Year.of(year), 2);

                assertTrue(upToTwo.containsAll(single), id + " " + year);

                for (var day : upToTwo) {
                    var before = calendar.isBusinessDay(day.minusDays(1));
                    var after = calendar.isBusinessDay(day.plusDays(1));

                    assertTrue(calendar.isBusinessDay(day), id + " " + day);
                    assertFalse(before && after, id + " " + day);
                    assertFalse(before && calendar.isBusinessDay(day.minusDays(2)), id + " " + day);
                    assertFalse(after && calendar.isBusinessDay(day.plusDays(2)), id + " " + day);
                }
            }
        }
    }
}
