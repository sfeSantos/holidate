package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code UY.json} as Java constants; see {@link Spain}.
 */
public final class Uruguay {

    public static final Layer NATIONAL = BundledDataProvider.require("UY");

    private Uruguay() {}
}
