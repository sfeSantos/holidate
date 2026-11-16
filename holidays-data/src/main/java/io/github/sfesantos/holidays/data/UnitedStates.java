package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code US.json} as Java constants, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(UnitedStates.FEDERAL, UnitedStates.TEXAS)}.
 * The data stays in the JSON file; these only read it.
 */
public final class UnitedStates {

    public static final Layer FEDERAL = BundledDataProvider.require("US");
    public static final Layer MASSACHUSETTS = BundledDataProvider.require("US-MA");
    public static final Layer TEXAS = BundledDataProvider.require("US-TX");

    private UnitedStates() {}
}
