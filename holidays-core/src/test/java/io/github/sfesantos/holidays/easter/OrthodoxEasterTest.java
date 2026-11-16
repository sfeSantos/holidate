package io.github.sfesantos.holidays.easter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.sfesantos.holidays.Holiday;
import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.Layer;

class OrthodoxEasterTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("referenceTable")
    void matchesReferenceTable(int year, LocalDate expected) {
        assertEquals(expected, OrthodoxEaster.of(year));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("referenceTable")
    void alwaysFallsOnASunday(int year, LocalDate expected) {
        assertEquals(DayOfWeek.SUNDAY, OrthodoxEaster.of(year).getDayOfWeek());
    }

    @Test
    void instanceDelegatesToStaticFunction() {
        var algorithm = EasterAlgorithm.orthodox();

        assertSame(algorithm, EasterAlgorithm.orthodox());
        assertEquals(LocalDate.of(2026, 4, 12), algorithm.easterSunday(2026));
        assertEquals(LocalDate.of(2027, 5, 2), algorithm.easterSunday(2027));
        assertEquals("OrthodoxEaster", algorithm.toString());
    }

    @Test
    void theGapToTheJulianDateGrowsWithTheCenturies() {
        assertEquals(10, daysAheadOfJulian(1583));
        assertEquals(12, daysAheadOfJulian(1899));
        assertEquals(13, daysAheadOfJulian(1900));
        assertEquals(13, daysAheadOfJulian(2000));
        assertEquals(13, daysAheadOfJulian(2099));
        assertEquals(14, daysAheadOfJulian(2100));
    }

    @Test
    void sometimesCoincidesWithTheGregorianEaster() {
        assertEquals(GregorianEaster.of(2025), OrthodoxEaster.of(2025));
        assertEquals(LocalDate.of(2024, 5, 5), OrthodoxEaster.of(2024));
        assertEquals(LocalDate.of(2024, 3, 31), GregorianEaster.of(2024));
    }

    @Test
    void aCalendarBuiltWithItMovesEveryEasterRelativeHoliday() {
        var greece = HolidayCalendar.builder()
                .easter(EasterAlgorithm.orthodox())
                .layer(Layer.named("GR")
                        .add(Holiday.easterRelative("Μεγάλη Παρασκευή", -2),
                                Holiday.easterRelative("Δευτέρα του Πάσχα", 1),
                                Holiday.easterRelative("Αγίου Πνεύματος", 50))
                        .build())
                .build();

        assertEquals("Μεγάλη Παρασκευή", greece.holidayOn(LocalDate.of(2026, 4, 10)).orElseThrow().name());
        assertEquals("Δευτέρα του Πάσχα", greece.holidayOn(LocalDate.of(2026, 4, 13)).orElseThrow().name());
        assertEquals("Αγίου Πνεύματος", greece.holidayOn(LocalDate.of(2026, 6, 1)).orElseThrow().name());
    }

    private static long daysAheadOfJulian(int year) {
        return ChronoUnit.DAYS.between(JulianEaster.of(year), OrthodoxEaster.of(year));
    }

    static Stream<Arguments> referenceTable() {
        return EasterTable.read("/orthodox-easter-2000-2100.txt");
    }
}
