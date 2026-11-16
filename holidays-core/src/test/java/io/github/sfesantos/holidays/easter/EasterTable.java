package io.github.sfesantos.holidays.easter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

/** Reads a reference table of "year date" lines, generated independently with python-dateutil. */
final class EasterTable {

    private EasterTable() {}

    static Stream<Arguments> read(String resourceName) {
        var resource = EasterTable.class.getResourceAsStream(resourceName);

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
