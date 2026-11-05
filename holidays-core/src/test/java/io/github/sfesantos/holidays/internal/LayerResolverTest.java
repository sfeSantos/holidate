package io.github.sfesantos.holidays.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.DayOfWeek;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.Holiday;
import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.ObservanceRule;
import io.github.sfesantos.holidays.Weekend;
import io.github.sfesantos.holidays.internal.LayerResolver.ResolvedHoliday;

class LayerResolverTest {

    private static final Holiday CHRISTMAS = Holiday.fixed("Christmas", Month.DECEMBER, 25);
    private static final Holiday CONSTITUTION = Holiday.fixed("Constitution", Month.DECEMBER, 6);
    private static final Holiday EPIPHANY = Holiday.fixed("Epiphany", Month.JANUARY, 6);

    private static final Layer SPAIN = Layer.named("ES").add(CHRISTMAS, CONSTITUTION, EPIPHANY).build();

    @Test
    void higherLayersAddToLowerOnes() {
        var andalucia = Layer.named("ES-AN").add(Holiday.fixed("Andalucia Day", Month.FEBRUARY, 28)).build();

        var names = LayerResolver.resolve(List.of(SPAIN, andalucia), ObservanceRule.none(), Weekend.standard())
                .holidays()
                .stream()
                .map(r -> r.holiday().name())
                .toList();

        assertEquals(List.of("Christmas", "Constitution", "Epiphany", "Andalucia Day"), names);
    }

    @Test
    void removeAndOverrideChangeInheritedHolidays() {
        var moved = Holiday.fixed("Constitution", Month.DECEMBER, 7);
        var catalunya = Layer.named("ES-CT").remove("Epiphany").override(moved).build();

        var resolved = LayerResolver.resolve(List.of(SPAIN, catalunya), ObservanceRule.none(), Weekend.standard()).holidays();

        assertEquals(List.of(new ResolvedHoliday(CHRISTMAS, "ES"), new ResolvedHoliday(moved, "ES-CT")), resolved);
    }

    @Test
    void addingANameDefinedBelowFails() {
        var clash = Layer.named("top").add(Holiday.fixed("Christmas", Month.DECEMBER, 24)).build();

        var ex = assertThrows(IllegalStateException.class,
                () -> LayerResolver.resolve(List.of(SPAIN, clash), ObservanceRule.none(), Weekend.standard()));

        assertEquals("Layer[top] adds 'Christmas' which ES already defines; use override or remove", ex.getMessage());
    }

    @Test
    void removingOrOverridingAnUnknownNameFails() {
        var removes = Layer.named("top").remove("Nope").build();
        var overrides = Layer.named("top").override(Holiday.fixed("Nope", Month.MAY, 1)).build();

        assertThrows(IllegalStateException.class, () -> LayerResolver.resolve(List.of(SPAIN, removes), ObservanceRule.none(), Weekend.standard()));
        assertThrows(IllegalStateException.class, () -> LayerResolver.resolve(List.of(SPAIN, overrides), ObservanceRule.none(), Weekend.standard()));
    }

    @Test
    void highestLayerDeclaringAnObservanceWins() {
        var andalucia = Layer.named("ES-AN").observance(ObservanceRule.sundayToMonday()).build();
        var malaga = Layer.named("ES-AN-MA").build();

        var withLayers = LayerResolver.resolve(List.of(SPAIN, andalucia, malaga), ObservanceRule.none(), Weekend.standard());
        var withoutLayers = LayerResolver.resolve(List.of(SPAIN), ObservanceRule.nearestWeekday(), Weekend.standard());

        assertEquals(ObservanceRule.sundayToMonday(), withLayers.observance());
        assertEquals(ObservanceRule.nearestWeekday(), withoutLayers.observance());
    }

    @Test
    void highestLayerDeclaringAWeekendWins() {
        var gulf = Layer.named("AE").weekend(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY).build();
        var branch = Layer.named("AE-DXB").build();

        var withLayers = LayerResolver.resolve(List.of(SPAIN, gulf, branch), ObservanceRule.none(), Weekend.standard());
        var withoutLayers = LayerResolver.resolve(List.of(SPAIN), ObservanceRule.none(), Weekend.of(DayOfWeek.SUNDAY));

        assertEquals(Weekend.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY), withLayers.weekend());
        assertEquals(Weekend.of(DayOfWeek.SUNDAY), withoutLayers.weekend());
    }
}
