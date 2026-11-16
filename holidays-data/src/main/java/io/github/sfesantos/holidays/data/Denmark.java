package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code DK.json} as a Java constant, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(Denmark.NATIONAL)}. The data
 * stays in the JSON file; this only reads it.
 */
public final class Denmark {

    public static final Layer NATIONAL = BundledDataProvider.require("DK");

    private Denmark() {}
}
