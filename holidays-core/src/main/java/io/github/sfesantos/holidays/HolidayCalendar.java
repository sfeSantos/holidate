package io.github.sfesantos.holidays;

/**
 * An immutable, thread-safe calendar of holidays and business days.
 *
 * <p>Instances are built once and then only read. All queries run against
 * per-year indexes that are computed lazily and cached, so every call is
 * O(1) or O(log n) over precomputed data.
 */
public interface HolidayCalendar {
}
