package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.easter.EasterAlgorithm;

class HolidayRuleTest {

    private static final EasterAlgorithm EASTER = EasterAlgorithm.gregorian();

    private static Optional<LocalDate> resolve(HolidayRule rule, int year) {
        return rule.dateIn(Year.of(year), EASTER);
    }

    @Nested
    class FixedDate {

        @Test
        void sameDayEveryYear() {
            var christmas = HolidayRule.fixed(Month.DECEMBER, 25);

            assertEquals(Optional.of(LocalDate.of(2026, 12, 25)), resolve(christmas, 2026));
            assertEquals(Optional.of(LocalDate.of(2027, 12, 25)), resolve(christmas, 2027));
        }

        @Test
        void leapDayOnlyOccursInLeapYears() {
            var leapDay = HolidayRule.fixed(MonthDay.of(2, 29));

            assertEquals(Optional.of(LocalDate.of(2028, 2, 29)), resolve(leapDay, 2028));
            assertTrue(resolve(leapDay, 2027).isEmpty());
        }

        @Test
        void rejectsInvalidDay() {
            assertThrows(java.time.DateTimeException.class, () -> HolidayRule.fixed(Month.FEBRUARY, 30));
        }
    }

    @Nested
    class EasterRelative {

        @Test
        void goodFridayIsTwoDaysBeforeEaster() {
            assertEquals(Optional.of(LocalDate.of(2026, 4, 3)), resolve(HolidayRule.easterRelative(-2), 2026));
        }

        @Test
        void corpusChristiIsSixtyDaysAfterEaster() {
            assertEquals(Optional.of(LocalDate.of(2026, 6, 4)), resolve(HolidayRule.easterRelative(60), 2026));
        }

        @Test
        void rejectsAbsurdOffsets() {
            assertThrows(IllegalArgumentException.class, () -> HolidayRule.easterRelative(400));
        }
    }

    @Nested
    class NthWeekdayOfMonth {

        @Test
        void fourthThursdayOfNovember() {
            var thanksgiving = HolidayRule.nthWeekdayOfMonth(4, DayOfWeek.THURSDAY, Month.NOVEMBER);

            assertEquals(Optional.of(LocalDate.of(2026, 11, 26)), resolve(thanksgiving, 2026));
        }

        @Test
        void negativeCountsFromTheEndOfTheMonth() {
            var lastMondayOfMay = HolidayRule.nthWeekdayOfMonth(-1, DayOfWeek.MONDAY, Month.MAY);

            assertEquals(Optional.of(LocalDate.of(2026, 5, 25)), resolve(lastMondayOfMay, 2026));
        }

        @Test
        void fifthOccurrenceIsEmptyWhenTheMonthHasOnlyFour() {
            var fifthMonday = HolidayRule.nthWeekdayOfMonth(5, DayOfWeek.MONDAY, Month.FEBRUARY);

            assertTrue(resolve(fifthMonday, 2026).isEmpty());
            assertEquals(Optional.of(LocalDate.of(2027, 3, 29)),
                    resolve(HolidayRule.nthWeekdayOfMonth(5, DayOfWeek.MONDAY, Month.MARCH), 2027));
        }

        @Test
        void rejectsZeroAndOutOfRange() {
            assertThrows(IllegalArgumentException.class,
                    () -> HolidayRule.nthWeekdayOfMonth(0, DayOfWeek.MONDAY, Month.MAY));
            assertThrows(IllegalArgumentException.class,
                    () -> HolidayRule.nthWeekdayOfMonth(6, DayOfWeek.MONDAY, Month.MAY));
        }
    }

    @Nested
    class WeekdayAfterAndBefore {

        @Test
        void afterIsStrict() {
            var rule = HolidayRule.weekdayAfter(DayOfWeek.MONDAY, MonthDay.of(5, 4));

            assertEquals(Optional.of(LocalDate.of(2026, 5, 11)), resolve(rule, 2026));
        }

        @Test
        void beforeIsStrict() {
            var rule = HolidayRule.weekdayBefore(DayOfWeek.MONDAY, MonthDay.of(5, 4));

            assertEquals(Optional.of(LocalDate.of(2026, 4, 27)), resolve(rule, 2026));
        }

        @Test
        void leapDayBaseClampsToTwentyEighthInNonLeapYears() {
            var rule = HolidayRule.weekdayAfter(DayOfWeek.SUNDAY, MonthDay.of(2, 29));

            assertEquals(Optional.of(LocalDate.of(2027, 3, 7)), resolve(rule, 2027));
        }
    }

    @Nested
    class Custom {

        @Test
        void delegatesToTheFunction() {
            var rule = HolidayRule.custom(year -> Optional.of(year.atDay(100)));

            assertEquals(Optional.of(LocalDate.of(2026, 4, 10)), resolve(rule, 2026));
        }

        @Test
        void rejectsNullResult() {
            var rule = HolidayRule.custom(year -> null);

            assertThrows(NullPointerException.class, () -> resolve(rule, 2026));
        }
    }
}
