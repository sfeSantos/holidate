package io.github.sfesantos.holidays.registry;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.format.HolidayFormat;

/**
 * Layers read from every {@code .json} file directly inside a directory,
 * in file name order. The files are read once, when the provider is
 * created; a duplicate id across files fails there.
 */
public final class DirectoryDataProvider implements HolidayDataProvider {

    private final Map<String, Layer> layers;

    private DirectoryDataProvider(Map<String, Layer> layers) {
        this.layers = Map.copyOf(layers);
    }

    public static DirectoryDataProvider of(Path directory) {
        Objects.requireNonNull(directory, "directory must not be null");

        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException(directory + " is not a directory");
        }

        var layers = new LinkedHashMap<String, Layer>();

        for (var file : jsonFiles(directory)) {
            Layers.addAll(layers, HolidayFormat.json().parse(file), file.toString());
        }

        return new DirectoryDataProvider(layers);
    }

    @Override
    public Set<String> layerIds() {
        return layers.keySet();
    }

    @Override
    public Optional<Layer> layer(String id) {
        return Optional.ofNullable(layers.get(id));
    }

    private static Iterable<Path> jsonFiles(Path directory) {
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
