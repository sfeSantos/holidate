package io.github.sfesantos.holidays.internal;

import java.util.List;

import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.Weekend;

/**
 * Several calendars seen as one: a day is a holiday when any member says so
 * and a business day only when every member agrees. Each year's index is
 * the members' indexes combined, so queries cost the same as on a single
 * calendar.
 */
public final class UnionCalendar extends IndexedCalendar {

    private final List<HolidayCalendar> members;
    private final UnionIndexBuilder indexBuilder;

    public UnionCalendar(List<? extends HolidayCalendar> members) {
        super(weekendOf(members));
        this.members = List.copyOf(members);
        this.indexBuilder = new UnionIndexBuilder(this.members);
    }

    @Override
    protected YearIndex buildIndex(int year) {
        return indexBuilder.build(year);
    }

    @Override
    protected boolean hasNoHolidays() {
        return false;
    }

    public List<HolidayCalendar> members() {
        return members;
    }

    private static Weekend weekendOf(List<? extends HolidayCalendar> members) {
        int mask = 0;

        for (var member : members) {
            mask |= member.weekend().mask();
        }

        return new Weekend(mask);
    }
}
