package io.github.sfesantos.holidays.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.registry.CalendarRegistry;

class BundledDataProviderTest {

    @Test
    void shipsSpainAndBrazil() {
        assertEquals(Set.of("ES", "ES-AN", "ES-AN-MA", "ES-CT", "BR", "BR-RJ", "BR-RJ-RIO"),
                new BundledDataProvider().layerIds());
    }

    @Test
    void isDiscoveredByServiceLoader() {
        assertTrue(CalendarRegistry.load().layerIds().contains("ES-AN-MA"));
    }
}
