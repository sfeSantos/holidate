package io.github.sfesantos.holidays.internal;

import java.util.List;
import java.util.Set;

import io.github.sfesantos.holidays.HolidayCalendar;
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

    public CompiledCalendar(Weekend weekend, List<ResolvedHoliday> holidays, ObservanceRule observance,
            EasterAlgorithm easter, Set<HolidayType> nonWorkingTypes) {
        this.weekend = weekend;
        this.holidays = List.copyOf(holidays);
        this.observance = observance;
        this.easter = easter;
        this.nonWorkingTypes = Set.copyOf(nonWorkingTypes);
    }

    @Override
    public Weekend weekend() {
        return weekend;
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
}
