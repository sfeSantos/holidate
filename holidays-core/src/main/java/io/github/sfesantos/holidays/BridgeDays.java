package io.github.sfesantos.holidays;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The bridge day scan written against {@link BusinessDayQueries#isBusinessDay}
 * only, behind the default of {@link BusinessDayQueries#bridgeDays(Year, int)}.
 */
final class BridgeDays {

    private static final int SEARCH_HORIZON_YEARS = 100;

    private BridgeDays() {}

    static List<LocalDate> in(BusinessDayQueries calendar, Year year, int maxLength) {
        Objects.requireNonNull(year, "year must not be null");

        if (maxLength < 1) {
            throw new IllegalArgumentException("maxLength must be at least 1: " + maxLength);
        }

        var length = year.length();
        var bridges = new ArrayList<LocalDate>();
        var day = 1;

        while (day <= length) {
            if (!calendar.isBusinessDay(year.atDay(day))) {
                day++;
                continue;
            }

            var start = day;

            while (day <= length && calendar.isBusinessDay(year.atDay(day))) {
                day++;
            }

            var run = (long) (day - start);

            if (run <= maxLength && start == 1) {
                run += adjacent(calendar, year.atDay(1), -1, maxLength);
            }

            if (run <= maxLength && day > length) {
                run += adjacent(calendar, year.atDay(length), 1, maxLength);
            }

            if (run <= maxLength) {
                for (var bridgeDay = start; bridgeDay < day; bridgeDay++) {
                    bridges.add(year.atDay(bridgeDay));
                }
            }
        }

        return Collections.unmodifiableList(bridges);
    }

    /**
     * Business days in a row right next to the date, on the side {@code step} points to, counted up to
     * {@code limit}; more than {@code limit} when the search horizon holds no day off, so an endless run
     * is never a bridge.
     */
    private static long adjacent(BusinessDayQueries calendar, LocalDate from, int step, int limit) {
        var edge = step < 0 ? LocalDate.MIN : LocalDate.MAX;
        var day = from;
        var count = 0;

        while (count < limit && !day.equals(edge)) {
            day = day.plusDays(step);

            if (Math.abs(day.getYear() - from.getYear()) > SEARCH_HORIZON_YEARS) {
                break;
            }

            if (!calendar.isBusinessDay(day)) {
                return count;
            }

            count++;
        }

        return count < limit ? limit + 1L : count;
    }
}
