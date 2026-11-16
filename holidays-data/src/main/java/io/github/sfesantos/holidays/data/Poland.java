package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code PL.json} as a Java constant, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(Poland.NATIONAL)}. The data
 * stays in the JSON file; this only reads it.
 */
public final class Poland {

    public static final Layer NATIONAL = BundledDataProvider.require("PL");

    private Poland() {}
}
