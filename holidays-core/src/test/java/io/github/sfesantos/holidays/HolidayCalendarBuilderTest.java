package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.EnumSet;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.easter.EasterAlgorithm;
import io.github.sfesantos.holidays.internal.CompiledCalendar;

class HolidayCalendarBuilderTest {

    private static final Layer SPAIN = Layer.named("ES").add(Holiday.fixed("Christmas", Month.DECEMBER, 25)).build();

    private static CompiledCalendar compile(HolidayCalendarBuilder builder) {
        return assertInstanceOf(CompiledCalendar.class, builder.build());
    }

    @Test
    void defaults() {
        var calendar = compile(HolidayCalendar.builder());

        assertEquals(Weekend.standard(), calendar.weekend());
        assertEquals(ObservanceRule.none(), calendar.observance());
        assertSame(EasterAlgorithm.gregorian(), calendar.easter());
        assertEquals(EnumSet.of(HolidayType.PUBLIC, HolidayType.BANK), calendar.nonWorkingTypes());
        assertEquals(0, calendar.holidays().size());
    }

    @Test
    void layersAreResolvedInOrder() {
        var andalucia = Layer.named("ES-AN")
                .add(Holiday.fixed("Andalucia Day", Month.FEBRUARY, 28))
                .observance(ObservanceRule.sundayToMonday())
                .build();

        var calendar = compile(HolidayCalendar.builder().layers(SPAIN, andalucia));

        assertEquals(2, calendar.holidays().size());
        assertEquals("ES-AN", calendar.holidays().get(1).layer());
        assertEquals(ObservanceRule.sundayToMonday(), calendar.observance());
    }

    @Test
    void calendarObservanceIsTheFallback() {
        var calendar = compile(HolidayCalendar.builder()
                .layer(SPAIN)
                .observance(ObservanceRule.weekendToMonday()));

        assertEquals(ObservanceRule.weekendToMonday(), calendar.observance());
    }

    @Test
    void customWeekendEasterAndTypes() {
        EasterAlgorithm fixedEaster = year -> LocalDate.of(year, 4, 1);

        var calendar = compile(HolidayCalendar.builder()
                .weekend(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
                .easter(fixedEaster)
                .nonWorkingTypes(HolidayType.PUBLIC));

        assertEquals(Weekend.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY), calendar.weekend());
        assertSame(fixedEaster, calendar.easter());
        assertEquals(EnumSet.of(HolidayType.PUBLIC), calendar.nonWorkingTypes());
    }

    @Test
    void duplicateLayerNamesFail() {
        var builder = HolidayCalendar.builder().layers(SPAIN, Layer.named("ES").build());

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void layerConflictsSurfaceAtBuild() {
        var builder = HolidayCalendar.builder().layers(SPAIN, Layer.named("top").remove("Nope").build());

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void inheritIsNotAValidCalendarObservance() {
        var builder = HolidayCalendar.builder().observance(ObservanceRule.inherit());

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void rejectsNulls() {
        var builder = HolidayCalendar.builder();

        assertThrows(NullPointerException.class, () -> builder.layer(null));
        assertThrows(NullPointerException.class, () -> builder.weekend((Weekend) null));
        assertThrows(NullPointerException.class, () -> builder.easter(null));
    }

    @Test
    void layerWeekendOverridesTheBuilderFallback() {
        var gulf = Layer.named("AE").weekend(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY).build();
        var fromLayer = HolidayCalendar.builder().layer(gulf).build();
        var fallback = HolidayCalendar.builder().weekend(DayOfWeek.SUNDAY).layer(Layer.named("x").build()).build();

        assertEquals(Weekend.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY), fromLayer.weekend());
        assertEquals(Weekend.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
                HolidayCalendar.builder().weekend(DayOfWeek.SUNDAY).layer(gulf).build().weekend());
        assertEquals(Weekend.of(DayOfWeek.SUNDAY), fallback.weekend());
    }
}
