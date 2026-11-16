package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code GT.json} as Java constants; see {@link Spain}.
 */
public final class Guatemala {

    public static final Layer NATIONAL = BundledDataProvider.require("GT");

    private Guatemala() {}
}
