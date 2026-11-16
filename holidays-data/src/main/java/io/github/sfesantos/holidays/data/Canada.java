package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code CA.json} as Java constants, for calendars built in
 * code: {@code HolidayCalendar.builder().layers(Canada.NATIONAL, Canada.ONTARIO)}.
 * The data stays in the JSON file; these only read it.
 *
 * <p>{@code NATIONAL} holds the days that are a statutory day off in every
 * province and territory. It is not the federal list of the Canada Labour
 * Code, which applies to federally regulated employers only. Each province
 * adds the general holidays of its own employment standards law.
 */
public final class Canada {

    public static final Layer NATIONAL = BundledDataProvider.require("CA");
    public static final Layer ALBERTA = BundledDataProvider.require("CA-AB");
    public static final Layer BRITISH_COLUMBIA = BundledDataProvider.require("CA-BC");
    public static final Layer MANITOBA = BundledDataProvider.require("CA-MB");
    public static final Layer NEW_BRUNSWICK = BundledDataProvider.require("CA-NB");
    public static final Layer NEWFOUNDLAND_AND_LABRADOR = BundledDataProvider.require("CA-NL");
    public static final Layer NOVA_SCOTIA = BundledDataProvider.require("CA-NS");
    public static final Layer NORTHWEST_TERRITORIES = BundledDataProvider.require("CA-NT");
    public static final Layer NUNAVUT = BundledDataProvider.require("CA-NU");
    public static final Layer ONTARIO = BundledDataProvider.require("CA-ON");
    public static final Layer PRINCE_EDWARD_ISLAND = BundledDataProvider.require("CA-PE");
    public static final Layer QUEBEC = BundledDataProvider.require("CA-QC");
    public static final Layer SASKATCHEWAN = BundledDataProvider.require("CA-SK");
    public static final Layer YUKON = BundledDataProvider.require("CA-YT");

    private Canada() {}
}
