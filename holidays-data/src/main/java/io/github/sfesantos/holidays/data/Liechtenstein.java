package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code LI.json} as a Java constant: the statutory holidays of
 * Art. 18 Abs. 2 Arbeitsgesetz.
 */
public final class Liechtenstein {

    public static final Layer NATIONAL = BundledDataProvider.require("LI");

    private Liechtenstein() {}
}
