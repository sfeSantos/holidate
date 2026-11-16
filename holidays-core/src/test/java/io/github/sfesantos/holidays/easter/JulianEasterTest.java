package io.github.sfesantos.holidays.easter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JulianEasterTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("referenceTable")
    void matchesReferenceTable(int year, LocalDate expected) {
        assertEquals(expected, JulianEaster.of(year));
    }

    @Test
    void instanceDelegatesToStaticFunction() {
        var algorithm = EasterAlgorithm.julian();

        assertSame(algorithm, EasterAlgorithm.julian());
        assertEquals(JulianEaster.of(2026), algorithm.easterSunday(2026));
        assertEquals("JulianEaster", algorithm.toString());
    }

    @Test
    void carriesTheJulianMonthAndDay() {
        assertEquals(LocalDate.of(2026, 3, 30), JulianEaster.of(2026));
        assertEquals(LocalDate.of(2027, 4, 19), JulianEaster.of(2027));
    }

    @Test
    void earliestAndLatestPossibleDates() {
        assertEquals(LocalDate.of(2010, 3, 22), JulianEaster.of(2010));
        assertEquals(LocalDate.of(2051, 4, 24), JulianEaster.of(2051));
    }

    static Stream<Arguments> referenceTable() {
        return EasterTable.read("/julian-easter-2000-2100.txt");
    }
}
