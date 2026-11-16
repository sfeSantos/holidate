package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code SE.json} as a Java constant, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(Sweden.NATIONAL)}. The data
 * stays in the JSON file; this only reads it.
 */
public final class Sweden {

    public static final Layer NATIONAL = BundledDataProvider.require("SE");

    private Sweden() {}
}
