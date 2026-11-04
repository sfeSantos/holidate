package io.github.sfesantos.holidays;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * A holiday resolved to a concrete year. {@code observed} is the day actually
 * taken off after the observance rule; it equals {@code original} when nothing
 * moved. {@code closesAt} is when business closes on a {@link HolidayType#HALF_DAY}
 * and carries no meaning for other types.
 */
public record HolidayOccurrence(String name, LocalDate original, LocalDate observed, HolidayType type, String layer,
        LocalTime closesAt) {

    public HolidayOccurrence(String name, LocalDate original, LocalDate observed, HolidayType type, String layer) {
        this(name, original, observed, type, layer, LocalTime.NOON);
    }

    public HolidayOccurrence {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(original, "original must not be null");
        Objects.requireNonNull(observed, "observed must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(layer, "layer must not be null");
        Objects.requireNonNull(closesAt, "closesAt must not be null");
    }

    public boolean isHalfDay() {
        return type == HolidayType.HALF_DAY;
    }

    public boolean isMoved() {
        return !observed.equals(original);
    }
}
