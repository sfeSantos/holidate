package io.github.sfesantos.holidays.registry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import io.github.sfesantos.holidays.Layer;

final class Layers {

    private Layers() {}

    static Map<String, Layer> index(Collection<Layer> layers, String source) {
        var index = new LinkedHashMap<String, Layer>();
        addAll(index, layers, source);

        return index;
    }

    /** Adds every layer, failing when an id is already taken so a pack never silently shadows another. */
    static void addAll(Map<String, Layer> index, Collection<Layer> layers, String source) {
        for (var layer : layers) {
            if (index.putIfAbsent(layer.name(), layer) != null) {
                throw new IllegalArgumentException("layer id '" + layer.name() + "' is defined more than once in " + source);
            }
        }
    }
}
