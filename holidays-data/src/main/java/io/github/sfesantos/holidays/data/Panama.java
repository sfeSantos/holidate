package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code PA.json} as Java constants; see {@link Spain}.
 */
public final class Panama {

    public static final Layer NATIONAL = BundledDataProvider.require("PA");

    private Panama() {}
}
