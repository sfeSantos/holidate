package io.github.sfesantos.holidays.internal;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.HolidayOccurrence;
import io.github.sfesantos.holidays.HolidayType;
import io.github.sfesantos.holidays.ObservanceRule;
import io.github.sfesantos.holidays.Weekend;
import io.github.sfesantos.holidays.easter.EasterAlgorithm;
import io.github.sfesantos.holidays.internal.LayerResolver.ResolvedHoliday;

/**
 * The result of {@code HolidayCalendarBuilder.build()}: layers already
 * flattened, nothing left to resolve at query time.
 */
public final class CompiledCalendar implements HolidayCalendar {

    private final Weekend weekend;
    private final List<ResolvedHoliday> holidays;
    private final ObservanceRule observance;
    private final EasterAlgorithm easter;
    private final Set<HolidayType> nonWorkingTypes;
    private final CalendarCache cache;

    public CompiledCalendar(Weekend weekend, List<ResolvedHoliday> holidays, ObservanceRule observance,
            EasterAlgorithm easter, Set<HolidayType> nonWorkingTypes) {
        this.weekend = weekend;
        this.holidays = List.copyOf(holidays);
        this.observance = observance;
        this.easter = easter;
        this.nonWorkingTypes = Set.copyOf(nonWorkingTypes);
        this.cache = new CalendarCache(new YearIndexBuilder(this)::build);
    }

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

        return !weekend.contains(date.getDayOfWeek())
                && !index(date.getYear()).isNonWorkingHoliday(date.getDayOfYear());
    }

    @Override
    public void warmUp(Year from, Year until) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(until, "until must not be null");

        for (int year = from.getValue(); year <= until.getValue(); year++) {
            index(year);
        }
    }

    public List<ResolvedHoliday> holidays() {
        return holidays;
    }

    public ObservanceRule observance() {
        return observance;
    }

    public EasterAlgorithm easter() {
        return easter;
    }

    public Set<HolidayType> nonWorkingTypes() {
        return nonWorkingTypes;
    }

    public boolean isNonWorking(HolidayType type) {
        return nonWorkingTypes.contains(type);
    }

    public YearIndex index(int year) {
        return cache.get(year);
    }

    public int cachedYears() {
        return cache.size();
    }
}
