module io.github.sfesantos.holidays.data {
    requires transitive io.github.sfesantos.holidays;

    exports io.github.sfesantos.holidays.data;

    provides io.github.sfesantos.holidays.registry.HolidayDataProvider
            with io.github.sfesantos.holidays.data.BundledDataProvider;
}
