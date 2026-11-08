package io.github.sfesantos.holidays.registry;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.github.sfesantos.holidays.Layer;

final class LayerIndex implements HolidayDataProvider {

    private final Map<String, Layer> layers;

    LayerIndex(Map<String, Layer> layers) {
        this.layers = Map.copyOf(layers);
    }

    @Override
    public Set<String> layerIds() {
        return layers.keySet();
    }

    @Override
    public Optional<Layer> layer(String id) {
        return Optional.ofNullable(layers.get(id));
    }
}
