package io.github.sfesantos.holidays.internal;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.github.sfesantos.holidays.BusinessHours;
import io.github.sfesantos.holidays.HolidayType;
import io.github.sfesantos.holidays.ObservanceRule;
import io.github.sfesantos.holidays.Weekend;
import io.github.sfesantos.holidays.easter.EasterAlgorithm;
import io.github.sfesantos.holidays.internal.LayerResolver.ResolvedHoliday;

/**
 * The result of {@code HolidayCalendarBuilder.build()}: layers already
 * flattened, nothing left to resolve at query time.
 */
public final class CompiledCalendar extends IndexedCalendar {

    private final BusinessHours businessHours;
    private final List<ResolvedHoliday> holidays;
    private final ObservanceRule observance;
    private final EasterAlgorithm easter;
    private final Set<HolidayType> nonWorkingTypes;
    private final YearIndexBuilder indexBuilder = new YearIndexBuilder(this);

    public CompiledCalendar(Weekend weekend, BusinessHours businessHours, List<ResolvedHoliday> holidays,
            ObservanceRule observance, EasterAlgorithm easter, Set<HolidayType> nonWorkingTypes) {
        super(weekend);
        this.businessHours = Objects.requireNonNull(businessHours, "businessHours must not be null");
        this.holidays = List.copyOf(holidays);
        this.observance = observance;
        this.easter = easter;
        this.nonWorkingTypes = Set.copyOf(nonWorkingTypes);
    }

    @Override
    protected YearIndex buildIndex(int year) {
        return indexBuilder.build(year);
    }

    @Override
    protected boolean hasNoHolidays() {
        return holidays.isEmpty();
    }

    @Override
    public BusinessHours businessHours() {
        return businessHours;
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
}
