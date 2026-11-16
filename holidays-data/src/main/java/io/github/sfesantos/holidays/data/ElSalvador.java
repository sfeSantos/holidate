package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layers of {@code SV.json} as Java constants; see {@link Spain}.
 */
public final class ElSalvador {

    public static final Layer NATIONAL = BundledDataProvider.require("SV");

    private ElSalvador() {}
}
