package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.Month;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class LayerTest {

    private static final Holiday CHRISTMAS = Holiday.fixed("Christmas", Month.DECEMBER, 25);
    private static final Holiday NEW_YEAR = Holiday.fixed("New Year", Month.JANUARY, 1);

    @Test
    void collectsAdditionsOverridesAndRemovals() {
        var layer = Layer.named("ES-CT")
                .add(CHRISTMAS, NEW_YEAR)
                .override(Holiday.fixed("Constitution", Month.DECEMBER, 7))
                .remove("Epiphany")
                .build();

        assertEquals("ES-CT", layer.name());
        assertEquals(List.of(CHRISTMAS, NEW_YEAR), layer.additions());
        assertEquals(Set.of("Constitution"), layer.overrides().keySet());
        assertEquals(Set.of("Epiphany"), layer.removals());
    }

    @Test
    void observanceIsInheritedByDefault() {
        var plain = Layer.named("plain").build();
        var moving = Layer.named("moving").observance(ObservanceRule.sundayToMonday()).build();

        assertFalse(plain.declaresObservance());
        assertTrue(moving.declaresObservance());
        assertEquals(ObservanceRule.sundayToMonday(), moving.observance());
    }

    @Test
    void duplicateNamesInTheSameLayerFailAtBuild() {
        var twice = Layer.named("x").add(CHRISTMAS).add(CHRISTMAS);
        var addAndRemove = Layer.named("x").add(CHRISTMAS).remove("Christmas");
        var overrideAndRemove = Layer.named("x").override(CHRISTMAS).remove("Christmas");

        assertThrows(IllegalArgumentException.class, twice::build);
        assertThrows(IllegalArgumentException.class, addAndRemove::build);
        assertThrows(IllegalArgumentException.class, overrideAndRemove::build);
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> Layer.named(" "));
    }

    @Test
    void collectionsAreImmutable() {
        var layer = Layer.named("x").add(CHRISTMAS).build();

        assertThrows(UnsupportedOperationException.class, () -> layer.additions().add(NEW_YEAR));
    }

    @Test
    void parentAndWeekendAreOptional() {
        var plain = Layer.named("ES").build();
        var regional = Layer.named("ES-AN").extending("ES").weekend(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY).build();

        assertTrue(plain.parent().isEmpty());
        assertTrue(plain.weekend().isEmpty());
        assertEquals("ES", regional.parent().orElseThrow());
        assertEquals(Weekend.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY), regional.weekend().orElseThrow());
    }

    @Test
    void parentIsValidated() {
        assertThrows(NullPointerException.class, () -> Layer.named("ES").extending(null));
        assertThrows(IllegalArgumentException.class, () -> Layer.named("ES").extending(" "));
        assertThrows(IllegalArgumentException.class, () -> Layer.named("ES").extending("ES"));
        assertThrows(NullPointerException.class, () -> Layer.named("ES").weekend((Weekend) null));
    }
}
