package io.github.sfesantos.holidays;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * The time of day, in a market's zone, after which an instruction counts
 * for the next day. An instruction received at or after the cut-off is
 * booked on the following date; the calendar then applies a
 * {@link BusinessDayConvention} to that date.
 */
public record CutOff(LocalTime time, ZoneId zone) {

    public CutOff {
        Objects.requireNonNull(time, "time must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
    }

    public static CutOff at(LocalTime time, ZoneId zone) {
        return new CutOff(time, zone);
    }

    /** The booking date of an instruction received at that instant. */
    public LocalDate dateOf(Instant instant) {
        Objects.requireNonNull(instant, "instant must not be null");

        return dateOf(LocalDateTime.ofInstant(instant, zone));
    }

    /** The booking date of an instruction received at that local time, read in this cut-off's zone. */
    public LocalDate dateOf(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        var date = dateTime.toLocalDate();

        return dateTime.toLocalTime().isBefore(time) ? date : date.plusDays(1);
    }
}
