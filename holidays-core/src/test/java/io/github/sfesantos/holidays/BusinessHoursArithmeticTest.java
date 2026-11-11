package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class BusinessHoursArithmeticTest {

    private static final LocalTime NINE = LocalTime.of(9, 0);
    private static final LocalTime HALF_PAST_FIVE = LocalTime.of(17, 30);
    private static final Duration SLOT = Duration.ofMinutes(30);

    private static final Layer SPAIN = Layer.named("ES")
            .add(Holiday.fixed("Navidad", Month.DECEMBER, 25),
                    Holiday.fixed("Nochebuena", Month.DECEMBER, 24).closingAt(LocalTime.of(13, 0)),
                    Holiday.fixed("Nochevieja", Month.DECEMBER, 31).ofType(HolidayType.HALF_DAY))
            .build();

    private static final HolidayCalendar OFFICE = HolidayCalendar.builder()
            .businessHours(NINE, HALF_PAST_FIVE)
            .layer(SPAIN)
            .build();

    private static final HolidayCalendar ALL_DAY = HolidayCalendar.builder().layer(SPAIN).build();

    private static final HolidayCalendar NEVER = HolidayCalendar.builder()
            .weekend(DayOfWeek.values())
            .build();

    private static final LocalDate TUESDAY = LocalDate.of(2026, 12, 22);
    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 12, 23);
    private static final LocalDate CHRISTMAS_EVE = LocalDate.of(2026, 12, 24);
    private static final LocalDate CHRISTMAS = LocalDate.of(2026, 12, 25);
    private static final LocalDate SATURDAY = LocalDate.of(2026, 12, 26);
    private static final LocalDate MONDAY = LocalDate.of(2026, 12, 28);

    /** Each test runs on the indexed calendar and on the interface defaults a foreign implementation would get. */
    static Stream<UnaryOperator<HolidayCalendar>> implementations() {
        return Stream.of(UnaryOperator.identity(), BusinessHoursArithmeticTest::withDefaultMethods);
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void spendsTheDurationInsideOpeningHours(UnaryOperator<HolidayCalendar> implementation) {
        var office = implementation.apply(OFFICE);
        var start = WEDNESDAY.atTime(16, 0);

        assertEquals(CHRISTMAS_EVE.atTime(11, 30), office.addBusinessHours(start, Duration.ofHours(4)));
        assertEquals(MONDAY.atTime(11, 30), office.addBusinessHours(start, Duration.ofHours(8)));
        assertEquals(WEDNESDAY.atTime(16, 30), office.addBusinessHours(start, SLOT));
        assertEquals(CHRISTMAS_EVE.atTime(9, 0, 0, 500_000_000),
                office.addBusinessHours(WEDNESDAY.atTime(17, 29, 59, 500_000_000), Duration.ofSeconds(1)));
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void startOutsideOpeningHoursCountsFromTheNextOpening(UnaryOperator<HolidayCalendar> implementation) {
        var office = implementation.apply(OFFICE);
        var hour = Duration.ofHours(1);

        assertEquals(WEDNESDAY.atTime(10, 0), office.addBusinessHours(WEDNESDAY.atTime(7, 0), hour));
        assertEquals(CHRISTMAS_EVE.atTime(10, 0), office.addBusinessHours(WEDNESDAY.atTime(18, 0), hour));
        assertEquals(CHRISTMAS_EVE.atTime(10, 0), office.addBusinessHours(WEDNESDAY.atTime(17, 30), hour));
        assertEquals(MONDAY.atTime(10, 0), office.addBusinessHours(CHRISTMAS_EVE.atTime(13, 0), hour));
        assertEquals(MONDAY.atTime(10, 0), office.addBusinessHours(CHRISTMAS.atTime(10, 0), hour));
        assertEquals(MONDAY.atTime(10, 0), office.addBusinessHours(SATURDAY.atTime(10, 0), hour));
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void durationEndingAtClosingTimeStopsThere(UnaryOperator<HolidayCalendar> implementation) {
        var office = implementation.apply(OFFICE);
        var start = WEDNESDAY.atTime(16, 0);

        assertEquals(WEDNESDAY.atTime(17, 30), office.addBusinessHours(start, Duration.ofMinutes(90)));
        assertEquals(CHRISTMAS_EVE.atTime(9, 0, 0, 1), office.addBusinessHours(start, Duration.ofMinutes(90).plusNanos(1)));
        assertEquals(CHRISTMAS_EVE.atTime(13, 0), office.addBusinessHours(CHRISTMAS_EVE.atTime(12, 0), Duration.ofHours(1)));
        assertEquals(MONDAY.atTime(10, 0), office.addBusinessHours(CHRISTMAS_EVE.atTime(12, 0), Duration.ofHours(2)));
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void zeroReturnsTheStartEvenWhenClosed(UnaryOperator<HolidayCalendar> implementation) {
        var office = implementation.apply(OFFICE);

        assertEquals(SATURDAY.atTime(10, 0), office.addBusinessHours(SATURDAY.atTime(10, 0), Duration.ZERO));
        assertEquals(WEDNESDAY.atTime(10, 0), office.addBusinessHours(WEDNESDAY.atTime(10, 0), Duration.ZERO));
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void negativeDurationsMoveBackwards(UnaryOperator<HolidayCalendar> implementation) {
        var office = implementation.apply(OFFICE);

        assertEquals(WEDNESDAY.atTime(16, 0), office.addBusinessHours(CHRISTMAS_EVE.atTime(11, 30), Duration.ofHours(-4)));
        assertEquals(WEDNESDAY.atTime(16, 0), office.addBusinessHours(MONDAY.atTime(11, 30), Duration.ofHours(-8)));
        assertEquals(WEDNESDAY.atTime(16, 0), office.addBusinessHours(CHRISTMAS_EVE.atTime(9, 0), Duration.ofMinutes(-90)));
        assertEquals(CHRISTMAS_EVE.atTime(12, 0), office.addBusinessHours(MONDAY.atTime(8, 0), Duration.ofHours(-1)));
        assertEquals(CHRISTMAS_EVE.atTime(12, 0), office.addBusinessHours(CHRISTMAS_EVE.atTime(15, 0), Duration.ofHours(-1)));
        assertEquals(CHRISTMAS_EVE.atTime(9, 0), office.addBusinessHours(SATURDAY.atTime(10, 0), Duration.ofHours(-4)));
        assertEquals(TUESDAY.atTime(17, 29, 59, 999_999_999),
                office.addBusinessHours(WEDNESDAY.atTime(9, 0), Duration.ofNanos(-1)));
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void durationBetweenCountsOpenTimeOnly(UnaryOperator<HolidayCalendar> implementation) {
        var office = implementation.apply(OFFICE);
        var start = WEDNESDAY.atTime(16, 0);

        assertEquals(Duration.ofHours(4), office.businessDurationBetween(start, CHRISTMAS_EVE.atTime(11, 30)));
        assertEquals(Duration.ofHours(8), office.businessDurationBetween(start, MONDAY.atTime(11, 30)));
        assertEquals(Duration.ofHours(-8), office.businessDurationBetween(MONDAY.atTime(11, 30), start));
        assertEquals(Duration.ofMinutes(510), office.businessDurationBetween(WEDNESDAY.atStartOfDay(), CHRISTMAS_EVE.atStartOfDay()));
        assertEquals(Duration.ofMinutes(30), office.businessDurationBetween(WEDNESDAY.atTime(10, 0), WEDNESDAY.atTime(10, 30)));
        assertEquals(Duration.ZERO, office.businessDurationBetween(CHRISTMAS.atStartOfDay(), MONDAY.atStartOfDay()));
        assertEquals(Duration.ZERO, office.businessDurationBetween(WEDNESDAY.atTime(18, 0), CHRISTMAS_EVE.atTime(9, 0)));
        assertEquals(Duration.ZERO, office.businessDurationBetween(start, start));
        assertEquals(Duration.ofMinutes(240 + 180 + 5 * 510),
                office.businessDurationBetween(CHRISTMAS_EVE.atStartOfDay(), LocalDate.of(2027, 1, 5).atStartOfDay()));
    }

    /** Checks both methods against a count of open half hours over three weeks around Christmas. */
    @ParameterizedTest
    @MethodSource("implementations")
    void agreesWithACountOfOpenHalfHours(UnaryOperator<HolidayCalendar> implementation) {
        var office = implementation.apply(OFFICE);
        var origin = LocalDate.of(2026, 12, 17).atStartOfDay();
        int slots = 21 * 48;
        var openBefore = new int[slots + 1];

        for (int i = 0; i < slots; i++) {
            openBefore[i + 1] = openBefore[i] + (OFFICE.isOpen(origin.plus(SLOT.multipliedBy(i))) ? 1 : 0);
        }

        for (int a = 0; a <= slots; a += 5) {
            var from = origin.plus(SLOT.multipliedBy(a));

            for (int b = 0; b <= slots; b += 7) {
                var to = origin.plus(SLOT.multipliedBy(b));
                var between = office.businessDurationBetween(from, to);

                assertEquals(SLOT.multipliedBy(openBefore[b] - openBefore[a]), between, from + " to " + to);
                assertEquals(between.negated(), office.businessDurationBetween(to, from), to + " to " + from);
            }

            for (int k = 1; k <= 60; k += 3) {
                if (openBefore[a] < k || openBefore[slots] - openBefore[a] < k) {
                    continue;
                }

                int forward = a;
                int backward = a;

                while (openBefore[forward] - openBefore[a] < k) {
                    forward++;
                }

                while (openBefore[a] - openBefore[backward] < k) {
                    backward--;
                }

                assertEquals(origin.plus(SLOT.multipliedBy(forward)), office.addBusinessHours(from, SLOT.multipliedBy(k)),
                        from + " plus " + k);
                assertEquals(origin.plus(SLOT.multipliedBy(backward)), office.addBusinessHours(from, SLOT.multipliedBy(-k)),
                        from + " minus " + k);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void addingTheDurationBetweenLandsOnAnOpenInstant(UnaryOperator<HolidayCalendar> implementation) {
        var office = implementation.apply(OFFICE);
        var origin = TUESDAY.atStartOfDay();
        int slots = 8 * 48;

        for (int a = 0; a <= slots; a += 3) {
            var from = origin.plus(SLOT.multipliedBy(a));

            for (int b = 0; b <= slots; b++) {
                var to = origin.plus(SLOT.multipliedBy(b));

                if (!OFFICE.isOpen(to)) {
                    continue;
                }

                var landed = office.addBusinessHours(from, office.businessDurationBetween(from, to));

                if (from.isBefore(to) && to.toLocalTime().equals(NINE)) {
                    assertEquals(Duration.ZERO, office.businessDurationBetween(landed, to), from + " to " + to);
                } else {
                    assertEquals(to, landed, from + " to " + to);
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void anOpeningTimeReachedFromAnEarlierDayStopsAtThePreviousClose(UnaryOperator<HolidayCalendar> implementation) {
        var office = implementation.apply(OFFICE);
        var from = WEDNESDAY.atTime(16, 0);
        var opening = CHRISTMAS_EVE.atTime(9, 0);

        assertEquals(WEDNESDAY.atTime(17, 30), office.addBusinessHours(from, office.businessDurationBetween(from, opening)));
        assertEquals(SATURDAY.atTime(10, 0),
                office.addBusinessHours(SATURDAY.atTime(10, 0), office.businessDurationBetween(SATURDAY.atTime(10, 0), MONDAY.atTime(9, 0))));
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void allDayCountsTwentyFourHours(UnaryOperator<HolidayCalendar> implementation) {
        var allDay = implementation.apply(ALL_DAY);
        var friday = LocalDate.of(2026, 12, 18);

        assertEquals(WEDNESDAY.atTime(1, 0), allDay.addBusinessHours(TUESDAY.atTime(23, 0), Duration.ofHours(2)));
        assertEquals(WEDNESDAY.atStartOfDay(), allDay.addBusinessHours(TUESDAY.atStartOfDay(), Duration.ofHours(24)));
        assertEquals(Duration.ofHours(24), allDay.businessDurationBetween(TUESDAY.atStartOfDay(), WEDNESDAY.atStartOfDay()));
        assertEquals(Duration.ofHours(48), allDay.businessDurationBetween(TUESDAY.atStartOfDay(), CHRISTMAS_EVE.atStartOfDay()));
        assertEquals(Duration.ofNanos(1), allDay.businessDurationBetween(WEDNESDAY.atTime(LocalTime.MAX), CHRISTMAS_EVE.atStartOfDay()));
        assertEquals(friday.plusDays(1).atStartOfDay(), allDay.addBusinessHours(friday.atTime(23, 0), Duration.ofHours(1)));
        assertEquals(TUESDAY.atTime(23, 0), allDay.addBusinessHours(WEDNESDAY.atStartOfDay(), Duration.ofHours(-1)));
        assertEquals(friday.atTime(23, 0), allDay.addBusinessHours(TUESDAY.minusDays(1).atStartOfDay(), Duration.ofHours(-1)));
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void allDayHalfDayRunsFromMidnightToItsClose(UnaryOperator<HolidayCalendar> implementation) {
        var allDay = implementation.apply(ALL_DAY);

        assertEquals(CHRISTMAS_EVE.atTime(13, 0), allDay.addBusinessHours(WEDNESDAY.atTime(12, 0), Duration.ofHours(25)));
        assertEquals(MONDAY.atTime(0, 1), allDay.addBusinessHours(WEDNESDAY.atTime(12, 0), Duration.ofHours(25).plusMinutes(1)));
        assertEquals(CHRISTMAS_EVE.atTime(12, 0), allDay.addBusinessHours(MONDAY.atStartOfDay(), Duration.ofHours(-1)));
        assertEquals(Duration.ofHours(13), allDay.businessDurationBetween(CHRISTMAS_EVE.atStartOfDay(), MONDAY.atStartOfDay()));
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void halfDayClosingBeforeOpeningHoldsNoTime(UnaryOperator<HolidayCalendar> implementation) {
        var closedEve = implementation.apply(HolidayCalendar.builder()
                .businessHours(NINE, HALF_PAST_FIVE)
                .layer(Layer.named("x").add(Holiday.fixed("Eve", Month.DECEMBER, 24).closingAt(LocalTime.of(8, 0))).build())
                .build());

        assertEquals(CHRISTMAS.atTime(11, 30), closedEve.addBusinessHours(WEDNESDAY.atTime(16, 0), Duration.ofHours(4)));
        assertEquals(WEDNESDAY.atTime(16, 0), closedEve.addBusinessHours(CHRISTMAS.atTime(11, 30), Duration.ofHours(-4)));
        assertEquals(Duration.ZERO, closedEve.businessDurationBetween(CHRISTMAS_EVE.atStartOfDay(), CHRISTMAS.atStartOfDay()));
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void unionUsesTheHoursEveryMemberIsOpen(UnaryOperator<HolidayCalendar> implementation) {
        var late = HolidayCalendar.builder().businessHours(LocalTime.of(10, 0), LocalTime.of(18, 0)).build();
        var union = implementation.apply(HolidayCalendar.union(OFFICE, late));

        assertEquals(CHRISTMAS_EVE.atTime(12, 30), union.addBusinessHours(WEDNESDAY.atTime(16, 0), Duration.ofHours(4)));
        assertEquals(Duration.ofHours(4), union.businessDurationBetween(WEDNESDAY.atTime(16, 0), CHRISTMAS_EVE.atTime(12, 30)));
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void crossesYears(UnaryOperator<HolidayCalendar> implementation) {
        var office = implementation.apply(OFFICE);
        var newYearsEve = LocalDate.of(2026, 12, 31);
        var newYear = LocalDate.of(2027, 1, 1);

        assertEquals(newYear.atTime(10, 0), office.addBusinessHours(newYearsEve.atTime(11, 0), Duration.ofHours(2)));
        assertEquals(newYearsEve.atTime(11, 0), office.addBusinessHours(newYear.atTime(10, 0), Duration.ofHours(-2)));
        assertEquals(Duration.ofHours(2), office.businessDurationBetween(newYearsEve.atTime(11, 0), newYear.atTime(10, 0)));
        assertEquals(Duration.ofHours(-2), office.businessDurationBetween(newYear.atTime(10, 0), newYearsEve.atTime(11, 0)));
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void givesUpWhenBusinessIsNeverOpen(UnaryOperator<HolidayCalendar> implementation) {
        var never = implementation.apply(NEVER);
        var start = WEDNESDAY.atTime(10, 0);

        assertThrows(IllegalStateException.class, () -> never.addBusinessHours(start, Duration.ofHours(1)));
        assertThrows(IllegalStateException.class, () -> never.addBusinessHours(start, Duration.ofHours(-1)));
        assertEquals(start, never.addBusinessHours(start, Duration.ZERO));
        assertEquals(Duration.ZERO, never.businessDurationBetween(start, start.plusYears(2)));
        assertEquals(Duration.ZERO, never.businessDurationBetween(start.plusYears(2), start));
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void givesUpWhenEveryBusinessDayIsAClosedHalfDay(UnaryOperator<HolidayCalendar> implementation) {
        var holidays = new ArrayList<Holiday>();

        for (var day = LocalDate.of(2024, 1, 1); day.getYear() == 2024; day = day.plusDays(1)) {
            holidays.add(Holiday.fixed("Closed " + day, day.getMonth(), day.getDayOfMonth()).closingAt(LocalTime.of(8, 0)));
        }

        var closed = implementation.apply(HolidayCalendar.builder()
                .weekend(Weekend.none())
                .businessHours(NINE, HALF_PAST_FIVE)
                .layer(Layer.named("closed").add(holidays.toArray(Holiday[]::new)).build())
                .build());
        var start = WEDNESDAY.atTime(10, 0);

        assertThrows(IllegalStateException.class, () -> closed.addBusinessHours(start, Duration.ofHours(1)));
        assertThrows(IllegalStateException.class, () -> closed.addBusinessHours(start, Duration.ofHours(-1)));
        assertEquals(Duration.ZERO, closed.businessDurationBetween(start, start.plusYears(1)));
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void rejectsDurationsBeyondALongOfNanoseconds(UnaryOperator<HolidayCalendar> implementation) {
        var office = implementation.apply(OFFICE);
        var start = WEDNESDAY.atTime(10, 0);

        assertThrows(ArithmeticException.class, () -> office.addBusinessHours(start, Duration.ofDays(200_000)));
        assertThrows(ArithmeticException.class, () -> office.addBusinessHours(start, Duration.ofNanos(Long.MIN_VALUE)));
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void rejectsNulls(UnaryOperator<HolidayCalendar> implementation) {
        var office = implementation.apply(OFFICE);
        var start = WEDNESDAY.atTime(10, 0);

        assertThrows(NullPointerException.class, () -> office.addBusinessHours(null, Duration.ZERO));
        assertThrows(NullPointerException.class, () -> office.addBusinessHours(start, null));
        assertThrows(NullPointerException.class, () -> office.businessDurationBetween(null, start));
        assertThrows(NullPointerException.class, () -> office.businessDurationBetween(start, null));
    }

    private static HolidayCalendar withDefaultMethods(HolidayCalendar target) {
        return (HolidayCalendar) Proxy.newProxyInstance(HolidayCalendar.class.getClassLoader(),
                new Class<?>[] {HolidayCalendar.class},
                (proxy, method, args) -> {
                    if (method.isDefault()) {
                        return InvocationHandler.invokeDefault(proxy, method, args);
                    }

                    try {
                        return method.invoke(target, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }
}
