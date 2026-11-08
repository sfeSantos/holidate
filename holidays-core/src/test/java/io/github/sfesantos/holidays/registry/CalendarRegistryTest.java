package io.github.sfesantos.holidays.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.Holiday;
import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.ObservanceRule;

class CalendarRegistryTest {

    static final Layer SPAIN = Layer.named("ES")
            .add(Holiday.fixed("Año Nuevo", Month.JANUARY, 1), Holiday.fixed("Todos los Santos", Month.NOVEMBER, 1))
            .build();

    static final Layer ANDALUCIA = Layer.named("ES-AN")
            .extending("ES")
            .add(Holiday.fixed("Día de Andalucía", Month.FEBRUARY, 28))
            .observance(ObservanceRule.sundayToMonday())
            .build();

    static final Layer MALAGA = Layer.named("ES-AN-MA")
            .extending("ES-AN")
            .add(Holiday.fixed("Virgen de la Victoria", Month.SEPTEMBER, 8))
            .build();

    final CalendarRegistry registry = CalendarRegistry.of(HolidayDataProvider.of(SPAIN, ANDALUCIA, MALAGA));

    @Test
    void resolvesTheExtendsChainLowestFirst() {
        assertEquals(List.of(SPAIN, ANDALUCIA, MALAGA), registry.layers("ES-AN-MA"));
        assertEquals(List.of(SPAIN), registry.layers("ES"));
    }

    @Test
    void calendarStacksTheWholeChain() {
        var malaga = registry.calendar("ES-AN-MA");

        assertTrue(malaga.isHoliday(LocalDate.of(2026, 9, 8)));
        assertTrue(malaga.isHoliday(LocalDate.of(2026, 2, 28)));
        assertTrue(malaga.isHoliday(LocalDate.of(2026, 11, 2)));
        assertEquals("ES", malaga.holidayOn(LocalDate.of(2026, 11, 2)).orElseThrow().layer());
        assertFalse(registry.calendar("ES").isHoliday(LocalDate.of(2026, 11, 2)));
    }

    @Test
    void calendarsAreCachedById() {
        assertSame(registry.calendar("ES-AN"), registry.calendar("ES-AN"));
    }

    @Test
    void builderStartsFromTheChainAndAcceptsMoreLayers() {
        var closure = Layer.named("BRANCH")
                .add(Holiday.fixed("Inventory", Month.JULY, 3))
                .build();
        var calendar = registry.builder("ES-AN-MA").layer(closure).build();

        assertTrue(calendar.isHoliday(LocalDate.of(2026, 7, 3)));
        assertTrue(calendar.isHoliday(LocalDate.of(2026, 9, 8)));
    }

    @Test
    void layerIdsAreTheSortedUnionOfAllProviders() {
        var registry = CalendarRegistry.of(HolidayDataProvider.of(MALAGA), HolidayDataProvider.of(SPAIN, ANDALUCIA));

        assertEquals(List.of("ES", "ES-AN", "ES-AN-MA"), List.copyOf(registry.layerIds()));
    }

    @Test
    void theFirstProviderThatKnowsAnIdWins() {
        var override = Layer.named("ES")
                .add(Holiday.fixed("Año Nuevo", Month.JANUARY, 1))
                .build();
        var registry = CalendarRegistry.of(HolidayDataProvider.of(override), HolidayDataProvider.of(SPAIN, ANDALUCIA));

        assertSame(override, registry.layer("ES").orElseThrow());
        assertEquals(List.of(override, ANDALUCIA), registry.layers("ES-AN"));
        assertFalse(registry.calendar("ES-AN").isHoliday(LocalDate.of(2026, 11, 2)));
    }

    @Test
    void unknownIdFails() {
        var e = assertThrows(IllegalArgumentException.class, () -> registry.calendar("FR"));

        assertEquals("unknown layer id 'FR'", e.getMessage());
        assertTrue(registry.layer("FR").isEmpty());
    }

    @Test
    void missingParentFails() {
        var registry = CalendarRegistry.of(HolidayDataProvider.of(ANDALUCIA));
        var e = assertThrows(IllegalStateException.class, () -> registry.layers("ES-AN"));

        assertEquals("layer 'ES-AN' extends 'ES' but no provider defines it", e.getMessage());
    }

    @Test
    void cycleFails() {
        var a = Layer.named("A").extending("B").build();
        var b = Layer.named("B").extending("A").build();
        var registry = CalendarRegistry.of(HolidayDataProvider.of(a, b));

        assertThrows(IllegalStateException.class, () -> registry.layers("A"));
    }

    @Test
    void duplicateIdsWithinOneProviderFail() {
        var twin = Layer.named("ES").build();

        assertThrows(IllegalArgumentException.class, () -> HolidayDataProvider.of(SPAIN, twin));
    }

    @Test
    void loadAcceptsProvidersAheadOfTheDiscoveredOnes() {
        var registry = CalendarRegistry.load(HolidayDataProvider.of(SPAIN));

        assertEquals(Set.of("ES"), registry.layerIds());
    }
}
