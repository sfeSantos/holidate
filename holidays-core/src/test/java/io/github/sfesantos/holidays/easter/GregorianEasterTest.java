package io.github.sfesantos.holidays.easter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GregorianEasterTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("referenceTable")
    void matchesReferenceTable(int year, LocalDate expected) {
        assertEquals(expected, GregorianEaster.of(year));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("referenceTable")
    void alwaysFallsOnASunday(int year, LocalDate expected) {
        assertEquals(DayOfWeek.SUNDAY, GregorianEaster.of(year).getDayOfWeek());
    }

    @Test
    void instanceDelegatesToStaticFunction() {
        var algorithm = EasterAlgorithm.gregorian();

        assertSame(algorithm, EasterAlgorithm.gregorian());
        assertEquals(GregorianEaster.of(2026), algorithm.easterSunday(2026));
    }

    @Test
    void earliestAndLatestPossibleDates() {
        assertEquals(LocalDate.of(2285, 3, 22), GregorianEaster.of(2285));
        assertEquals(LocalDate.of(2038, 4, 25), GregorianEaster.of(2038));
    }

    /** Reference dates generated independently with python-dateutil. */
    static Stream<Arguments> referenceTable() {
        var resource = GregorianEasterTest.class.getResourceAsStream("/gregorian-easter-2000-2100.txt");

        try (var reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(line -> line.split(" "))
                    .map(parts -> Arguments.of(Integer.parseInt(parts[0]), LocalDate.parse(parts[1])))
                    .toList()
                    .stream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
