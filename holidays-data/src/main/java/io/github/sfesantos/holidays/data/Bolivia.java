package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code BO.json} as Java constants; see {@link Spain}.
 */
public final class Bolivia {

    public static final Layer NATIONAL = BundledDataProvider.require("BO");

    private Bolivia() {}
}
