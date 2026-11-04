package io.github.sfesantos.holidays;

import java.time.LocalTime;
import java.util.Objects;

/**
 * The opening hours of a business day, {@code open} inclusive and
 * {@code close} exclusive, in the calendar's local time. {@link #allDay()}
 * is the default when a calendar declares none.
 */
public record BusinessHours(LocalTime open, LocalTime close) {

    private static final BusinessHours ALL_DAY = new BusinessHours(LocalTime.MIDNIGHT, LocalTime.MAX);

    public BusinessHours {
        Objects.requireNonNull(open, "open must not be null");
        Objects.requireNonNull(close, "close must not be null");

        if (!close.isAfter(open)) {
            throw new IllegalArgumentException("close must be after open: " + open + " to " + close);
        }
    }

    public static BusinessHours of(LocalTime open, LocalTime close) {
        return new BusinessHours(open, close);
    }

    public static BusinessHours allDay() {
        return ALL_DAY;
    }

    public boolean contains(LocalTime time) {
        Objects.requireNonNull(time, "time must not be null");

        return !time.isBefore(open) && (time.isBefore(close) || close.equals(LocalTime.MAX));
    }

    /** These hours cut short at {@code time}, or unchanged when {@code time} is not earlier than {@code close}. */
    public BusinessHours closingAt(LocalTime time) {
        Objects.requireNonNull(time, "time must not be null");

        return time.isBefore(close) ? new BusinessHours(open, time) : this;
    }

    public boolean isAllDay() {
        return equals(ALL_DAY);
    }

    @Override
    public String toString() {
        return isAllDay() ? "BusinessHours[all day]" : "BusinessHours[" + open + " to " + close + "]";
    }
}
