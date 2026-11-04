package io.github.sfesantos.holidays;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.github.sfesantos.holidays.easter.EasterAlgorithm;
import io.github.sfesantos.holidays.internal.CompiledCalendar;
import io.github.sfesantos.holidays.internal.LayerResolver;

/**
 * Not thread-safe. Layers are applied in the order given, the last one
 * having the highest precedence. Everything is validated in {@link #build()}.
 */
public final class HolidayCalendarBuilder {

    private final List<Layer> layers = new ArrayList<>();
    private Weekend weekend = Weekend.standard();
    private BusinessHours businessHours = BusinessHours.allDay();
    private ObservanceRule observance = ObservanceRule.none();
    private EasterAlgorithm easter = EasterAlgorithm.gregorian();
    private Set<HolidayType> nonWorkingTypes = EnumSet.of(HolidayType.PUBLIC, HolidayType.BANK);

    HolidayCalendarBuilder() {}

    public HolidayCalendarBuilder weekend(DayOfWeek... days) {
        return weekend(Weekend.of(days));
    }

    public HolidayCalendarBuilder weekend(Weekend weekend) {
        this.weekend = Objects.requireNonNull(weekend, "weekend must not be null");

        return this;
    }

    /** Opening hours of a business day. Without them a business day is open all day. */
    public HolidayCalendarBuilder businessHours(LocalTime open, LocalTime close) {
        return businessHours(BusinessHours.of(open, close));
    }

    public HolidayCalendarBuilder businessHours(BusinessHours businessHours) {
        this.businessHours = Objects.requireNonNull(businessHours, "businessHours must not be null");

        return this;
    }

    public HolidayCalendarBuilder layer(Layer layer) {
        layers.add(Objects.requireNonNull(layer, "layer must not be null"));

        return this;
    }

    public HolidayCalendarBuilder layers(Layer... layers) {
        for (var layer : layers) {
            layer(layer);
        }

        return this;
    }

    public HolidayCalendarBuilder layers(Iterable<Layer> layers) {
        layers.forEach(this::layer);

        return this;
    }

    /** Applies when no layer declares an observance rule of its own. */
    public HolidayCalendarBuilder observance(ObservanceRule observance) {
        this.observance = Objects.requireNonNull(observance, "observance must not be null");

        return this;
    }

    public HolidayCalendarBuilder easter(EasterAlgorithm easter) {
        this.easter = Objects.requireNonNull(easter, "easter must not be null");

        return this;
    }

    /** Which holiday types make a day non-working. Defaults to {@code PUBLIC} and {@code BANK}. */
    public HolidayCalendarBuilder nonWorkingTypes(HolidayType first, HolidayType... rest) {
        nonWorkingTypes = EnumSet.of(first, rest);

        return this;
    }

    public HolidayCalendar build() {
        requireUniqueLayerNames();

        if (observance == ObservanceRule.inherit()) {
            throw new IllegalArgumentException("calendar observance cannot be inherit()");
        }

        var resolution = LayerResolver.resolve(layers, observance);

        return new CompiledCalendar(weekend, businessHours, resolution.holidays(), resolution.observance(), easter,
                nonWorkingTypes);
    }

    private void requireUniqueLayerNames() {
        var names = new HashSet<String>();

        for (var layer : layers) {
            if (!names.add(layer.name())) {
                throw new IllegalArgumentException("layer name '" + layer.name() + "' is used more than once");
            }
        }
    }
}
