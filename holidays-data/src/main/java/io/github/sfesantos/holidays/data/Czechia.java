package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code CZ.json} as a Java constant, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(Czechia.NATIONAL)}. The data
 * stays in the JSON file; this only reads it.
 */
public final class Czechia {

    public static final Layer NATIONAL = BundledDataProvider.require("CZ");

    private Czechia() {}
}
