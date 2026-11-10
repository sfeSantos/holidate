package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class BusinessDayMonthTest {

    private static final HolidayCalendar SPAIN = HolidayCalendar.builder()
            .layer(Layer.named("ES")
                    .add(Holiday.fixed("Año Nuevo", Month.JANUARY, 1),
                            Holiday.fixed("Epifanía", Month.JANUARY, 6),
                            Holiday.easterRelative("Viernes Santo", -2),
                            Holiday.fixed("Fiesta del Trabajo", Month.MAY, 1),
                            Holiday.fixed("Constitución", Month.DECEMBER, 6),
                            Holiday.fixed("Inmaculada", Month.DECEMBER, 8),
                            Holiday.fixed("Navidad", Month.DECEMBER, 25))
                    .build())
            .build();

    private static final HolidayCalendar BRAZIL = HolidayCalendar.builder()
            .layer(Layer.named("BR")
                    .add(Holiday.fixed("Confraternização Universal", Month.JANUARY, 1),
                            Holiday.fixed("Tiradentes", Month.APRIL, 21),
                            Holiday.fixed("Natal", Month.DECEMBER, 25))
                    .build())
            .build();

    private static final HolidayCalendar NEVER = HolidayCalendar.builder()
            .weekend(DayOfWeek.values())
            .build();

    private static final YearMonth JANUARY_2026 = YearMonth.of(2026, 1);

    @Test
    void firstLastAndCountOfAMonth() {
        assertEquals(Optional.of(LocalDate.of(2026, 1, 2)), SPAIN.firstBusinessDayOfMonth(JANUARY_2026));
        assertEquals(Optional.of(LocalDate.of(2026, 1, 30)), SPAIN.lastBusinessDayOfMonth(JANUARY_2026));
        assertEquals(20, SPAIN.businessDaysInMonth(JANUARY_2026));
        assertEquals(Optional.of(LocalDate.of(2026, 1, 9)), SPAIN.nthBusinessDayOfMonth(JANUARY_2026, 5));
        assertEquals(Optional.of(LocalDate.of(2026, 1, 29)), SPAIN.nthBusinessDayOfMonth(JANUARY_2026, -2));
    }

    @Test
    void everyPositionMatchesADayByDayScan() {
        assertMatchesScan(SPAIN, SPAIN, 2026);
        assertMatchesScan(SPAIN, SPAIN, 2028);
    }

    @Test
    void minusOneIsTheLastAndOneIsTheFirst() {
        for (var month = JANUARY_2026; month.getYear() == 2026; month = month.plusMonths(1)) {
            assertEquals(SPAIN.lastBusinessDayOfMonth(month), SPAIN.nthBusinessDayOfMonth(month, -1), month.toString());
            assertEquals(SPAIN.firstBusinessDayOfMonth(month), SPAIN.nthBusinessDayOfMonth(month, 1), month.toString());
        }
    }

    @Test
    void monthCountsAddUpToTheYear() {
        long total = 0;

        for (var month = JANUARY_2026; month.getYear() == 2026; month = month.plusMonths(1)) {
            total += SPAIN.businessDaysInMonth(month);
        }

        assertEquals(SPAIN.businessDaysBetween(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1)), total);
    }

    @Test
    void positionsOutsideTheMonthAreEmpty() {
        assertEquals(Optional.empty(), SPAIN.nthBusinessDayOfMonth(JANUARY_2026, 21));
        assertEquals(Optional.empty(), SPAIN.nthBusinessDayOfMonth(JANUARY_2026, -21));
        assertEquals(Optional.empty(), SPAIN.nthBusinessDayOfMonth(JANUARY_2026, Integer.MAX_VALUE));
        assertEquals(Optional.empty(), SPAIN.nthBusinessDayOfMonth(JANUARY_2026, Integer.MIN_VALUE));
    }

    @Test
    void monthWithoutBusinessDaysIsEmpty() {
        assertEquals(0, NEVER.businessDaysInMonth(JANUARY_2026));
        assertEquals(Optional.empty(), NEVER.firstBusinessDayOfMonth(JANUARY_2026));
        assertEquals(Optional.empty(), NEVER.lastBusinessDayOfMonth(JANUARY_2026));
        assertEquals(Optional.empty(), NEVER.nthBusinessDayOfMonth(JANUARY_2026, 1));
        assertEquals(Optional.empty(), NEVER.nthBusinessDayOfMonth(JANUARY_2026, -1));
    }

    @Test
    void unionCountsDaysOpenInEveryMember() {
        var union = HolidayCalendar.union(SPAIN, BRAZIL);
        var april = YearMonth.of(2026, 4);

        assertEquals(SPAIN.businessDaysInMonth(april) - 1, union.businessDaysInMonth(april));
        assertMatchesScan(union, union, 2026);
    }

    @Test
    void adjustersMoveWithinTheMonthAndKeepTheTime() {
        var midJanuary = LocalDate.of(2026, 1, 15);

        assertEquals(LocalDate.of(2026, 1, 2), midJanuary.with(SPAIN.firstBusinessDayOfMonthAdjuster()));
        assertEquals(LocalDate.of(2026, 1, 30), midJanuary.with(SPAIN.lastBusinessDayOfMonthAdjuster()));
        assertEquals(LocalDate.of(2026, 1, 9), midJanuary.with(SPAIN.nthBusinessDayOfMonthAdjuster(5)));
        assertEquals(LocalDate.of(2026, 1, 9).atTime(18, 45),
                midJanuary.atTime(18, 45).with(SPAIN.nthBusinessDayOfMonthAdjuster(5)));
        assertSame(SPAIN.firstBusinessDayOfMonthAdjuster(), SPAIN.firstBusinessDayOfMonthAdjuster());
        assertSame(SPAIN.lastBusinessDayOfMonthAdjuster(), SPAIN.lastBusinessDayOfMonthAdjuster());
    }

    @Test
    void adjustersFailWhereTheQueryIsEmpty() {
        var date = LocalDate.of(2026, 1, 15);

        assertThrows(DateTimeException.class, () -> date.with(SPAIN.nthBusinessDayOfMonthAdjuster(21)));
        assertThrows(DateTimeException.class, () -> date.with(NEVER.firstBusinessDayOfMonthAdjuster()));
        assertThrows(DateTimeException.class, () -> date.with(NEVER.lastBusinessDayOfMonthAdjuster()));
    }

    @Test
    void rejectsZeroAndNull() {
        assertThrows(IllegalArgumentException.class, () -> SPAIN.nthBusinessDayOfMonth(JANUARY_2026, 0));
        assertThrows(IllegalArgumentException.class, () -> SPAIN.nthBusinessDayOfMonthAdjuster(0));
        assertThrows(NullPointerException.class, () -> SPAIN.businessDaysInMonth(null));
        assertThrows(NullPointerException.class, () -> SPAIN.firstBusinessDayOfMonth(null));
        assertThrows(NullPointerException.class, () -> SPAIN.lastBusinessDayOfMonth(null));
        assertThrows(NullPointerException.class, () -> SPAIN.nthBusinessDayOfMonth(null, 1));
    }

    @Test
    void defaultMethodsAgreeWithTheIndex() {
        var foreign = withDefaultMethods(SPAIN);
        var never = withDefaultMethods(NEVER);
        var date = LocalDate.of(2026, 1, 15);

        assertMatchesScan(foreign, SPAIN, 2026);
        assertMatchesScan(foreign, SPAIN, 2028);
        assertEquals(Optional.empty(), foreign.nthBusinessDayOfMonth(JANUARY_2026, Integer.MIN_VALUE));
        assertEquals(0, never.businessDaysInMonth(JANUARY_2026));
        assertEquals(Optional.empty(), never.firstBusinessDayOfMonth(JANUARY_2026));
        assertEquals(Optional.empty(), never.lastBusinessDayOfMonth(JANUARY_2026));
        assertEquals(LocalDate.of(2026, 1, 2), date.with(foreign.firstBusinessDayOfMonthAdjuster()));
        assertEquals(LocalDate.of(2026, 1, 30), date.with(foreign.lastBusinessDayOfMonthAdjuster()));
        assertThrows(DateTimeException.class, () -> date.with(never.lastBusinessDayOfMonthAdjuster()));
        assertThrows(IllegalArgumentException.class, () -> foreign.nthBusinessDayOfMonth(JANUARY_2026, 0));
        assertThrows(IllegalArgumentException.class, () -> foreign.nthBusinessDayOfMonthAdjuster(0));
        assertThrows(NullPointerException.class, () -> foreign.businessDaysInMonth(null));
        assertThrows(NullPointerException.class, () -> foreign.nthBusinessDayOfMonth(null, 1));
    }

    /** Checks every month of the year on {@code calendar} against the business days {@code reference} reports day by day. */
    private static void assertMatchesScan(HolidayCalendar calendar, HolidayCalendar reference, int year) {
        for (var month = YearMonth.of(year, 1); month.getYear() == year; month = month.plusMonths(1)) {
            var expected = businessDaysOf(reference, month);
            int count = expected.size();

            assertEquals(count, calendar.businessDaysInMonth(month), month.toString());
            assertEquals(Optional.of(expected.getFirst()), calendar.firstBusinessDayOfMonth(month), month.toString());
            assertEquals(Optional.of(expected.getLast()), calendar.lastBusinessDayOfMonth(month), month.toString());
            assertEquals(Optional.empty(), calendar.nthBusinessDayOfMonth(month, count + 1), month.toString());
            assertEquals(Optional.empty(), calendar.nthBusinessDayOfMonth(month, -count - 1), month.toString());

            for (int n = 1; n <= count; n++) {
                assertEquals(Optional.of(expected.get(n - 1)), calendar.nthBusinessDayOfMonth(month, n), month + " " + n);
                assertEquals(Optional.of(expected.get(count - n)), calendar.nthBusinessDayOfMonth(month, -n), month + " -" + n);
            }
        }
    }

    private static List<LocalDate> businessDaysOf(HolidayCalendar calendar, YearMonth month) {
        var days = new ArrayList<LocalDate>();

        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            if (calendar.isBusinessDay(month.atDay(day))) {
                days.add(month.atDay(day));
            }
        }

        return days;
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
