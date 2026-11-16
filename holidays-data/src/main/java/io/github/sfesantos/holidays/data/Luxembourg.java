package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code LU.json} as a Java constant: the eleven statutory
 * holidays of the Code du travail.
 */
public final class Luxembourg {

    public static final Layer NATIONAL = BundledDataProvider.require("LU");

    private Luxembourg() {}
}
