package io.github.sfesantos.holidays.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ClasspathDataProviderTest {

    @Test
    void readsLayersFromResourcesNextToTheAnchor() {
        var provider = ClasspathDataProvider.of(ClasspathDataProviderTest.class, "test-pack.json");

        assertEquals(Set.of("ES", "ES-AN"), provider.layerIds());
        assertEquals("ES", provider.layer("ES-AN").orElseThrow().parent().orElseThrow());
        assertTrue(CalendarRegistry.of(provider).calendar("ES-AN").isHoliday(LocalDate.of(2026, 2, 28)));
    }

    @Test
    void missingResourceFails() {
        var e = assertThrows(IllegalArgumentException.class,
                () -> ClasspathDataProvider.of(ClasspathDataProviderTest.class, "missing.json"));

        assertTrue(e.getMessage().startsWith("resource missing.json not found"));
    }

    @Test
    void duplicateIdsAcrossResourcesFail() {
        assertThrows(IllegalArgumentException.class,
                () -> ClasspathDataProvider.of(ClasspathDataProviderTest.class, "test-pack.json", "test-pack.json"));
    }
}
