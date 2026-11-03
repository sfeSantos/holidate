package io.github.sfesantos.holidays;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * The days of the week that are not working days. Stored as a 7-bit mask so
 * {@link #contains(DayOfWeek)} is a single AND on the hot path.
 */
public record Weekend(int mask) {

    private static final int ALL_DAYS = 0b1111111;
    private static final Weekend STANDARD = of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    private static final Weekend NONE = new Weekend(0);

    public Weekend {
        if ((mask & ~ALL_DAYS) != 0) {
            throw new IllegalArgumentException("mask uses bits outside the seven days of the week: " + mask);
        }
    }

    public static Weekend standard() {
        return STANDARD;
    }

    public static Weekend none() {
        return NONE;
    }

    public static Weekend of(DayOfWeek... days) {
        Objects.requireNonNull(days, "days must not be null");
        int mask = 0;

        for (var day : days) {
            mask |= bit(Objects.requireNonNull(day, "day must not be null"));
        }

        return new Weekend(mask);
    }

    public static Weekend of(Set<DayOfWeek> days) {
        return of(days.toArray(DayOfWeek[]::new));
    }

    public boolean contains(DayOfWeek day) {
        return (mask & bit(day)) != 0;
    }

    public Set<DayOfWeek> days() {
        var days = EnumSet.noneOf(DayOfWeek.class);

        for (var day : DayOfWeek.values()) {
            if (contains(day)) {
                days.add(day);
            }
        }

        return days;
    }

    public boolean isEmpty() {
        return mask == 0;
    }

    static int bit(DayOfWeek day) {
        return 1 << day.ordinal();
    }

    @Override
    public String toString() {
        return "Weekend" + days();
    }
}
