package io.github.sfesantos.holidays;

/**
 * An immutable, thread-safe calendar of holidays and business days. Queries
 * run against per-year indexes computed lazily and cached, so every call is
 * O(1) or O(log n) over precomputed data.
 */
public interface HolidayCalendar {

    static HolidayCalendarBuilder builder() {
        return new HolidayCalendarBuilder();
    }

    Weekend weekend();
}
