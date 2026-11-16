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

    private static final ClasspathDataProvider PACKS = ClasspathDataProvider.of(BundledDataProvider.class,
            "/META-INF/holidays/AT.json",
            "/META-INF/holidays/BE.json",
            "/META-INF/holidays/BR.json",
            "/META-INF/holidays/CH.json",
            "/META-INF/holidays/CZ.json",
            "/META-INF/holidays/DE.json",
            "/META-INF/holidays/DK.json",
            "/META-INF/holidays/EE.json",
            "/META-INF/holidays/ES.json",
            "/META-INF/holidays/FI.json",
            "/META-INF/holidays/FR.json",
            "/META-INF/holidays/GB.json",
            "/META-INF/holidays/HR.json",
            "/META-INF/holidays/HU.json",
            "/META-INF/holidays/IE.json",
            "/META-INF/holidays/IS.json",
            "/META-INF/holidays/IT.json",
            "/META-INF/holidays/LI.json",
            "/META-INF/holidays/LT.json",
            "/META-INF/holidays/LU.json",
            "/META-INF/holidays/LV.json",
            "/META-INF/holidays/MT.json",
            "/META-INF/holidays/NL.json",
            "/META-INF/holidays/NO.json",
            "/META-INF/holidays/PL.json",
            "/META-INF/holidays/PT.json",
            "/META-INF/holidays/SE.json",
            "/META-INF/holidays/SI.json",
            "/META-INF/holidays/SK.json");

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
