package io.github.sfesantos.holidays.format;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import io.github.sfesantos.holidays.HolidayRule;
import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.internal.json.JsonHolidayFormat;

/**
 * Reads and writes layers as data. Files are UTF-8. Parsing fails fast with
 * a {@link HolidayFormatException} that names the line and column; writing
 * fails when a layer holds something that has no data form, such as a
 * {@code custom} rule or observance. {@code write} then {@code parse} gives
 * back equivalent layers, so calendars defined in Java can be exported.
 */
public interface HolidayFormat {

    /** The JSON format: {@code {"layers": [...]}}, described by {@code META-INF/holidays/holidays.schema.json}. */
    static HolidayFormat json() {
        return JsonHolidayFormat.INSTANCE;
    }

    List<Layer> parse(String text);

    default List<Layer> parse(Reader reader) {
        Objects.requireNonNull(reader, "reader must not be null");

        try {
            var out = new StringBuilder();
            var buffer = new char[8192];
            int read;

            while ((read = reader.read(buffer)) >= 0) {
                out.append(buffer, 0, read);
            }

            return parse(out.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default List<Layer> parse(Path file) {
        Objects.requireNonNull(file, "file must not be null");

        try {
            return parse(Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * One rule on its own, written as it appears under {@code "rule"} in a
     * file, for example {@code {"fixed": "01-01"}}. For sources that keep
     * holidays as rows instead of documents.
     */
    HolidayRule parseRule(String text);

    String write(List<Layer> layers);

    default void write(List<Layer> layers, Writer writer) {
        Objects.requireNonNull(writer, "writer must not be null");

        try {
            writer.write(write(layers));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default void write(List<Layer> layers, Path file) {
        Objects.requireNonNull(file, "file must not be null");

        try {
            Files.writeString(file, write(layers), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
