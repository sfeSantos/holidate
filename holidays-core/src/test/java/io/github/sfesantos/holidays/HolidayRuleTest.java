package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.easter.EasterAlgorithm;
import io.github.sfesantos.holidays.hebrew.HebrewMonth;

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

    @Nested
    class Hijri {

        @Test
        void islamicNewYearFellTwiceIn2008() {
            var newYear = HolidayRule.hijri(1, 1);

            assertEquals(List.of(LocalDate.of(2008, 1, 10), LocalDate.of(2008, 12, 29)), newYear.datesIn(Year.of(2008), EASTER));
            assertEquals(Optional.of(LocalDate.of(2008, 1, 10)), resolve(newYear, 2008));
            assertEquals(List.of(LocalDate.of(2026, 6, 16)), newYear.datesIn(Year.of(2026), EASTER));
        }

        @Test
        void matchesTheUmmAlQuraTablesFor2026() {
            assertEquals(Optional.of(LocalDate.of(2026, 2, 18)), resolve(HolidayRule.hijri(9, 1), 2026));
            assertEquals(Optional.of(LocalDate.of(2026, 3, 20)), resolve(HolidayRule.hijri(10, 1), 2026));
            assertEquals(Optional.of(LocalDate.of(2026, 5, 27)), resolve(HolidayRule.hijri(12, 10), 2026));
        }

        @Test
        void thirtiethDayOnlyExistsWhenTheMonthHasThirtyDays() {
            var lastOfRamadan = HolidayRule.hijri(9, 30);

            assertTrue(resolve(lastOfRamadan, 2025).isEmpty());
            assertEquals(Optional.of(LocalDate.of(2026, 3, 19)), resolve(lastOfRamadan, 2026));
        }

        @Test
        void yearsOutsideTheTablesYieldNothing() {
            assertTrue(resolve(HolidayRule.hijri(1, 1), 1800).isEmpty());
            assertTrue(resolve(HolidayRule.hijri(1, 1), 2200).isEmpty());
        }

        @Test
        void validatesMonthAndDay() {
            assertThrows(IllegalArgumentException.class, () -> HolidayRule.hijri(0, 1));
            assertThrows(IllegalArgumentException.class, () -> HolidayRule.hijri(13, 1));
            assertThrows(IllegalArgumentException.class, () -> HolidayRule.hijri(1, 0));
            assertThrows(IllegalArgumentException.class, () -> HolidayRule.hijri(1, 31));
        }
    }

    @Nested
    class Hebrew {

        @Test
        void tenthOfTevetFellTwiceIn2023() {
            var fast = HolidayRule.hebrew(HebrewMonth.TEVET, 10);

            assertEquals(List.of(LocalDate.of(2023, 1, 3), LocalDate.of(2023, 12, 22)), fast.datesIn(Year.of(2023), EASTER));
            assertEquals(List.of(LocalDate.of(2026, 12, 20)), fast.datesIn(Year.of(2026), EASTER));
        }

        @Test
        void festivalsOf2026() {
            assertEquals(Optional.of(LocalDate.of(2026, 4, 2)), resolve(HolidayRule.hebrew(HebrewMonth.NISAN, 15), 2026));
            assertEquals(Optional.of(LocalDate.of(2026, 9, 12)), resolve(HolidayRule.hebrew(HebrewMonth.TISHRI, 1), 2026));
            assertEquals(Optional.of(LocalDate.of(2026, 9, 21)), resolve(HolidayRule.hebrew(HebrewMonth.TISHRI, 10), 2026));
            assertEquals(Optional.of(LocalDate.of(2026, 12, 5)), resolve(HolidayRule.hebrew(HebrewMonth.KISLEV, 25), 2026));
        }

        @Test
        void adarFollowsTheFestivalsIntoAdarTwoInLeapYears() {
            var purim = HolidayRule.hebrew(HebrewMonth.ADAR, 14);

            assertEquals(Optional.of(LocalDate.of(2026, 3, 3)), resolve(purim, 2026));
            assertEquals(Optional.of(LocalDate.of(2024, 3, 24)), resolve(purim, 2024));
        }

        @Test
        void adarOneOnlyExistsInLeapYears() {
            var purimKatan = HolidayRule.hebrew(HebrewMonth.ADAR_I, 14);

            assertEquals(Optional.of(LocalDate.of(2024, 2, 23)), resolve(purimKatan, 2024));
            assertTrue(resolve(purimKatan, 2026).isEmpty());
        }

        @Test
        void thirtiethDayOnlyExistsWhenTheMonthHasThirtyDays() {
            var lastOfHeshvan = HolidayRule.hebrew(HebrewMonth.HESHVAN, 30);

            assertTrue(resolve(lastOfHeshvan, 2025).isEmpty());
            assertEquals(Optional.of(LocalDate.of(2026, 11, 10)), resolve(lastOfHeshvan, 2026));
        }

        @Test
        void validatesMonthAndDay() {
            assertThrows(NullPointerException.class, () -> HolidayRule.hebrew(null, 1));
            assertThrows(IllegalArgumentException.class, () -> HolidayRule.hebrew(HebrewMonth.NISAN, 0));
            assertThrows(IllegalArgumentException.class, () -> HolidayRule.hebrew(HebrewMonth.NISAN, 31));
        }
    }
}
