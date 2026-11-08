package io.github.sfesantos.holidays.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DirectoryDataProviderTest {

    static final String SPAIN = """
            { "layers": [ { "id": "ES", "holidays": [ { "name": "Navidad", "rule": { "fixed": "12-25" } } ] } ] }
            """;

    static final String ANDALUCIA = """
            { "layers": [ { "id": "ES-AN", "extends": "ES",
                "holidays": [ { "name": "Día de Andalucía", "rule": { "fixed": "02-28" } } ] } ] }
            """;

    @TempDir
    Path directory;

    @Test
    void readsEveryJsonFileAndIgnoresTheRest() throws IOException {
        Files.writeString(directory.resolve("ES.json"), SPAIN);
        Files.writeString(directory.resolve("ES-AN.json"), ANDALUCIA);
        Files.writeString(directory.resolve("README.txt"), "not data");
        Files.createDirectory(directory.resolve("nested"));
        var provider = DirectoryDataProvider.of(directory);

        assertEquals(Set.of("ES", "ES-AN"), provider.layerIds());
        assertTrue(CalendarRegistry.of(provider).calendar("ES-AN").isHoliday(LocalDate.of(2026, 12, 25)));
    }

    @Test
    void duplicateIdsAcrossFilesFail() throws IOException {
        Files.writeString(directory.resolve("a.json"), SPAIN);
        Files.writeString(directory.resolve("b.json"), SPAIN);

        var e = assertThrows(IllegalArgumentException.class, () -> DirectoryDataProvider.of(directory));

        assertTrue(e.getMessage().contains("b.json"));
    }

    @Test
    void notADirectoryFails() {
        assertThrows(IllegalArgumentException.class, () -> DirectoryDataProvider.of(directory.resolve("missing")));
    }
}
