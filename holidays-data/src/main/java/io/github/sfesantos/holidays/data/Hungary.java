package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code HU.json} as a Java constant, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(Hungary.NATIONAL)}. The data
 * stays in the JSON file; this only reads it.
 */
public final class Hungary {

    public static final Layer NATIONAL = BundledDataProvider.require("HU");

    private Hungary() {}
}
