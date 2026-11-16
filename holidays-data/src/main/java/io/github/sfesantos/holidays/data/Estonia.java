package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code EE.json} as a Java constant, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(Estonia.NATIONAL)}. The data
 * stays in the JSON file; this only reads it.
 */
public final class Estonia {

    public static final Layer NATIONAL = BundledDataProvider.require("EE");

    private Estonia() {}
}
