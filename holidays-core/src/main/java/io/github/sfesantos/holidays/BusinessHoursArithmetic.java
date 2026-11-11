package io.github.sfesantos.holidays;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Business-hours arithmetic written against the public queries only, behind
 * the defaults of {@link BusinessDayQueries}. Times of day are nanoseconds
 * since midnight; a close of {@link LocalTime#MAX} is the end of the day.
 */
final class BusinessHoursArithmetic {

    private static final long NANOS_PER_DAY = 86_400_000_000_000L;
    private static final int SEARCH_HORIZON_YEARS = 100;

    private BusinessHoursArithmetic() {}

    static LocalDateTime add(BusinessDayQueries calendar, LocalDateTime start, Duration duration) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(duration, "duration must not be null");
        long nanos = duration.toNanos();

        if (nanos == 0) {
            return start;
        }

        return nanos > 0 ? forward(calendar, start, nanos) : backward(calendar, start, Math.negateExact(nanos));
    }

    static Duration between(BusinessDayQueries calendar, LocalDateTime from, LocalDateTime to) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");

        return Duration.ofNanos(from.isAfter(to) ? -openNanos(calendar, to, from) : openNanos(calendar, from, to));
    }

    private static LocalDateTime forward(BusinessDayQueries calendar, LocalDateTime start, long nanos) {
        var date = start.toLocalDate();
        long from = start.toLocalTime().toNanoOfDay();
        long remaining = nanos;
        int lastOpenYear = date.getYear();

        while (true) {
            var hours = calendar.businessHoursOn(date).orElse(null);

            if (hours != null) {
                long open = Math.max(from, hours.open().toNanoOfDay());
                long available = endOf(hours) - open;

                if (available > 0) {
                    if (remaining <= available) {
                        return at(date, open + remaining);
                    }

                    remaining -= available;
                    lastOpenYear = date.getYear();
                }
            }

            date = calendar.nextBusinessDay(date);
            from = 0;

            if (date.getYear() - lastOpenYear > SEARCH_HORIZON_YEARS) {
                throw noOpenTime(lastOpenYear);
            }
        }
    }

    private static LocalDateTime backward(BusinessDayQueries calendar, LocalDateTime start, long nanos) {
        var date = start.toLocalDate();
        long until = start.toLocalTime().toNanoOfDay();
        long remaining = nanos;
        int lastOpenYear = date.getYear();

        while (true) {
            var hours = calendar.businessHoursOn(date).orElse(null);

            if (hours != null) {
                long close = Math.min(until, endOf(hours));
                long available = close - hours.open().toNanoOfDay();

                if (available > 0) {
                    if (remaining <= available) {
                        return at(date, close - remaining);
                    }

                    remaining -= available;
                    lastOpenYear = date.getYear();
                }
            }

            date = calendar.previousBusinessDay(date);
            until = NANOS_PER_DAY;

            if (lastOpenYear - date.getYear() > SEARCH_HORIZON_YEARS) {
                throw noOpenTime(lastOpenYear);
            }
        }
    }

    /** Open time from {@code from} to {@code to}, which must not be before it. Steps by calendar day so it never searches past {@code to}. */
    private static long openNanos(BusinessDayQueries calendar, LocalDateTime from, LocalDateTime to) {
        var last = to.toLocalDate();
        long start = from.toLocalTime().toNanoOfDay();
        long total = 0;

        for (var date = from.toLocalDate(); !date.isAfter(last); date = date.plusDays(1)) {
            var hours = calendar.businessHoursOn(date).orElse(null);

            if (hours != null) {
                long open = Math.max(start, hours.open().toNanoOfDay());
                long close = date.equals(last) ? Math.min(to.toLocalTime().toNanoOfDay(), endOf(hours)) : endOf(hours);

                if (close > open) {
                    total = Math.addExact(total, close - open);
                }
            }

            start = 0;
        }

        return total;
    }

    private static long endOf(BusinessHours hours) {
        return hours.close().equals(LocalTime.MAX) ? NANOS_PER_DAY : hours.close().toNanoOfDay();
    }

    private static LocalDateTime at(LocalDate date, long nanoOfDay) {
        if (nanoOfDay == NANOS_PER_DAY) {
            return date.plusDays(1).atStartOfDay();
        }

        return LocalDateTime.of(date, LocalTime.ofNanoOfDay(nanoOfDay));
    }

    private static IllegalStateException noOpenTime(int year) {
        return new IllegalStateException("no open business hours found within " + SEARCH_HORIZON_YEARS
                + " years around " + year + "; business is never open");
    }
}
