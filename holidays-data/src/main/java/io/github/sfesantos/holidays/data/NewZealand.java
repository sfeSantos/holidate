package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code NZ.json} as a Java constant, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(NewZealand.NATIONAL)}. The
 * data stays in the JSON file; this only reads it. Provincial anniversary
 * days are not part of the pack.
 */
public final class NewZealand {

    public static final Layer NATIONAL = BundledDataProvider.require("NZ");

    private NewZealand() {}
}
