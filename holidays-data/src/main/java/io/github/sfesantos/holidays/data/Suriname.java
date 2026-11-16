package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code SR.json} as Java constants; see {@link Spain}.
 */
public final class Suriname {

    public static final Layer NATIONAL = BundledDataProvider.require("SR");

    private Suriname() {}
}
