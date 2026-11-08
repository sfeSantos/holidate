package io.github.sfesantos.holidays.data;

import static io.github.sfesantos.holidays.data.Observed.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Year;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.HolidayCalendar;
import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.ObservanceRule;
import io.github.sfesantos.holidays.easter.EasterAlgorithm;

class ConstantsTest {

    @Test
    void layerConstantsAreThePackLayers() {
        assertSame(new BundledDataProvider().layer("ES-AN-MA").orElseThrow(), Spain.MALAGA);
        assertEquals(Optional.of("ES-AN"), Spain.MALAGA.parent());
        assertEquals("BR-RJ-RIO", Brazil.RIO_DE_JANEIRO_CITY.name());
    }

    @Test
    void layerConstantsBuildTheSameCalendarAsTheRegistry() {
        var fromConstants = HolidayCalendar.builder()
                .layers(Spain.NATIONAL, Spain.ANDALUCIA, Spain.MALAGA)
                .build();

        assertEquals(Observed.calendar("ES-AN-MA").holidaysIn(Year.of(2026)), fromConstants.holidaysIn(Year.of(2026)));
    }

    @Test
    void sharedDefinitionsResolveToTheExpectedDates() {
        var easter = EasterAlgorithm.gregorian();

        assertEquals(Optional.of(d(2026, 4, 3)), Holidays.GOOD_FRIDAY.dateIn(Year.of(2026), easter));
        assertEquals(Optional.of(d(2026, 4, 6)), Holidays.EASTER_MONDAY.dateIn(Year.of(2026), easter));
        assertEquals(Optional.of(d(2026, 5, 14)), Holidays.ASCENSION.dateIn(Year.of(2026), easter));
        assertEquals(Optional.of(d(2026, 5, 25)), Holidays.WHIT_MONDAY.dateIn(Year.of(2026), easter));
        assertEquals(Optional.of(d(2026, 6, 4)), Holidays.CORPUS_CHRISTI.dateIn(Year.of(2026), easter));
        assertEquals(Optional.of(d(2026, 2, 17)), Holidays.CARNIVAL_TUESDAY.dateIn(Year.of(2026), easter));
    }

    @Test
    void sharedDefinitionsStackWithLocalOnes() {
        var germany = HolidayCalendar.builder()
                .layer(Layer.named("DE")
                        .add(Holidays.NEW_YEAR, Holidays.GOOD_FRIDAY, Holidays.EASTER_MONDAY, Holidays.LABOUR_DAY,
                                Holidays.ASCENSION, Holidays.WHIT_MONDAY, Holidays.CHRISTMAS, Holidays.BOXING_DAY)
                        .observance(ObservanceRule.none())
                        .build())
                .build();

        assertEquals(8, germany.holidaysIn(Year.of(2026)).size());
        assertTrue(germany.isHoliday(d(2026, 5, 25)));
    }
}
