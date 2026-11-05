package io.github.sfesantos.holidays.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.sfesantos.holidays.Holiday;
import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.ObservanceRule;
import io.github.sfesantos.holidays.Weekend;

/**
 * Flattens an ordered list of layers (lowest precedence first) into the
 * holidays actually in force. Runs once at calendar build time.
 */
public final class LayerResolver {

    public record ResolvedHoliday(Holiday holiday, String layer) {}

    public record Resolution(List<ResolvedHoliday> holidays, ObservanceRule observance, Weekend weekend) {}

    private LayerResolver() {}

    /** The fallbacks apply when no layer declares an observance rule or a weekend; the highest layer that does wins. */
    public static Resolution resolve(List<Layer> layers, ObservanceRule fallbackObservance, Weekend fallbackWeekend) {
        var holidays = new LinkedHashMap<String, ResolvedHoliday>();
        var observance = fallbackObservance;
        var weekend = fallbackWeekend;

        for (var layer : layers) {
            applyRemovals(layer, holidays);
            applyOverrides(layer, holidays);
            applyAdditions(layer, holidays);

            if (layer.declaresObservance()) {
                observance = layer.observance();
            }

            weekend = layer.weekend().orElse(weekend);
        }

        return new Resolution(new ArrayList<>(holidays.values()), observance, weekend);
    }

    private static void applyRemovals(Layer layer, Map<String, ResolvedHoliday> holidays) {
        for (var name : layer.removals()) {
            if (holidays.remove(name) == null) {
                throw new IllegalStateException(layer + " removes '" + name + "' but no lower layer defines it");
            }
        }
    }

    private static void applyOverrides(Layer layer, Map<String, ResolvedHoliday> holidays) {
        for (var holiday : layer.overrides().values()) {
            if (!holidays.containsKey(holiday.name())) {
                throw new IllegalStateException(layer + " overrides '" + holiday.name() + "' but no lower layer defines it");
            }

            holidays.put(holiday.name(), new ResolvedHoliday(holiday, layer.name()));
        }
    }

    private static void applyAdditions(Layer layer, Map<String, ResolvedHoliday> holidays) {
        for (var holiday : layer.additions()) {
            if (holidays.containsKey(holiday.name())) {
                throw new IllegalStateException(layer + " adds '" + holiday.name() + "' which "
                        + holidays.get(holiday.name()).layer() + " already defines; use override or remove");
            }

            holidays.put(holiday.name(), new ResolvedHoliday(holiday, layer.name()));
        }
    }
}
