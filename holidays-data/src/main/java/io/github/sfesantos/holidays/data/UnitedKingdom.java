package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code GB.json} as Java constants; see {@link Spain}.
 * {@code BASE} holds only the bank holidays the four nations share and is
 * there to be extended: a calendar takes it with one nation on top, as in
 * {@code layers(UnitedKingdom.BASE, UnitedKingdom.SCOTLAND)}.
 */
public final class UnitedKingdom {

    public static final Layer BASE = BundledDataProvider.require("GB");
    public static final Layer ENGLAND = BundledDataProvider.require("GB-ENG");
    public static final Layer WALES = BundledDataProvider.require("GB-WLS");
    public static final Layer SCOTLAND = BundledDataProvider.require("GB-SCT");
    public static final Layer NORTHERN_IRELAND = BundledDataProvider.require("GB-NIR");

    private UnitedKingdom() {}
}
