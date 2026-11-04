package io.github.sfesantos.holidays;

import java.time.Year;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import io.github.sfesantos.holidays.internal.UnionCalendar;

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

    /**
     * Several calendars seen as one, for work that spans markets: a day is a
     * holiday when it is a holiday in any member and a business day only when
     * it is a business day in all of them. The weekend is the union of the
     * members' weekends. {@code holidaysIn} lists every member's occurrences,
     * each with its own layer; when two members share a day, {@code holidayOn}
     * returns the first member's. A single calendar is returned as is.
     */
    static HolidayCalendar union(HolidayCalendar... calendars) {
        Objects.requireNonNull(calendars, "calendars must not be null");

        return union(List.of(calendars));
    }

    static HolidayCalendar union(Collection<? extends HolidayCalendar> calendars) {
        Objects.requireNonNull(calendars, "calendars must not be null");
        var members = List.copyOf(calendars);

        if (members.isEmpty()) {
            throw new IllegalArgumentException("union needs at least one calendar");
        }

        if (members.size() == 1) {
            return members.get(0);
        }

        return new UnionCalendar(members);
    }

    /** Precomputes the given years, both inclusive, so later queries never pay for indexing. */
    void warmUp(Year from, Year until);
}
