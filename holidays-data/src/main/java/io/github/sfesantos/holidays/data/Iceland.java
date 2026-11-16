package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code IS.json} as a Java constant, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(Iceland.NATIONAL)}. The data
 * stays in the JSON file; this only reads it.
 */
public final class Iceland {

    public static final Layer NATIONAL = BundledDataProvider.require("IS");

    private Iceland() {}
}
