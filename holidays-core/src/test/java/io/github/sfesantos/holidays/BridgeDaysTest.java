package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;

import org.junit.jupiter.api.Test;

class BridgeDaysTest {

    private static final HolidayCalendar SPAIN = HolidayCalendar.builder()
            .layer(Layer.named("ES")
                    .add(Holiday.fixed("Año Nuevo", Month.JANUARY, 1),
                            Holiday.fixed("Epifanía", Month.JANUARY, 6),
                            Holiday.easterRelative("Jueves Santo", -3),
                            Holiday.easterRelative("Viernes Santo", -2),
                            Holiday.fixed("Fiesta del Trabajo", Month.MAY, 1),
                            Holiday.fixed("Asunción", Month.AUGUST, 15),
                            Holiday.fixed("Constitución", Month.DECEMBER, 6),
                            Holiday.fixed("Inmaculada", Month.DECEMBER, 8),
                            Holiday.fixed("Navidad", Month.DECEMBER, 25))
                    .build())
            .build();

    private static final HolidayCalendar NEVER = HolidayCalendar.builder()
            .weekend(DayOfWeek.values())
            .build();

    private static final HolidayCalendar ALWAYS = HolidayCalendar.builder()
            .weekend(Weekend.none())
            .build();

    @Test
    void aThursdayHolidayMakesFridayABridge() {
        var calendar = closedOn(d(2026, 5, 14));

        assertEquals(List.of(d(2026, 5, 15)), calendar.bridgeDays(Year.of(2026)));
        assertEquals(calendar.bridgeDays(Year.of(2026)), calendar.bridgeDays(Year.of(2026), 1));
    }

    @Test
    void aWednesdayHolidayLeavesTwoDayBridgesOnBothSides() {
        var calendar = closedOn(d(2026, 5, 13));

        assertEquals(List.of(), calendar.bridgeDays(Year.of(2026)));
        assertEquals(List.of(d(2026, 5, 11), d(2026, 5, 12), d(2026, 5, 14), d(2026, 5, 15)),
                calendar.bridgeDays(Year.of(2026), 2));
    }

    @Test
    void aDayNextToAHolidayOnOneSideOnlyIsNotABridge() {
        var calendar = closedOn(d(2026, 5, 15));

        assertEquals(List.of(), calendar.bridgeDays(Year.of(2026), 3));
        assertEquals(List.of(d(2026, 5, 11), d(2026, 5, 12), d(2026, 5, 13), d(2026, 5, 14)),
                calendar.bridgeDays(Year.of(2026), 4));
    }

    @Test
    void aRunAcrossTheYearEndIsMeasuredWholeAndSplitBetweenTheYears() {
        var calendar = closedOn(d(2025, 12, 30), d(2026, 1, 2));

        assertEquals(List.of(d(2025, 12, 29)), calendar.bridgeDays(Year.of(2025)));
        assertEquals(List.of(), calendar.bridgeDays(Year.of(2026)));
        assertEquals(List.of(d(2025, 12, 29), d(2025, 12, 31)), calendar.bridgeDays(Year.of(2025), 2));
        assertEquals(List.of(d(2026, 1, 1)), calendar.bridgeDays(Year.of(2026), 2));
    }

    @Test
    void aHolidayThatIsNotADayOffMakesNoBridge() {
        var calendar = HolidayCalendar.builder()
                .layer(Layer.named("x")
                        .add(Holiday.on("Observed only", d(2026, 5, 14)).ofType(HolidayType.OBSERVANCE))
                        .build())
                .build();

        assertEquals(List.of(), calendar.bridgeDays(Year.of(2026)));
    }

    @Test
    void withoutAWeekendOnlyHolidaysBoundABridge() {
        var calendar = HolidayCalendar.builder()
                .weekend(Weekend.none())
                .layer(Layer.named("x")
                        .add(Holiday.on("A", d(2026, 3, 10)), Holiday.on("B", d(2026, 3, 12)))
                        .build())
                .build();

        assertEquals(List.of(d(2026, 3, 11)), calendar.bridgeDays(Year.of(2026)));
        assertEquals(List.of(d(2026, 3, 11)), calendar.bridgeDays(Year.of(2026), 60));
        assertEquals(List.of(), calendar.bridgeDays(Year.of(2027), 366));
    }

    @Test
    void calendarsWithNoBusinessDayOrNoDayOffHaveNoBridges() {
        assertEquals(List.of(), NEVER.bridgeDays(Year.of(2026), 7));
        assertEquals(List.of(), ALWAYS.bridgeDays(Year.of(2026), Integer.MAX_VALUE));
        assertEquals(List.of(), withDefaultMethods(ALWAYS).bridgeDays(Year.of(2026), Integer.MAX_VALUE));
    }

    @Test
    void aLengthThatReachesTheWorkingWeekReturnsEveryBusinessDay() {
        var all = SPAIN.bridgeDays(Year.of(2026), 5);

        assertEquals(SPAIN.businessDaysBetween(d(2026, 1, 1), d(2027, 1, 1)), all.size());
    }

    @Test
    void aUnionFindsTheBridgesOfItsMembersTogether() {
        var union = HolidayCalendar.union(closedOn(d(2026, 5, 14)), closedOn(d(2026, 6, 9)));

        assertEquals(List.of(d(2026, 5, 15), d(2026, 6, 8)), union.bridgeDays(Year.of(2026)));
    }

    @Test
    void theListCannotBeModified() {
        var bridges = closedOn(d(2026, 5, 14)).bridgeDays(Year.of(2026));

        assertThrows(UnsupportedOperationException.class, () -> bridges.add(d(2026, 1, 1)));
    }

    @Test
    void rejectsANullYearAndALengthBelowOne() {
        for (var calendar : List.of(SPAIN, withDefaultMethods(SPAIN))) {
            assertThrows(NullPointerException.class, () -> calendar.bridgeDays(null));
            assertThrows(NullPointerException.class, () -> calendar.bridgeDays(null, 2));
            assertThrows(IllegalArgumentException.class, () -> calendar.bridgeDays(Year.of(2026), 0));
            assertThrows(IllegalArgumentException.class, () -> calendar.bridgeDays(Year.of(2026), -1));
        }
    }

    @Test
    void theInterfaceDefaultAgreesWithTheIndex() {
        var yearEnd = closedOn(d(2025, 12, 30), d(2026, 1, 2));

        for (var calendar : List.of(SPAIN, NEVER, ALWAYS, yearEnd)) {
            var foreign = withDefaultMethods(calendar);

            for (var year = 2020; year <= 2035; year++) {
                for (var maxLength = 1; maxLength <= 6; maxLength++) {
                    assertEquals(foreign.bridgeDays(Year.of(year), maxLength), calendar.bridgeDays(Year.of(year), maxLength),
                            year + " up to " + maxLength);
                }
            }
        }
    }

    @Test
    void everyBridgeDayIsABusinessDayInABoundedRunOfTheAllowedLength() {
        for (var year = 2020; year <= 2035; year++) {
            for (var maxLength = 1; maxLength <= 4; maxLength++) {
                var bridges = SPAIN.bridgeDays(Year.of(year), maxLength);

                assertEquals(bridges.stream().sorted().distinct().toList(), bridges);

                for (var day : bridges) {
                    assertTrue(SPAIN.isBusinessDay(day), day.toString());
                    assertTrue(runAround(SPAIN, day) <= maxLength, day + " up to " + maxLength);
                }

                if (maxLength > 1) {
                    assertTrue(bridges.containsAll(SPAIN.bridgeDays(Year.of(year), maxLength - 1)));
                }
            }
        }
    }

    /** The length of the run of business days the day belongs to. */
    private static int runAround(HolidayCalendar calendar, LocalDate day) {
        var first = day;
        var last = day;

        while (calendar.isBusinessDay(first.minusDays(1))) {
            first = first.minusDays(1);
        }

        while (calendar.isBusinessDay(last.plusDays(1))) {
            last = last.plusDays(1);
        }

        return (int) (last.toEpochDay() - first.toEpochDay()) + 1;
    }

    private static HolidayCalendar closedOn(LocalDate... dates) {
        var layer = Layer.named("x");

        for (var date : dates) {
            layer.add(Holiday.on("Closed " + date, date));
        }

        return HolidayCalendar.builder()
                .layer(layer.build())
                .build();
    }

    private static LocalDate d(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }

    /** A calendar as a foreign implementation would see it: the interface defaults over the target's other queries. */
    private static HolidayCalendar withDefaultMethods(HolidayCalendar target) {
        return (HolidayCalendar) Proxy.newProxyInstance(HolidayCalendar.class.getClassLoader(),
                new Class<?>[] {HolidayCalendar.class},
                (proxy, method, args) -> method.isDefault()
                        ? InvocationHandler.invokeDefault(proxy, method, args)
                        : method.invoke(target, args));
    }
}
