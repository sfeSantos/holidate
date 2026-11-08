package io.github.sfesantos.holidays.registry;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.format.HolidayFormat;

/**
 * Layers read from JSON resources on the classpath, named relative to an
 * anchor class as {@link Class#getResourceAsStream} resolves them. The files
 * are read once, when the provider is created; a missing resource or a
 * duplicate id fails there.
 */
public final class ClasspathDataProvider implements HolidayDataProvider {

    private final Map<String, Layer> layers;

    private ClasspathDataProvider(Map<String, Layer> layers) {
        this.layers = Map.copyOf(layers);
    }

    public static ClasspathDataProvider of(Class<?> anchor, String... resources) {
        Objects.requireNonNull(anchor, "anchor must not be null");
        Objects.requireNonNull(resources, "resources must not be null");
        var layers = new LinkedHashMap<String, Layer>();

        for (var resource : resources) {
            Layers.addAll(layers, read(anchor, resource), resource);
        }

        return new ClasspathDataProvider(layers);
    }

    @Override
    public Set<String> layerIds() {
        return layers.keySet();
    }

    @Override
    public Optional<Layer> layer(String id) {
        return Optional.ofNullable(layers.get(id));
    }

    private static List<Layer> read(Class<?> anchor, String resource) {
        try (var stream = anchor.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalArgumentException("resource " + resource + " not found next to " + anchor.getName());
            }

            return HolidayFormat.json().parse(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
