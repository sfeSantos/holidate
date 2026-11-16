package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Germany under the holiday law of each Land, as the state law portals and
 * interior ministries publish it: FTG Baden-Württemberg § 1, BayFTG Art. 1,
 * FTG Berlin § 1, FTG Brandenburg § 2, the Bremen Gesetz über die Sonn-,
 * Gedenk- und Feiertage § 2, the Hamburg Feiertagsgesetz, HFeiertagsG,
 * FTG M-V § 2, NFeiertagsG § 2, Feiertagsgesetz NW § 2, LFtG
 * Rheinland-Pfalz, SFG Saarland, SächsSFG § 1, FeiertG LSA, SFTG
 * Schleswig-Holstein § 2 and ThürFGtG § 2. 3 October comes from Article 2 of
 * the Einigungsvertrag. No yearly list covers all Länder, so the dates are
 * derived from the laws. {@code DE} is the nine days common to every Land.
 * Holidays limited to some municipalities are left out: Mariä Himmelfahrt
 * and the Augsburg Friedensfest in Bavaria, Fronleichnam in Saxony and
 * Thuringia.
 */
class GermanyHolidaysTest {

    private static final List<LocalDate> COMMON_2025 = List.of(
            d(2025, 1, 1), d(2025, 4, 18), d(2025, 4, 21), d(2025, 5, 1), d(2025, 5, 29), d(2025, 6, 9),
            d(2025, 10, 3), d(2025, 12, 25), d(2025, 12, 26));
    private static final List<LocalDate> COMMON_2026 = List.of(
            d(2026, 1, 1), d(2026, 4, 3), d(2026, 4, 6), d(2026, 5, 1), d(2026, 5, 14), d(2026, 5, 25),
            d(2026, 10, 3), d(2026, 12, 25), d(2026, 12, 26));
    private static final List<LocalDate> COMMON_2027 = List.of(
            d(2027, 1, 1), d(2027, 3, 26), d(2027, 3, 29), d(2027, 5, 1), d(2027, 5, 6), d(2027, 5, 17),
            d(2027, 10, 3), d(2027, 12, 25), d(2027, 12, 26));

    @Test
    void common2025() {
        assertEquals(COMMON_2025, Observed.datesIn("DE", 2025));
    }

    @Test
    void common2026() {
        assertEquals(COMMON_2026, Observed.datesIn("DE", 2026));
    }

    @Test
    void common2027() {
        assertEquals(COMMON_2027, Observed.datesIn("DE", 2027));
    }

    @Test
    void badenWurttemberg() {
        assertEquals(with(COMMON_2025, d(2025, 1, 6), d(2025, 6, 19), d(2025, 11, 1)), Observed.datesIn("DE-BW", 2025));
        assertEquals(with(COMMON_2026, d(2026, 1, 6), d(2026, 6, 4), d(2026, 11, 1)), Observed.datesIn("DE-BW", 2026));
        assertEquals(with(COMMON_2027, d(2027, 1, 6), d(2027, 5, 27), d(2027, 11, 1)), Observed.datesIn("DE-BW", 2027));
    }

    @Test
    void bavaria() {
        assertEquals(with(COMMON_2025, d(2025, 1, 6), d(2025, 6, 19), d(2025, 11, 1)), Observed.datesIn("DE-BY", 2025));
        assertEquals(with(COMMON_2026, d(2026, 1, 6), d(2026, 6, 4), d(2026, 11, 1)), Observed.datesIn("DE-BY", 2026));
        assertEquals(with(COMMON_2027, d(2027, 1, 6), d(2027, 5, 27), d(2027, 11, 1)), Observed.datesIn("DE-BY", 2027));
    }

    @Test
    void berlin() {
        assertEquals(with(COMMON_2025, d(2025, 3, 8), d(2025, 5, 8)), Observed.datesIn("DE-BE", 2025));
        assertEquals(with(COMMON_2026, d(2026, 3, 8)), Observed.datesIn("DE-BE", 2026));
        assertEquals(with(COMMON_2027, d(2027, 3, 8)), Observed.datesIn("DE-BE", 2027));
    }

    @Test
    void brandenburg() {
        assertEquals(with(COMMON_2025, d(2025, 4, 20), d(2025, 6, 8), d(2025, 10, 31)), Observed.datesIn("DE-BB", 2025));
        assertEquals(with(COMMON_2026, d(2026, 4, 5), d(2026, 5, 24), d(2026, 10, 31)), Observed.datesIn("DE-BB", 2026));
        assertEquals(with(COMMON_2027, d(2027, 3, 28), d(2027, 5, 16), d(2027, 10, 31)), Observed.datesIn("DE-BB", 2027));
    }

    @Test
    void bremen() {
        assertEquals(with(COMMON_2025, d(2025, 10, 31)), Observed.datesIn("DE-HB", 2025));
        assertEquals(with(COMMON_2026, d(2026, 10, 31)), Observed.datesIn("DE-HB", 2026));
        assertEquals(with(COMMON_2027, d(2027, 10, 31)), Observed.datesIn("DE-HB", 2027));
    }

    @Test
    void hamburg() {
        assertEquals(with(COMMON_2025, d(2025, 10, 31)), Observed.datesIn("DE-HH", 2025));
        assertEquals(with(COMMON_2026, d(2026, 10, 31)), Observed.datesIn("DE-HH", 2026));
        assertEquals(with(COMMON_2027, d(2027, 10, 31)), Observed.datesIn("DE-HH", 2027));
    }

    @Test
    void hesse() {
        assertEquals(with(COMMON_2025, d(2025, 6, 19)), Observed.datesIn("DE-HE", 2025));
        assertEquals(with(COMMON_2026, d(2026, 6, 4)), Observed.datesIn("DE-HE", 2026));
        assertEquals(with(COMMON_2027, d(2027, 5, 27)), Observed.datesIn("DE-HE", 2027));
    }

    @Test
    void mecklenburgWesternPomerania() {
        assertEquals(with(COMMON_2025, d(2025, 3, 8), d(2025, 10, 31)), Observed.datesIn("DE-MV", 2025));
        assertEquals(with(COMMON_2026, d(2026, 3, 8), d(2026, 10, 31)), Observed.datesIn("DE-MV", 2026));
        assertEquals(with(COMMON_2027, d(2027, 3, 8), d(2027, 10, 31)), Observed.datesIn("DE-MV", 2027));
    }

    @Test
    void lowerSaxony() {
        assertEquals(with(COMMON_2025, d(2025, 10, 31)), Observed.datesIn("DE-NI", 2025));
        assertEquals(with(COMMON_2026, d(2026, 10, 31)), Observed.datesIn("DE-NI", 2026));
        assertEquals(with(COMMON_2027, d(2027, 10, 31)), Observed.datesIn("DE-NI", 2027));
    }

    @Test
    void northRhineWestphalia() {
        assertEquals(with(COMMON_2025, d(2025, 6, 19), d(2025, 11, 1)), Observed.datesIn("DE-NW", 2025));
        assertEquals(with(COMMON_2026, d(2026, 6, 4), d(2026, 11, 1)), Observed.datesIn("DE-NW", 2026));
        assertEquals(with(COMMON_2027, d(2027, 5, 27), d(2027, 11, 1)), Observed.datesIn("DE-NW", 2027));
    }

    @Test
    void rhinelandPalatinate() {
        assertEquals(with(COMMON_2025, d(2025, 6, 19), d(2025, 11, 1)), Observed.datesIn("DE-RP", 2025));
        assertEquals(with(COMMON_2026, d(2026, 6, 4), d(2026, 11, 1)), Observed.datesIn("DE-RP", 2026));
        assertEquals(with(COMMON_2027, d(2027, 5, 27), d(2027, 11, 1)), Observed.datesIn("DE-RP", 2027));
    }

    @Test
    void saarland() {
        assertEquals(with(COMMON_2025, d(2025, 6, 19), d(2025, 8, 15), d(2025, 11, 1)), Observed.datesIn("DE-SL", 2025));
        assertEquals(with(COMMON_2026, d(2026, 6, 4), d(2026, 8, 15), d(2026, 11, 1)), Observed.datesIn("DE-SL", 2026));
        assertEquals(with(COMMON_2027, d(2027, 5, 27), d(2027, 8, 15), d(2027, 11, 1)), Observed.datesIn("DE-SL", 2027));
    }

    @Test
    void saxony() {
        assertEquals(with(COMMON_2025, d(2025, 10, 31), d(2025, 11, 19)), Observed.datesIn("DE-SN", 2025));
        assertEquals(with(COMMON_2026, d(2026, 10, 31), d(2026, 11, 18)), Observed.datesIn("DE-SN", 2026));
        assertEquals(with(COMMON_2027, d(2027, 10, 31), d(2027, 11, 17)), Observed.datesIn("DE-SN", 2027));
    }

    @Test
    void saxonyAnhalt() {
        assertEquals(with(COMMON_2025, d(2025, 1, 6), d(2025, 10, 31)), Observed.datesIn("DE-ST", 2025));
        assertEquals(with(COMMON_2026, d(2026, 1, 6), d(2026, 10, 31)), Observed.datesIn("DE-ST", 2026));
        assertEquals(with(COMMON_2027, d(2027, 1, 6), d(2027, 10, 31)), Observed.datesIn("DE-ST", 2027));
    }

    @Test
    void schleswigHolstein() {
        assertEquals(with(COMMON_2025, d(2025, 10, 31)), Observed.datesIn("DE-SH", 2025));
        assertEquals(with(COMMON_2026, d(2026, 10, 31)), Observed.datesIn("DE-SH", 2026));
        assertEquals(with(COMMON_2027, d(2027, 10, 31)), Observed.datesIn("DE-SH", 2027));
    }

    @Test
    void thuringia() {
        assertEquals(with(COMMON_2025, d(2025, 9, 20), d(2025, 10, 31)), Observed.datesIn("DE-TH", 2025));
        assertEquals(with(COMMON_2026, d(2026, 9, 20), d(2026, 10, 31)), Observed.datesIn("DE-TH", 2026));
        assertEquals(with(COMMON_2027, d(2027, 9, 20), d(2027, 10, 31)), Observed.datesIn("DE-TH", 2027));
    }

    @Test
    void aSundayHolidayIsNotMoved() {
        var germany = Observed.calendar("DE");

        assertTrue(germany.isHoliday(d(2027, 10, 3)));
        assertTrue(germany.isBusinessDay(d(2027, 10, 4)));
    }

    @Test
    void bussUndBettagIsTheWednesdayBefore23November() {
        var saxony = Observed.calendar("DE-SN");

        assertTrue(saxony.isHoliday(d(2022, 11, 16)));
        assertTrue(saxony.isHoliday(d(2023, 11, 22)));
        assertFalse(saxony.isHoliday(d(2026, 11, 25)));
    }

    @Test
    void berlinKeepsWomensDayFrom2019AndLiberationDayOnlyIn2025() {
        var berlin = Observed.calendar("DE-BE");

        assertFalse(berlin.isHoliday(d(2018, 3, 8)));
        assertTrue(berlin.isHoliday(d(2019, 3, 8)));
        assertFalse(berlin.isHoliday(d(2024, 5, 8)));
        assertFalse(berlin.isHoliday(d(2026, 5, 8)));
    }

    @Test
    void newerStateHolidaysStartInTheirYear() {
        assertFalse(Observed.calendar("DE-MV").isHoliday(d(2022, 3, 8)));
        assertTrue(Observed.calendar("DE-MV").isHoliday(d(2023, 3, 8)));
        assertFalse(Observed.calendar("DE-TH").isHoliday(d(2018, 9, 20)));
        assertTrue(Observed.calendar("DE-TH").isHoliday(d(2019, 9, 20)));

        for (var land : List.of("DE-HB", "DE-HH", "DE-NI", "DE-SH")) {
            assertFalse(Observed.calendar(land).isHoliday(d(2016, 10, 31)), land);
            assertTrue(Observed.calendar(land).isHoliday(d(2018, 10, 31)), land);
        }
    }

    private static List<LocalDate> with(List<LocalDate> common, LocalDate... extras) {
        return Stream.concat(common.stream(), Stream.of(extras))
                .sorted()
                .toList();
    }
}
