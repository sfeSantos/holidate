package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code CO.json} as Java constants; see {@link Spain}.
 */
public final class Colombia {

    public static final Layer NATIONAL = BundledDataProvider.require("CO");

    private Colombia() {}
}
