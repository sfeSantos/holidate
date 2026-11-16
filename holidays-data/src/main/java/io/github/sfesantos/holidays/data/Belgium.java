package io.github.sfesantos.holidays.data;

import io.github.sfesantos.holidays.Layer;

/**
 * The layer of {@code BE.json} as a Java constant: the ten statutory
 * holidays of the royal decree of 18 April 1974.
 */
public final class Belgium {

    public static final Layer NATIONAL = BundledDataProvider.require("BE");

    private Belgium() {}
}
