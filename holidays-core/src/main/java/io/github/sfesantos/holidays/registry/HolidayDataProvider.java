package io.github.sfesantos.holidays.registry;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import io.github.sfesantos.holidays.Layer;

/**
 * A source of layers keyed by id, the layer's name. Implementations are
 * discovered through {@link ServiceLoader} or handed to a
 * {@link CalendarRegistry} directly; a database or a vendor feed is one
 * class away. A provider must be thread-safe once built.
 */
public interface HolidayDataProvider {

    Set<String> layerIds();

    Optional<Layer> layer(String id);

    static HolidayDataProvider of(Layer... layers) {
        Objects.requireNonNull(layers, "layers must not be null");

        return of(List.of(layers));
    }

    static HolidayDataProvider of(Collection<Layer> layers) {
        Objects.requireNonNull(layers, "layers must not be null");

        return new LayerIndex(Layers.index(layers, "the given layers"));
    }
}
