package io.github.sfesantos.holidays.internal.json;

import java.util.List;
import java.util.Objects;

import io.github.sfesantos.holidays.HolidayRule;
import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.format.HolidayFormat;

public final class JsonHolidayFormat implements HolidayFormat {

    public static final JsonHolidayFormat INSTANCE = new JsonHolidayFormat();

    private JsonHolidayFormat() {}

    @Override
    public List<Layer> parse(String text) {
        Objects.requireNonNull(text, "text must not be null");

        return LayerJsonReader.read(JsonReader.parse(text));
    }

    @Override
    public HolidayRule parseRule(String text) {
        Objects.requireNonNull(text, "text must not be null");

        return LayerJsonReader.readRule(JsonReader.parse(text).asObject());
    }

    @Override
    public String write(List<Layer> layers) {
        Objects.requireNonNull(layers, "layers must not be null");

        return JsonWriter.write(LayerJsonWriter.write(layers));
    }
}
