package io.github.sfesantos.holidays;

import static io.github.sfesantos.holidays.BusinessDayConvention.FOLLOWING;
import static io.github.sfesantos.holidays.BusinessDayConvention.MODIFIED_FOLLOWING;
import static io.github.sfesantos.holidays.BusinessDayConvention.MODIFIED_PRECEDING;
import static io.github.sfesantos.holidays.BusinessDayConvention.PRECEDING;
import static io.github.sfesantos.holidays.BusinessDayConvention.UNADJUSTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class BusinessDayConventionTest {

    private static final HolidayCalendar SPAIN = HolidayCalendar.builder()
            .layer(Layer.named("ES")
                    .add(Holiday.fixed("Año Nuevo", Month.JANUARY, 1),
                            Holiday.fixed("Fiesta del Trabajo", Month.MAY, 1),
                            Holiday.fixed("Asunción", Month.AUGUST, 15),
                            Holiday.fixed("Fiesta Nacional", Month.OCTOBER, 12),
                            Holiday.fixed("Todos los Santos", Month.NOVEMBER, 1),
                            Holiday.fixed("Navidad", Month.DECEMBER, 25))
                    .build())
            .build();

    private static final LocalDate CHRISTMAS_FRIDAY = LocalDate.of(2026, 12, 25);
    private static final LocalDate ASSUMPTION_SATURDAY = LocalDate.of(2026, 8, 15);

    @ParameterizedTest
    @EnumSource(BusinessDayConvention.class)
    void businessDaysNeverMove(BusinessDayConvention convention) {
        var wednesday = LocalDate.of(2026, 3, 11);

        assertEquals(wednesday, SPAIN.adjust(wednesday, convention));
    }

    @Test
    void unadjustedKeepsTheDate() {
        assertEquals(CHRISTMAS_FRIDAY, SPAIN.adjust(CHRISTMAS_FRIDAY, UNADJUSTED));
    }

    @Test
    void followingAndPrecedingSkipHolidaysAndWeekends() {
        assertEquals(LocalDate.of(2026, 12, 28), SPAIN.adjust(CHRISTMAS_FRIDAY, FOLLOWING));
        assertEquals(LocalDate.of(2026, 12, 24), SPAIN.adjust(CHRISTMAS_FRIDAY, PRECEDING));
        assertEquals(LocalDate.of(2026, 8, 17), SPAIN.adjust(ASSUMPTION_SATURDAY, FOLLOWING));
        assertEquals(LocalDate.of(2026, 8, 14), SPAIN.adjust(ASSUMPTION_SATURDAY, PRECEDING));
    }

    @Test
    void sundayFollowedByAMondayHolidayMovesToTuesday() {
        var sunday = LocalDate.of(2026, 10, 11);

        assertEquals(LocalDate.of(2026, 10, 13), SPAIN.adjust(sunday, FOLLOWING));
        assertEquals(LocalDate.of(2026, 10, 13), SPAIN.adjust(sunday, MODIFIED_FOLLOWING));
        assertEquals(LocalDate.of(2026, 10, 9), SPAIN.adjust(sunday, PRECEDING));
    }

    @Test
    void modifiedFollowingStaysInTheMonth() {
        var lastSundayOfMay = LocalDate.of(2026, 5, 31);

        assertEquals(LocalDate.of(2026, 6, 1), SPAIN.adjust(lastSundayOfMay, FOLLOWING));
        assertEquals(LocalDate.of(2026, 5, 29), SPAIN.adjust(lastSundayOfMay, MODIFIED_FOLLOWING));
        assertEquals(LocalDate.of(2026, 8, 17), SPAIN.adjust(ASSUMPTION_SATURDAY, MODIFIED_FOLLOWING));
    }

    @Test
    void modifiedPrecedingStaysInTheMonth() {
        var allSaintsSunday = LocalDate.of(2026, 11, 1);

        assertEquals(LocalDate.of(2026, 10, 30), SPAIN.adjust(allSaintsSunday, PRECEDING));
        assertEquals(LocalDate.of(2026, 11, 2), SPAIN.adjust(allSaintsSunday, MODIFIED_PRECEDING));
        assertEquals(LocalDate.of(2026, 8, 14), SPAIN.adjust(ASSUMPTION_SATURDAY, MODIFIED_PRECEDING));
    }

    @Test
    void conventionCanBeAppliedDirectly() {
        assertEquals(SPAIN.adjust(CHRISTMAS_FRIDAY, FOLLOWING), FOLLOWING.adjust(CHRISTMAS_FRIDAY, SPAIN));
    }

    @Test
    void adjusterKeepsTheTime() {
        var debit = LocalDateTime.of(2026, 8, 15, 9, 0);

        assertEquals(LocalDateTime.of(2026, 8, 17, 9, 0), debit.with(SPAIN.adjuster(FOLLOWING)));
        assertEquals(LocalDate.of(2026, 8, 14), ASSUMPTION_SATURDAY.with(SPAIN.adjuster(PRECEDING)));
    }

    @Test
    void worksOnAUnion() {
        var brazil = HolidayCalendar.builder()
                .layer(Layer.named("BR").add(Holiday.fixed("Finados", Month.NOVEMBER, 2)).build())
                .build();
        var union = HolidayCalendar.union(SPAIN, brazil);

        assertEquals(LocalDate.of(2026, 11, 3), union.adjust(LocalDate.of(2026, 11, 1), FOLLOWING));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> SPAIN.adjust(null, FOLLOWING));
        assertThrows(NullPointerException.class, () -> SPAIN.adjust(CHRISTMAS_FRIDAY, null));
    }
}
