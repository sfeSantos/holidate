package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code BR.json} as Java constants; see {@link Spain}.
 */
public final class Brazil {

    public static final Layer NATIONAL = BundledDataProvider.require("BR");
    public static final Layer RIO_DE_JANEIRO = BundledDataProvider.require("BR-RJ");
    public static final Layer RIO_DE_JANEIRO_CITY = BundledDataProvider.require("BR-RJ-RIO");

    private Brazil() {}
}
