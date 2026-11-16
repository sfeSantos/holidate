package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code HN.json} as Java constants; see {@link Spain}.
 */
public final class Honduras {

    public static final Layer NATIONAL = BundledDataProvider.require("HN");

    private Honduras() {}
}
