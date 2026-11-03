package io.github.sfesantos.holidays;

import java.time.Year;

/**
 * An immutable, thread-safe calendar of holidays and business days. Queries
 * run against per-year indexes computed lazily and cached, so every call is
 * O(1) or O(log n) over precomputed data. Safe to call from any thread,
 * including event loops.
 */
public interface HolidayCalendar extends HolidayQueries, BusinessDayQueries {

    static HolidayCalendarBuilder builder() {
        return new HolidayCalendarBuilder();
    }

    /** Precomputes the given years, both inclusive, so later queries never pay for indexing. */
    void warmUp(Year from, Year until);
}
