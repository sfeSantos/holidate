package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code AR.json} as Java constants; see {@link Spain}.
 */
public final class Argentina {

    public static final Layer NATIONAL = BundledDataProvider.require("AR");

    private Argentina() {}
}
