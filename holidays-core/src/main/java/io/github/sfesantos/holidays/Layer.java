package io.github.sfesantos.holidays;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * An ordered set of changes to the holidays inherited from the layers below:
 * additions, overrides by name, removals by name and, optionally, an
 * observance rule or a weekend that replaces the one in force so far. A
 * layer may name the layer it extends, which a registry uses to stack
 * layers from data files; the builder ignores it and stacks layers in the
 * order given. Layers are immutable and can be shared between calendars.
 */
public final class Layer {

    private final String name;
    private final String parent;
    private final Weekend weekend;
    private final List<Holiday> additions;
    private final Map<String, Holiday> overrides;
    private final Set<String> removals;
    private final ObservanceRule observance;

    Layer(String name, String parent, Weekend weekend, List<Holiday> additions, Map<String, Holiday> overrides,
            Set<String> removals, ObservanceRule observance) {
        this.name = name;
        this.parent = parent;
        this.weekend = weekend;
        this.additions = List.copyOf(additions);
        this.overrides = Map.copyOf(overrides);
        this.removals = Set.copyOf(removals);
        this.observance = observance;
    }

    public static LayerBuilder named(String name) {
        return new LayerBuilder(name);
    }

    public String name() {
        return name;
    }

    /** The name of the layer this one builds on, when the data says so. */
    public Optional<String> parent() {
        return Optional.ofNullable(parent);
    }

    /** The weekend this layer imposes on the calendar, when it declares one. */
    public Optional<Weekend> weekend() {
        return Optional.ofNullable(weekend);
    }

    public List<Holiday> additions() {
        return additions;
    }

    public Map<String, Holiday> overrides() {
        return overrides;
    }

    public Set<String> removals() {
        return removals;
    }

    public ObservanceRule observance() {
        return observance;
    }

    public boolean declaresObservance() {
        return observance != ObservanceRule.inherit();
    }

    @Override
    public String toString() {
        return "Layer[" + name + "]";
    }
}
