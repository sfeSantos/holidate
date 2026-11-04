package io.github.sfesantos.holidays;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Not thread-safe. Names must be unique within a layer across additions,
 * overrides and removals; conflicts fail at {@link #build()}.
 */
public final class LayerBuilder {

    private final String name;
    private String parent;
    private Weekend weekend;
    private final List<Holiday> additions = new ArrayList<>();
    private final Map<String, Holiday> overrides = new LinkedHashMap<>();
    private final Set<String> removals = new LinkedHashSet<>();
    private ObservanceRule observance = ObservanceRule.inherit();

    LayerBuilder(String name) {
        Objects.requireNonNull(name, "name must not be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("layer name must not be blank");
        }

        this.name = name;
    }

    /** Names the layer this one builds on; see {@link Layer#parent()}. */
    public LayerBuilder extending(String parent) {
        Objects.requireNonNull(parent, "parent must not be null");

        if (parent.isBlank()) {
            throw new IllegalArgumentException("parent must not be blank");
        }

        if (parent.equals(name)) {
            throw new IllegalArgumentException("layer " + name + " cannot extend itself");
        }

        this.parent = parent;

        return this;
    }

    public LayerBuilder weekend(DayOfWeek... days) {
        return weekend(Weekend.of(days));
    }

    /** The weekend the calendar uses from this layer up, replacing the one in force below. */
    public LayerBuilder weekend(Weekend weekend) {
        this.weekend = Objects.requireNonNull(weekend, "weekend must not be null");

        return this;
    }

    public LayerBuilder add(Holiday holiday) {
        additions.add(Objects.requireNonNull(holiday, "holiday must not be null"));

        return this;
    }

    public LayerBuilder add(Holiday... holidays) {
        for (var holiday : holidays) {
            add(holiday);
        }

        return this;
    }

    public LayerBuilder add(Iterable<Holiday> holidays) {
        holidays.forEach(this::add);

        return this;
    }

    /** Replaces the holiday of the same name inherited from a lower layer. */
    public LayerBuilder override(Holiday holiday) {
        Objects.requireNonNull(holiday, "holiday must not be null");
        overrides.put(holiday.name(), holiday);

        return this;
    }

    /** Drops the holiday of that name inherited from a lower layer. */
    public LayerBuilder remove(String holidayName) {
        removals.add(Objects.requireNonNull(holidayName, "holidayName must not be null"));

        return this;
    }

    public LayerBuilder observance(ObservanceRule observance) {
        this.observance = Objects.requireNonNull(observance, "observance must not be null");

        return this;
    }

    public Layer build() {
        var seen = new LinkedHashSet<String>();

        for (var holiday : additions) {
            requireUnique(seen, holiday.name());
        }

        for (var name : overrides.keySet()) {
            requireUnique(seen, name);
        }

        for (var name : removals) {
            requireUnique(seen, name);
        }

        return new Layer(name, parent, weekend, additions, overrides, removals, observance);
    }

    private void requireUnique(Set<String> seen, String holidayName) {
        if (!seen.add(holidayName)) {
            throw new IllegalArgumentException("layer " + name + " declares '" + holidayName + "' more than once");
        }
    }
}
