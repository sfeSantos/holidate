package io.github.sfesantos.holidays.internal;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.UnaryOperator;

import io.github.sfesantos.holidays.BusinessHours;
import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.HolidayOccurrence;
import io.github.sfesantos.holidays.Weekend;

/**
 * A calendar answered entirely from one {@link YearIndex} per year, built
 * lazily by {@link #buildIndex(int)} and cached. Navigation walks the
 * indexes and gives up after {@link #SEARCH_HORIZON_YEARS} years without a
 * match, so a calendar that never yields a holiday or a business day cannot
 * hang a query.
 */
public abstract class IndexedCalendar implements HolidayCalendar {

    static final int SEARCH_HORIZON_YEARS = 100;
    private static final long NANOS_PER_DAY = 86_400_000_000_000L;

    private final Weekend weekend;
    private final CalendarCache cache = new CalendarCache(this::buildIndex);
    private final TemporalAdjuster nextBusinessDay = adjuster(this::nextBusinessDay);
    private final TemporalAdjuster nextOrSameBusinessDay = adjuster(this::nextOrSameBusinessDay);
    private final TemporalAdjuster previousBusinessDay = adjuster(this::previousBusinessDay);
    private final TemporalAdjuster previousOrSameBusinessDay = adjuster(this::previousOrSameBusinessDay);
    private final TemporalAdjuster firstBusinessDayOfMonth = nthBusinessDayOfMonthAdjuster(1);
    private final TemporalAdjuster lastBusinessDayOfMonth = nthBusinessDayOfMonthAdjuster(-1);
    private final TemporalQuery<Boolean> isHoliday = temporal -> isHoliday(LocalDate.from(temporal));
    private final TemporalQuery<Boolean> isBusinessDay = temporal -> isBusinessDay(LocalDate.from(temporal));
    private final TemporalQuery<Optional<HolidayOccurrence>> holidayOn = temporal -> holidayOn(LocalDate.from(temporal));

    protected IndexedCalendar(Weekend weekend) {
        this.weekend = Objects.requireNonNull(weekend, "weekend must not be null");
    }

    protected abstract YearIndex buildIndex(int year);

    /** True when no year can hold a holiday, which lets navigation skip the search. */
    protected abstract boolean hasNoHolidays();

    @Override
    public boolean isHoliday(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");

        return index(date.getYear()).isHoliday(date.getDayOfYear());
    }

    @Override
    public Optional<HolidayOccurrence> holidayOn(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");

        return Optional.ofNullable(index(date.getYear()).holidayOn(date.getDayOfYear()));
    }

    @Override
    public List<HolidayOccurrence> holidaysIn(Year year) {
        Objects.requireNonNull(year, "year must not be null");

        return index(year.getValue()).occurrences();
    }

    @Override
    public List<HolidayOccurrence> holidaysBetween(LocalDate from, LocalDate to) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");

        if (!from.isBefore(to)) {
            return List.of();
        }

        var found = new ArrayList<HolidayOccurrence>();
        int lastYear = to.getYear();

        for (int year = from.getYear(); year <= lastYear; year++) {
            var index = index(year);
            var observed = index.byObserved();
            int start = year == from.getYear() ? index.observedIndex(from.getDayOfYear()) : 0;
            int end = year == lastYear ? index.observedIndex(to.getDayOfYear()) : observed.length;

            for (int i = start; i < end; i++) {
                found.add(observed[i]);
            }
        }

        return List.copyOf(found);
    }

    @Override
    public Optional<HolidayOccurrence> nextHoliday(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");

        return holidayAtOrAfter(date.getYear(), date.getDayOfYear() + 1);
    }

    @Override
    public Optional<HolidayOccurrence> nextHolidayInclusive(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");

        return holidayAtOrAfter(date.getYear(), date.getDayOfYear());
    }

    @Override
    public Optional<HolidayOccurrence> previousHoliday(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");

        return holidayBefore(date.getYear(), date.getDayOfYear());
    }

    @Override
    public Optional<HolidayOccurrence> previousHolidayInclusive(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");

        return holidayBefore(date.getYear(), date.getDayOfYear() + 1);
    }

    @Override
    public OptionalLong daysUntilNextHoliday(LocalDate date) {
        var next = nextHoliday(date);

        if (next.isEmpty()) {
            return OptionalLong.empty();
        }

        return OptionalLong.of(ChronoUnit.DAYS.between(date, next.get().observed()));
    }

    @Override
    public TemporalQuery<Boolean> isHolidayQuery() {
        return isHoliday;
    }

    @Override
    public TemporalQuery<Optional<HolidayOccurrence>> holidayOnQuery() {
        return holidayOn;
    }

    private Optional<HolidayOccurrence> holidayAtOrAfter(int year, int dayOfYear) {
        if (hasNoHolidays()) {
            return Optional.empty();
        }

        int lastYear = Math.min(year + SEARCH_HORIZON_YEARS, Year.MAX_VALUE);
        int day = dayOfYear;

        for (int y = year; y <= lastYear; y++) {
            var index = index(y);
            int i = index.holidayAtOrAfter(day);

            if (i >= 0) {
                return Optional.of(index.byObserved()[i]);
            }

            day = 1;
        }

        return Optional.empty();
    }

    private Optional<HolidayOccurrence> holidayBefore(int year, int dayOfYear) {
        if (hasNoHolidays()) {
            return Optional.empty();
        }

        int firstYear = Math.max(year - SEARCH_HORIZON_YEARS, Year.MIN_VALUE);
        int day = dayOfYear;

        for (int y = year; y >= firstYear; y--) {
            var index = index(y);
            int i = index.holidayBefore(day);

            if (i >= 0) {
                return Optional.of(index.byObserved()[i]);
            }

            day = Integer.MAX_VALUE;
        }

        return Optional.empty();
    }

    @Override
    public Weekend weekend() {
        return weekend;
    }

    @Override
    public boolean isWeekend(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");

        return weekend.contains(date.getDayOfWeek());
    }

    @Override
    public boolean isBusinessDay(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");

        return index(date.getYear()).isBusinessDay(date.getDayOfYear());
    }

    @Override
    public Optional<BusinessHours> businessHoursOn(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        var index = index(date.getYear());
        int day = date.getDayOfYear();

        if (!index.isBusinessDay(day)) {
            return Optional.empty();
        }

        var hours = businessHours();
        var close = earliestClose(index, day, hours.close());

        if (!close.isAfter(hours.open())) {
            return Optional.empty();
        }

        return Optional.of(hours.closingAt(close));
    }

    @Override
    public boolean isHalfDay(LocalDate date) {
        var hours = businessHoursOn(date);

        return hours.isPresent() && hours.get().close().isBefore(businessHours().close());
    }

    @Override
    public boolean isOpen(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        var hours = businessHoursOn(dateTime.toLocalDate());

        return hours.isPresent() && hours.get().contains(dateTime.toLocalTime());
    }

    private static LocalTime earliestClose(YearIndex index, int day, LocalTime close) {
        var observed = index.byObserved();
        var earliest = close;

        for (int i = index.observedIndex(day); i < observed.length && observed[i].observed().getDayOfYear() == day; i++) {
            if (observed[i].isHalfDay() && observed[i].closesAt().isBefore(earliest)) {
                earliest = observed[i].closesAt();
            }
        }

        return earliest;
    }

    @Override
    public LocalDateTime addBusinessHours(LocalDateTime start, Duration duration) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(duration, "duration must not be null");
        long nanos = duration.toNanos();

        if (nanos == 0) {
            return start;
        }

        return nanos > 0 ? forwardHours(start, nanos) : backwardHours(start, Math.negateExact(nanos));
    }

    @Override
    public Duration businessDurationBetween(LocalDateTime from, LocalDateTime to) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");

        return Duration.ofNanos(from.isAfter(to) ? -openNanosBetween(to, from) : openNanosBetween(from, to));
    }

    /** Walks business days in the index with times as nanoseconds of the day, so only the result is allocated. */
    private LocalDateTime forwardHours(LocalDateTime start, long nanos) {
        var hours = businessHours();
        long dayOpen = hours.open().toNanoOfDay();
        int year = start.getYear();
        int day = start.getDayOfYear();
        int lastYear = horizonAfter(year);
        long from = start.toLocalTime().toNanoOfDay();
        long remaining = nanos;
        var index = index(year);

        while (true) {
            int found = index.businessDayAtOrAfter(day);

            if (found < 0) {
                if (year >= lastYear) {
                    throw noOpenTime(year);
                }

                index = index(++year);
                day = 1;
                from = 0;
                continue;
            }

            long open = found == day ? Math.max(from, dayOpen) : dayOpen;
            long available = endOfDay(earliestClose(index, found, hours.close())) - open;

            if (available > 0) {
                if (remaining <= available) {
                    return dateTime(year, found, open + remaining);
                }

                remaining -= available;
                lastYear = horizonAfter(year);
            }

            day = found + 1;
            from = 0;
        }
    }

    private LocalDateTime backwardHours(LocalDateTime start, long nanos) {
        var hours = businessHours();
        long dayOpen = hours.open().toNanoOfDay();
        int year = start.getYear();
        int day = start.getDayOfYear();
        int firstYear = horizonBefore(year);
        long until = start.toLocalTime().toNanoOfDay();
        long remaining = nanos;
        var index = index(year);

        while (true) {
            int found = index.businessDayAtOrBefore(day);

            if (found < 0) {
                if (year <= firstYear) {
                    throw noOpenTime(year);
                }

                index = index(--year);
                day = index.length();
                until = NANOS_PER_DAY;
                continue;
            }

            long dayClose = endOfDay(earliestClose(index, found, hours.close()));
            long close = found == day ? Math.min(until, dayClose) : dayClose;
            long available = close - dayOpen;

            if (available > 0) {
                if (remaining <= available) {
                    return dateTime(year, found, close - remaining);
                }

                remaining -= available;
                firstYear = horizonBefore(year);
            }

            day = found - 1;
            until = NANOS_PER_DAY;
        }
    }

    /** Open time from {@code from} to {@code to}, which must not be before it. */
    private long openNanosBetween(LocalDateTime from, LocalDateTime to) {
        var hours = businessHours();
        long dayOpen = hours.open().toNanoOfDay();
        int firstYear = from.getYear();
        int firstDay = from.getDayOfYear();
        int lastYear = to.getYear();
        int lastDay = to.getDayOfYear();
        int year = firstYear;
        int day = firstDay;
        long total = 0;
        var index = index(year);

        while (true) {
            int found = index.businessDayAtOrAfter(day);

            if (found < 0) {
                if (year == lastYear) {
                    return total;
                }

                index = index(++year);
                day = 1;
                continue;
            }

            if (year == lastYear && found > lastDay) {
                return total;
            }

            long open = dayOpen;
            long close = endOfDay(earliestClose(index, found, hours.close()));

            if (year == firstYear && found == firstDay) {
                open = Math.max(open, from.toLocalTime().toNanoOfDay());
            }

            if (year == lastYear && found == lastDay) {
                close = Math.min(close, to.toLocalTime().toNanoOfDay());
            }

            if (close > open) {
                total = Math.addExact(total, close - open);
            }

            day = found + 1;
        }
    }

    private static int horizonAfter(int year) {
        return Math.min(year + SEARCH_HORIZON_YEARS, Year.MAX_VALUE);
    }

    private static int horizonBefore(int year) {
        return Math.max(year - SEARCH_HORIZON_YEARS, Year.MIN_VALUE);
    }

    /** A close of {@link LocalTime#MAX} is the end of the day, so an all-day business day is 24 hours. */
    private static long endOfDay(LocalTime close) {
        return close.equals(LocalTime.MAX) ? NANOS_PER_DAY : close.toNanoOfDay();
    }

    private static LocalDateTime dateTime(int year, int dayOfYear, long nanoOfDay) {
        var date = LocalDate.ofYearDay(year, dayOfYear);

        if (nanoOfDay == NANOS_PER_DAY) {
            return date.plusDays(1).atStartOfDay();
        }

        return LocalDateTime.of(date, LocalTime.ofNanoOfDay(nanoOfDay));
    }

    private static IllegalStateException noOpenTime(int year) {
        return new IllegalStateException("no open business hours found within " + SEARCH_HORIZON_YEARS
                + " years around " + year + "; business is never open");
    }

    @Override
    public LocalDate nextBusinessDay(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");

        return businessDayAtOrAfter(date.getYear(), date.getDayOfYear() + 1);
    }

    @Override
    public LocalDate nextOrSameBusinessDay(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");

        return businessDayAtOrAfter(date.getYear(), date.getDayOfYear());
    }

    @Override
    public LocalDate previousBusinessDay(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");

        return businessDayAtOrBefore(date.getYear(), date.getDayOfYear() - 1);
    }

    @Override
    public LocalDate previousOrSameBusinessDay(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");

        return businessDayAtOrBefore(date.getYear(), date.getDayOfYear());
    }

    @Override
    public LocalDate addBusinessDays(LocalDate date, int days) {
        Objects.requireNonNull(date, "date must not be null");

        if (days == 0) {
            return date;
        }

        return days > 0 ? forward(date, days) : backward(date, -(long) days);
    }

    @Override
    public long businessDaysBetween(LocalDate from, LocalDate to) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");

        if (from.isAfter(to)) {
            return -businessDaysBetween(to, from);
        }

        int fromYear = from.getYear();
        int toYear = to.getYear();
        var first = index(fromYear);

        if (fromYear == toYear) {
            return first.businessDaysBefore(to.getDayOfYear()) - first.businessDaysBefore(from.getDayOfYear());
        }

        long count = first.businessDaysInYear() - first.businessDaysBefore(from.getDayOfYear());

        for (int year = fromYear + 1; year < toYear; year++) {
            count += index(year).businessDaysInYear();
        }

        return count + index(toYear).businessDaysBefore(to.getDayOfYear());
    }

    @Override
    public int businessDaysInMonth(YearMonth month) {
        Objects.requireNonNull(month, "month must not be null");
        var index = index(month.getYear());
        int start = firstDayOfYear(month);

        return index.businessDaysBefore(start + month.lengthOfMonth()) - index.businessDaysBefore(start);
    }

    @Override
    public Optional<LocalDate> firstBusinessDayOfMonth(YearMonth month) {
        Objects.requireNonNull(month, "month must not be null");
        int start = firstDayOfYear(month);
        int found = index(month.getYear()).businessDayAtOrAfter(start);

        if (found < 0 || found >= start + month.lengthOfMonth()) {
            return Optional.empty();
        }

        return Optional.of(month.atDay(found - start + 1));
    }

    @Override
    public Optional<LocalDate> lastBusinessDayOfMonth(YearMonth month) {
        Objects.requireNonNull(month, "month must not be null");
        int start = firstDayOfYear(month);
        int found = index(month.getYear()).businessDayAtOrBefore(start + month.lengthOfMonth() - 1);

        if (found < start) {
            return Optional.empty();
        }

        return Optional.of(month.atDay(found - start + 1));
    }

    @Override
    public Optional<LocalDate> nthBusinessDayOfMonth(YearMonth month, int n) {
        Objects.requireNonNull(month, "month must not be null");

        if (n == 0) {
            throw new IllegalArgumentException("n must not be 0");
        }

        var index = index(month.getYear());
        int start = firstDayOfYear(month);
        int before = index.businessDaysBefore(start);
        int count = index.businessDaysBefore(start + month.lengthOfMonth()) - before;
        long position = n > 0 ? n : count + (long) n + 1;

        if (position < 1 || position > count) {
            return Optional.empty();
        }

        int found = index.nthBusinessDay(before + (int) position);

        return Optional.of(month.atDay(found - start + 1));
    }

    private static int firstDayOfYear(YearMonth month) {
        return month.getMonth().firstDayOfYear(month.isLeapYear());
    }

    @Override
    public TemporalAdjuster firstBusinessDayOfMonthAdjuster() {
        return firstBusinessDayOfMonth;
    }

    @Override
    public TemporalAdjuster lastBusinessDayOfMonthAdjuster() {
        return lastBusinessDayOfMonth;
    }

    @Override
    public TemporalAdjuster nextBusinessDayAdjuster() {
        return nextBusinessDay;
    }

    @Override
    public TemporalAdjuster nextOrSameBusinessDayAdjuster() {
        return nextOrSameBusinessDay;
    }

    @Override
    public TemporalAdjuster previousBusinessDayAdjuster() {
        return previousBusinessDay;
    }

    @Override
    public TemporalAdjuster previousOrSameBusinessDayAdjuster() {
        return previousOrSameBusinessDay;
    }

    @Override
    public TemporalAdjuster addBusinessDaysAdjuster(int days) {
        return adjuster(date -> addBusinessDays(date, days));
    }

    @Override
    public TemporalQuery<Boolean> isBusinessDayQuery() {
        return isBusinessDay;
    }

    private LocalDate businessDayAtOrAfter(int year, int dayOfYear) {
        int lastYear = Math.min(year + SEARCH_HORIZON_YEARS, Year.MAX_VALUE);
        int day = dayOfYear;

        for (int y = year; y <= lastYear; y++) {
            int found = index(y).businessDayAtOrAfter(day);

            if (found > 0) {
                return LocalDate.ofYearDay(y, found);
            }

            day = 1;
        }

        throw noBusinessDays(year);
    }

    private LocalDate businessDayAtOrBefore(int year, int dayOfYear) {
        int firstYear = Math.max(year - SEARCH_HORIZON_YEARS, Year.MIN_VALUE);
        int day = dayOfYear;

        for (int y = year; y >= firstYear; y--) {
            int found = index(y).businessDayAtOrBefore(day);

            if (found > 0) {
                return LocalDate.ofYearDay(y, found);
            }

            day = Integer.MAX_VALUE;
        }

        throw noBusinessDays(year);
    }

    /** Skips whole years using their precomputed counts and binary-searches the final one. */
    private LocalDate forward(LocalDate date, long days) {
        int year = date.getYear();
        var index = index(year);
        long target = index.businessDaysBefore(date.getDayOfYear() + 1) + days;
        int emptyYears = 0;

        while (target > index.businessDaysInYear()) {
            target -= index.businessDaysInYear();
            index = index(++year);
            emptyYears = index.businessDaysInYear() == 0 ? emptyYears + 1 : 0;

            if (emptyYears > SEARCH_HORIZON_YEARS) {
                throw noBusinessDays(year);
            }
        }

        return LocalDate.ofYearDay(year, index.nthBusinessDay((int) target));
    }

    private LocalDate backward(LocalDate date, long days) {
        int year = date.getYear();
        var index = index(year);
        long remaining = days;
        long before = index.businessDaysBefore(date.getDayOfYear());
        int emptyYears = 0;

        while (remaining > before) {
            remaining -= before;
            index = index(--year);
            before = index.businessDaysInYear();
            emptyYears = before == 0 ? emptyYears + 1 : 0;

            if (emptyYears > SEARCH_HORIZON_YEARS) {
                throw noBusinessDays(year);
            }
        }

        return LocalDate.ofYearDay(year, index.nthBusinessDay((int) (before - remaining + 1)));
    }

    private static IllegalStateException noBusinessDays(int year) {
        return new IllegalStateException("no business day found within " + SEARCH_HORIZON_YEARS
                + " years around " + year + "; every day is a weekend day or a holiday");
    }

    private static TemporalAdjuster adjuster(UnaryOperator<LocalDate> move) {
        return temporal -> temporal.with(ChronoField.EPOCH_DAY, move.apply(LocalDate.from(temporal)).toEpochDay());
    }

    @Override
    public void warmUp(Year from, Year until) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(until, "until must not be null");

        for (int year = from.getValue(); year <= until.getValue(); year++) {
            index(year);
        }
    }

    public YearIndex index(int year) {
        return cache.get(year);
    }

    public int cachedYears() {
        return cache.size();
    }
}
