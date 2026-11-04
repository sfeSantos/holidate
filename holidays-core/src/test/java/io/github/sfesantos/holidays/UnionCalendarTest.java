package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.internal.UnionCalendar;

class UnionCalendarTest {

    private static final HolidayCalendar SPAIN = HolidayCalendar.builder()
            .layer(Layer.named("ES")
                    .add(Holiday.fixed("Año Nuevo", Month.JANUARY, 1),
                            Holiday.fixed("Fiesta Nacional", Month.OCTOBER, 12),
                            Holiday.fixed("Inmaculada", Month.DECEMBER, 8),
                            Holiday.fixed("Navidad", Month.DECEMBER, 25))
                    .build())
            .build();

    private static final HolidayCalendar BRAZIL = HolidayCalendar.builder()
            .layer(Layer.named("BR")
                    .add(Holiday.fixed("Confraternização Universal", Month.JANUARY, 1),
                            Holiday.fixed("Nossa Senhora Aparecida", Month.OCTOBER, 12),
                            Holiday.fixed("Finados", Month.NOVEMBER, 2),
                            Holiday.fixed("Consciência Negra", Month.NOVEMBER, 20),
                            Holiday.fixed("Natal", Month.DECEMBER, 25))
                    .build())
            .build();

    private static final HolidayCalendar EUR_BRL = HolidayCalendar.union(SPAIN, BRAZIL);

    @Test
    void holidayInAnyMemberIsAHoliday() {
        assertTrue(EUR_BRL.isHoliday(LocalDate.of(2026, 12, 8)));
        assertTrue(EUR_BRL.isHoliday(LocalDate.of(2026, 11, 20)));
        assertFalse(EUR_BRL.isHoliday(LocalDate.of(2026, 11, 19)));
    }

    @Test
    void businessDayOnlyWhenEveryMemberIsOpen() {
        assertFalse(EUR_BRL.isBusinessDay(LocalDate.of(2026, 11, 2)));
        assertFalse(EUR_BRL.isBusinessDay(LocalDate.of(2026, 12, 8)));
        assertTrue(EUR_BRL.isBusinessDay(LocalDate.of(2026, 12, 9)));
    }

    @Test
    void holidaysInListsEveryMemberWithItsOwnLayer() {
        var occurrences = EUR_BRL.holidaysIn(Year.of(2026));
        var christmas = occurrences.stream()
                .filter(o -> o.original().equals(LocalDate.of(2026, 12, 25)))
                .toList();

        assertEquals(9, occurrences.size());
        assertEquals(List.of("ES", "BR"), christmas.stream().map(HolidayOccurrence::layer).toList());
        assertEquals(LocalDate.of(2026, 1, 1), occurrences.get(0).original());
        assertEquals(LocalDate.of(2026, 12, 25), occurrences.get(8).original());
    }

    @Test
    void holidayOnPrefersTheFirstMember() {
        assertEquals("Navidad", EUR_BRL.holidayOn(LocalDate.of(2026, 12, 25)).orElseThrow().name());
        assertEquals("Natal", HolidayCalendar.union(BRAZIL, SPAIN).holidayOn(LocalDate.of(2026, 12, 25)).orElseThrow().name());
        assertEquals("Finados", EUR_BRL.holidayOn(LocalDate.of(2026, 11, 2)).orElseThrow().name());
    }

    @Test
    void navigationSkipsBothMembersHolidays() {
        var monday = LocalDate.of(2026, 12, 7);

        assertEquals(LocalDate.of(2026, 12, 9), EUR_BRL.nextBusinessDay(monday));
        assertEquals(LocalDate.of(2026, 12, 8), EUR_BRL.nextHoliday(monday).orElseThrow().observed());
        assertEquals(LocalDate.of(2026, 11, 20), EUR_BRL.nextHoliday(LocalDate.of(2026, 11, 3)).orElseThrow().observed());
        assertEquals(LocalDate.of(2026, 12, 28), EUR_BRL.addBusinessDays(LocalDate.of(2026, 12, 23), 2));
    }

    @Test
    void weekendIsTheUnionOfTheMembersWeekends() {
        var gulf = HolidayCalendar.builder().weekend(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY).build();
        var union = HolidayCalendar.union(SPAIN, gulf);

        assertEquals(Set.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY), union.weekend().days());
        assertFalse(union.isBusinessDay(LocalDate.of(2026, 3, 6)));
        assertTrue(union.isBusinessDay(LocalDate.of(2026, 3, 5)));
    }

    @Test
    void unionOfUnionsWorks() {
        var gulf = HolidayCalendar.builder().weekend(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY).build();
        var nested = HolidayCalendar.union(EUR_BRL, gulf);

        assertTrue(nested.isHoliday(LocalDate.of(2026, 11, 20)));
        assertFalse(nested.isBusinessDay(LocalDate.of(2026, 3, 6)));
        assertEquals(9, nested.holidaysIn(Year.of(2026)).size());
    }

    @Test
    void foreignImplementationsAreReadThroughTheirQueries() {
        var foreign = (HolidayCalendar) Proxy.newProxyInstance(HolidayCalendar.class.getClassLoader(),
                new Class<?>[] {HolidayCalendar.class}, (proxy, method, args) -> method.invoke(BRAZIL, args));
        var union = HolidayCalendar.union(SPAIN, foreign);
        var year = Year.of(2026);

        assertEquals(EUR_BRL.holidaysIn(year), union.holidaysIn(year));

        for (var day = LocalDate.of(2026, 1, 1); day.getYear() == 2026; day = day.plusDays(1)) {
            assertEquals(EUR_BRL.isHoliday(day), union.isHoliday(day), day.toString());
            assertEquals(EUR_BRL.isBusinessDay(day), union.isBusinessDay(day), day.toString());
            assertEquals(EUR_BRL.holidayOn(day), union.holidayOn(day), day.toString());
        }
    }

    @Test
    void singleMemberIsReturnedAsIs() {
        assertSame(SPAIN, HolidayCalendar.union(SPAIN));
        assertSame(SPAIN, HolidayCalendar.union(List.of(SPAIN)));
        assertInstanceOf(UnionCalendar.class, EUR_BRL);
    }

    @Test
    void warmUpCachesTheUnionAndItsMembers() {
        var union = assertInstanceOf(UnionCalendar.class, HolidayCalendar.union(SPAIN, BRAZIL));

        union.warmUp(Year.of(2030), Year.of(2032));

        assertEquals(3, union.cachedYears());
    }

    @Test
    void rejectsEmptyAndNullMembers() {
        assertThrows(IllegalArgumentException.class, HolidayCalendar::union);
        assertThrows(IllegalArgumentException.class, () -> HolidayCalendar.union(List.of()));
        assertThrows(NullPointerException.class, () -> HolidayCalendar.union((HolidayCalendar[]) null));
        assertThrows(NullPointerException.class, () -> HolidayCalendar.union(SPAIN, null));
        assertThrows(NullPointerException.class, () -> HolidayCalendar.union((List<HolidayCalendar>) null));
    }
}
