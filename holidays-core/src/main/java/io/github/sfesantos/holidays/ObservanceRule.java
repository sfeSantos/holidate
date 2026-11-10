package io.github.sfesantos.holidays;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;
import java.util.function.UnaryOperator;

import io.github.sfesantos.holidays.internal.ObservanceNames;

/**
 * Moves a holiday off a day on which it is not observed. A rule performs one
 * step; the calendar repeats it until the date stops moving, at most seven
 * times, so a rule only has to be idempotent on a date it accepts.
 */
@FunctionalInterface
public interface ObservanceRule {

    LocalDate observe(LocalDate date, ObservanceContext context);

    static ObservanceRule none() {
        return Observances.NONE;
    }

    /** Marks a holiday as using the rule of the layer or calendar it belongs to. */
    static ObservanceRule inherit() {
        return Observances.INHERIT;
    }

    static ObservanceRule sundayToMonday() {
        return Observances.SUNDAY_TO_MONDAY;
    }

    /** Saturday or Sunday move forward to the next free weekday, as do collisions with other holidays. */
    static ObservanceRule weekendToMonday() {
        return Observances.WEEKEND_TO_MONDAY;
    }

    /** United States federal model: Saturday back to Friday, Sunday forward to Monday. */
    static ObservanceRule saturdayToFridaySundayToMonday() {
        return Observances.SATURDAY_TO_FRIDAY_SUNDAY_TO_MONDAY;
    }

    /** Closest day that is neither weekend nor another holiday; on a tie the earlier day wins. */
    static ObservanceRule nearestWeekday() {
        return Observances.NEAREST_WEEKDAY;
    }

    /** The built-in rule with that data name, such as {@code SUNDAY_TO_MONDAY}; the names are the ones data files use. */
    static ObservanceRule named(String name) {
        Objects.requireNonNull(name, "name must not be null");

        return ObservanceNames.parse(name)
                .orElseThrow(() -> new IllegalArgumentException(
                        "unknown observance \"" + name + "\"; expected one of " + ObservanceNames.names()));
    }

    static ObservanceRule custom(UnaryOperator<LocalDate> adjuster) {
        Objects.requireNonNull(adjuster, "adjuster must not be null");

        return (date, context) -> adjuster.apply(date);
    }
}

final class Observances {

    static final ObservanceRule NONE = (date, context) -> date;

    static final ObservanceRule INHERIT = (date, context) -> {
        throw new UnsupportedOperationException("inherit() must be resolved against the owning layer or calendar");
    };

    static final ObservanceRule SUNDAY_TO_MONDAY = (date, context) ->
            date.getDayOfWeek() == DayOfWeek.SUNDAY ? date.plusDays(1) : date;

    static final ObservanceRule WEEKEND_TO_MONDAY = (date, context) ->
            context.isNonWorking(date) ? date.plusDays(1) : date;

    static final ObservanceRule SATURDAY_TO_FRIDAY_SUNDAY_TO_MONDAY = (date, context) ->
            switch (date.getDayOfWeek()) {
                case SATURDAY -> date.minusDays(1);
                case SUNDAY -> date.plusDays(1);
                default -> date;
            };

    static final ObservanceRule NEAREST_WEEKDAY = Observances::nearestWeekday;

    private Observances() {}

    private static LocalDate nearestWeekday(LocalDate date, ObservanceContext context) {
        if (!context.isNonWorking(date)) {
            return date;
        }

        for (int distance = 1; distance <= 7; distance++) {
            var earlier = date.minusDays(distance);

            if (!context.isNonWorking(earlier)) {
                return earlier;
            }

            var later = date.plusDays(distance);

            if (!context.isNonWorking(later)) {
                return later;
            }
        }

        return date;
    }
}
