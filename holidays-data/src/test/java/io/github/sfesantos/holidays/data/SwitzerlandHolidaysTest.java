package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Switzerland's national layer. Only 1 August is a federal holiday (Art. 110
 * Abs. 3 of the Federal Constitution); everything else is cantonal. The
 * layer holds the days observed in all 26 cantons according to the SECO
 * table "Feiertage Schweiz" (Stand 12.12.2024), the appendix to its guide
 * to the Arbeitsgesetz: Neujahrstag, Auffahrt, Bundesfeiertag and
 * Weihnachtstag. Karfreitag is missing in Ticino and Valais, so it is not
 * here. The table gives no dates, so they are derived from it.
 */
class SwitzerlandHolidaysTest {

    @Test
    void allCantons2025() {
        assertEquals(List.of(d(2025, 1, 1), d(2025, 5, 29), d(2025, 8, 1), d(2025, 12, 25)),
                Observed.datesIn("CH", 2025));
    }

    @Test
    void allCantons2026() {
        assertEquals(List.of(d(2026, 1, 1), d(2026, 5, 14), d(2026, 8, 1), d(2026, 12, 25)),
                Observed.datesIn("CH", 2026));
    }

    @Test
    void allCantons2027() {
        assertEquals(List.of(d(2027, 1, 1), d(2027, 5, 6), d(2027, 8, 1), d(2027, 12, 25)),
                Observed.datesIn("CH", 2027));
    }

    @Test
    void cantonalDaysStayBusinessDays() {
        var switzerland = Observed.calendar("CH");

        assertTrue(switzerland.isBusinessDay(d(2026, 4, 3)));
        assertTrue(switzerland.isBusinessDay(d(2026, 4, 6)));
    }
}
