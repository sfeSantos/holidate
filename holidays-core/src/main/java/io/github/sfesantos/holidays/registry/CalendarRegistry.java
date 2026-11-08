package io.github.sfesantos.holidays.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.HolidayCalendarBuilder;
import io.github.sfesantos.holidays.Layer;

/**
 * Resolves a layer id to a calendar by stacking the layer on the chain it
 * extends: {@code ES-AN-MA} is built from {@code ES}, {@code ES-AN} and
 * {@code ES-AN-MA}, in that order. Providers are asked in the order given
 * and the first that knows an id wins, so a provider placed before the
 * shipped pack overrides it. Calendars are built once per id and cached;
 * the registry is thread-safe.
 */
public final class CalendarRegistry {

    private final List<HolidayDataProvider> providers;
    private final Map<String, HolidayCalendar> calendars = new ConcurrentHashMap<>();

    private CalendarRegistry(List<HolidayDataProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    public static CalendarRegistry of(HolidayDataProvider... providers) {
        Objects.requireNonNull(providers, "providers must not be null");

        return of(List.of(providers));
    }

    public static CalendarRegistry of(List<? extends HolidayDataProvider> providers) {
        Objects.requireNonNull(providers, "providers must not be null");

        return new CalendarRegistry(new ArrayList<>(providers));
    }

    /**
     * Every provider registered through {@link ServiceLoader}, preceded by
     * the given ones so that they take precedence over the shipped packs.
     */
    public static CalendarRegistry load(HolidayDataProvider... first) {
        Objects.requireNonNull(first, "first must not be null");
        var providers = new ArrayList<HolidayDataProvider>(List.of(first));

        ServiceLoader.load(HolidayDataProvider.class).forEach(providers::add);

        return new CalendarRegistry(providers);
    }

    /** Every id any provider knows, sorted. */
    public Set<String> layerIds() {
        var ids = new TreeSet<String>();

        for (var provider : providers) {
            ids.addAll(provider.layerIds());
        }

        return Collections.unmodifiableSet(ids);
    }

    public Optional<Layer> layer(String id) {
        Objects.requireNonNull(id, "id must not be null");

        for (var provider : providers) {
            var layer = provider.layer(id);

            if (layer.isPresent()) {
                return layer;
            }
        }

        return Optional.empty();
    }

    /** The layer and the chain it extends, lowest first, ready for {@link HolidayCalendarBuilder#layers(Iterable)}. */
    public List<Layer> layers(String id) {
        var chain = new ArrayList<Layer>();
        var visited = new LinkedHashSet<String>();
        var layer = layer(id).orElseThrow(() -> new IllegalArgumentException("unknown layer id '" + id + "'"));

        while (true) {
            if (!visited.add(layer.name())) {
                throw new IllegalStateException("layer '" + layer.name() + "' extends itself through " + visited);
            }

            chain.add(layer);
            var parent = layer.parent();

            if (parent.isEmpty()) {
                break;
            }

            layer = requireParent(layer, parent.get());
        }

        Collections.reverse(chain);

        return List.copyOf(chain);
    }

    /** A builder preloaded with {@link #layers(String)}, for calendars that need more than the data says. */
    public HolidayCalendarBuilder builder(String id) {
        return HolidayCalendar.builder().layers(layers(id));
    }

    public HolidayCalendar calendar(String id) {
        Objects.requireNonNull(id, "id must not be null");

        return calendars.computeIfAbsent(id, key -> builder(key).build());
    }

    private Layer requireParent(Layer child, String parentId) {
        return layer(parentId).orElseThrow(() -> new IllegalStateException(
                "layer '" + child.name() + "' extends '" + parentId + "' but no provider defines it"));
    }
}
