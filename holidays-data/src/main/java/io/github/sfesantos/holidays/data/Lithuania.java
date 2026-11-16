package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code LT.json} as a Java constant, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(Lithuania.NATIONAL)}. The data
 * stays in the JSON file; this only reads it.
 */
public final class Lithuania {

    public static final Layer NATIONAL = BundledDataProvider.require("LT");

    private Lithuania() {}
}
