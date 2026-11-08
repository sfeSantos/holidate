package io.github.sfesantos.holidays.data;

import java.util.Optional;
import java.util.Set;

import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.registry.ClasspathDataProvider;
import io.github.sfesantos.holidays.registry.HolidayDataProvider;

/**
 * The country packs shipped in this jar, one JSON file per country under
 * {@code META-INF/holidays}. Registered as a service, so
 * {@code CalendarRegistry.load()} finds it without configuration.
 */
public final class BundledDataProvider implements HolidayDataProvider {

    private static final ClasspathDataProvider PACKS =
            ClasspathDataProvider.of(BundledDataProvider.class, "/META-INF/holidays/ES.json", "/META-INF/holidays/BR.json");

    public BundledDataProvider() {}

    @Override
    public Set<String> layerIds() {
        return PACKS.layerIds();
    }

    @Override
    public Optional<Layer> layer(String id) {
        return PACKS.layer(id);
    }

    static Layer require(String id) {
        return PACKS.layer(id).orElseThrow(() -> new IllegalStateException("pack does not define " + id));
    }
}
