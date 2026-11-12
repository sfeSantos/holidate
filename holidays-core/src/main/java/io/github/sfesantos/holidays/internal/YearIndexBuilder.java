package io.github.sfesantos.holidays.internal;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.sfesantos.holidays.HolidayOccurrence;
import io.github.sfesantos.holidays.HolidayRule;
import io.github.sfesantos.holidays.ObservanceContext;
import io.github.sfesantos.holidays.Weekend;
import io.github.sfesantos.holidays.internal.LayerResolver.ResolvedHoliday;

/**
 * Builds a {@link YearIndex}. Observance can push a holiday into the
 * neighbouring year (1 January on a Saturday observed on 31 December), so
 * the previous and next years are placed too and their spillovers kept.
 * A calendar with a rule whose Easter offset and shifts add up to about a
 * year can land a date two years away, and places two years on each side.
 */
final class YearIndexBuilder {

    private static final Comparator<HolidayOccurrence> BY_OBSERVED = Comparator.comparing(HolidayOccurrence::observed);
    private static final Comparator<HolidayOccurrence> BY_ORIGINAL = Comparator.comparing(HolidayOccurrence::original);

    /** A date moved by more days than this may leave the neighbouring year once observance has moved it too. */
    private static final int NEAR_DAYS = 365 - 14;

    private final CompiledCalendar calendar;
    private final int reach;

    YearIndexBuilder(CompiledCalendar calendar) {
        this.calendar = calendar;
        this.reach = reachOf(calendar.holidays());
    }

    YearIndex build(int year) {
        var own = place(year);
        var observedHere = new ArrayList<HolidayOccurrence>();

        for (int placed = year - reach; placed <= year + reach; placed++) {
            collectObservedIn(year, placed == year ? own : place(placed), observedHere);
        }

        observedHere.sort(BY_OBSERVED);
        own.sort(BY_ORIGINAL);

        return assemble(year, observedHere, own);
    }

    /** How many years on each side of a year can hold a date observed in it. */
    private static int reachOf(List<ResolvedHoliday> holidays) {
        for (var resolved : holidays) {
            if (Math.abs(daysMoved(resolved.holiday().rule())) > NEAR_DAYS) {
                return 2;
            }
        }

        return 1;
    }

    private static long daysMoved(HolidayRule rule) {
        return switch (rule) {
            case HolidayRule.Shifted shifted -> shifted.days() + daysMoved(shifted.base());
            case HolidayRule.EasterRelative easter -> easter.offsetDays();
            default -> 0;
        };
    }

    private YearIndex assemble(int year, List<HolidayOccurrence> observedHere, List<HolidayOccurrence> own) {
        int length = Year.isLeap(year) ? 366 : 365;
        var anyHoliday = new long[YearIndex.WORDS];
        var nonWorking = new long[YearIndex.WORDS];

        for (var occurrence : observedHere) {
            int day = occurrence.observed().getDayOfYear();
            YearIndex.set(anyHoliday, day);

            if (calendar.isNonWorking(occurrence.type())) {
                YearIndex.set(nonWorking, day);
            }
        }

        var businessDays = businessDays(year, length, nonWorking);

        return new YearIndex(year, length, anyHoliday, nonWorking, businessDays,
                observedHere.toArray(HolidayOccurrence[]::new), own);
    }

    private long[] businessDays(int year, int length, long[] nonWorking) {
        var businessDays = new long[YearIndex.WORDS];
        var weekend = calendar.weekend();
        var day = LocalDate.ofYearDay(year, 1).getDayOfWeek();

        for (int d = 1; d <= length; d++) {
            if (!weekend.contains(day) && !YearIndex.bit(nonWorking, d)) {
                YearIndex.set(businessDays, d);
            }

            day = day.plus(1);
        }

        return businessDays;
    }

    private List<HolidayOccurrence> place(int year) {
        if (year < Year.MIN_VALUE || year > Year.MAX_VALUE) {
            return List.of();
        }

        var placed = new ArrayList<HolidayOccurrence>();
        var context = new PlacementContext(calendar.weekend(), new HashSet<>());

        for (var resolved : calendar.holidays()) {
            for (var original : resolved.holiday().datesIn(Year.of(year), calendar.easter())) {
                placed.add(place(resolved, original, context));
            }
        }

        return placed;
    }

    private HolidayOccurrence place(ResolvedHoliday resolved, LocalDate original, PlacementContext context) {
        var holiday = resolved.holiday();
        var rule = holiday.inheritsObservance() ? calendar.observance() : holiday.observance();
        var observed = ObservanceChain.observe(rule, original, context);

        if (calendar.isNonWorking(holiday.type())) {
            context.taken().add(observed);
        }

        return new HolidayOccurrence(holiday.name(), original, observed, holiday.type(), resolved.layer(),
                holiday.closesAt());
    }

    private static void collectObservedIn(int year, List<HolidayOccurrence> placed, List<HolidayOccurrence> into) {
        for (var occurrence : placed) {
            if (occurrence.observed().getYear() == year) {
                into.add(occurrence);
            }
        }
    }

    private record PlacementContext(Weekend weekend, Set<LocalDate> taken) implements ObservanceContext {

        @Override
        public boolean isWeekend(LocalDate date) {
            return weekend.contains(date.getDayOfWeek());
        }

        @Override
        public boolean isHoliday(LocalDate date) {
            return taken.contains(date);
        }
    }
}
