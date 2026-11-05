package io.github.sfesantos.holidays.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import io.github.sfesantos.holidays.ObservanceRule;

/** The data names of the built-in observance rules. A custom rule has none. */
public final class ObservanceNames {

    private static final Map<String, ObservanceRule> BY_NAME = new LinkedHashMap<>();

    static {
        BY_NAME.put("NONE", ObservanceRule.none());
        BY_NAME.put("SUNDAY_TO_MONDAY", ObservanceRule.sundayToMonday());
        BY_NAME.put("WEEKEND_TO_MONDAY", ObservanceRule.weekendToMonday());
        BY_NAME.put("SATURDAY_TO_FRIDAY_SUNDAY_TO_MONDAY", ObservanceRule.saturdayToFridaySundayToMonday());
        BY_NAME.put("NEAREST_WEEKDAY", ObservanceRule.nearestWeekday());
        BY_NAME.put("INHERIT", ObservanceRule.inherit());
    }

    private ObservanceNames() {}

    public static Optional<ObservanceRule> parse(String name) {
        return Optional.ofNullable(BY_NAME.get(name));
    }

    public static Optional<String> nameOf(ObservanceRule rule) {
        for (var entry : BY_NAME.entrySet()) {
            if (entry.getValue() == rule) {
                return Optional.of(entry.getKey());
            }
        }

        return Optional.empty();
    }

    public static String names() {
        return BY_NAME.keySet().toString();
    }
}
