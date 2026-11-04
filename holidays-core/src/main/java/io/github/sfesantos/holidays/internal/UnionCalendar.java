package io.github.sfesantos.holidays.internal;

import java.time.LocalTime;
import java.util.List;

import io.github.sfesantos.holidays.BusinessHours;
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

    /** The hours every member is open. Fails when the members' hours do not overlap. */
    @Override
    public BusinessHours businessHours() {
        var open = LocalTime.MIDNIGHT;
        var close = LocalTime.MAX;

        for (var member : members) {
            var hours = member.businessHours();
            open = hours.open().isAfter(open) ? hours.open() : open;
            close = hours.close().isBefore(close) ? hours.close() : close;
        }

        if (!close.isAfter(open)) {
            throw new IllegalStateException("the members' business hours do not overlap");
        }

        return BusinessHours.of(open, close);
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
