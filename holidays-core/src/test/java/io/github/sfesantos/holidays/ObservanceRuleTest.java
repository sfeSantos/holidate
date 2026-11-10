package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.github.sfesantos.holidays.internal.ObservanceChain;

class ObservanceRuleTest {

    private static final LocalDate SATURDAY = LocalDate.of(2021, 12, 25);
    private static final LocalDate SUNDAY = LocalDate.of(2021, 12, 26);
    private static final LocalDate MONDAY = LocalDate.of(2021, 12, 27);
    private static final LocalDate TUESDAY = LocalDate.of(2021, 12, 28);

    private record Context(Set<DayOfWeek> weekend, Set<LocalDate> taken) implements ObservanceContext {

        static Context standard() {
            return new Context(EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY), Set.of());
        }

        Context taking(LocalDate... dates) {
            return new Context(weekend, Set.of(dates));
        }

        @Override
        public boolean isWeekend(LocalDate date) {
            return weekend.contains(date.getDayOfWeek());
        }

        @Override
        public boolean isHoliday(LocalDate date) {
            return taken.contains(date);
        }
    }

    private static LocalDate observe(ObservanceRule rule, LocalDate date, ObservanceContext context) {
        return ObservanceChain.observe(rule, date, context);
    }

    @Test
    void noneLeavesTheDateAlone() {
        assertEquals(SUNDAY, observe(ObservanceRule.none(), SUNDAY, Context.standard()));
    }

    @Test
    void sundayToMondayMovesOnlySundays() {
        assertEquals(MONDAY, observe(ObservanceRule.sundayToMonday(), SUNDAY, Context.standard()));
        assertEquals(SATURDAY, observe(ObservanceRule.sundayToMonday(), SATURDAY, Context.standard()));
    }

    @Test
    void weekendToMondayMovesBothWeekendDays() {
        assertEquals(MONDAY, observe(ObservanceRule.weekendToMonday(), SATURDAY, Context.standard()));
        assertEquals(MONDAY, observe(ObservanceRule.weekendToMonday(), SUNDAY, Context.standard()));
    }

    @Test
    void weekendToMondayChainsPastAnotherHoliday() {
        var boxingDayContext = Context.standard().taking(MONDAY);

        assertEquals(TUESDAY, observe(ObservanceRule.weekendToMonday(), SUNDAY, boxingDayContext));
    }

    @Test
    void saturdayToFridaySundayToMonday() {
        var rule = ObservanceRule.saturdayToFridaySundayToMonday();

        assertEquals(LocalDate.of(2020, 7, 3), observe(rule, LocalDate.of(2020, 7, 4), Context.standard()));
        assertEquals(LocalDate.of(2021, 7, 5), observe(rule, LocalDate.of(2021, 7, 4), Context.standard()));
        assertEquals(LocalDate.of(2022, 7, 4), observe(rule, LocalDate.of(2022, 7, 4), Context.standard()));
    }

    @Test
    void nearestWeekdayHonoursTheWeekendSet() {
        var gulf = new Context(EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY), Set.of());
        var friday = LocalDate.of(2021, 12, 24);

        assertEquals(LocalDate.of(2021, 12, 23), observe(ObservanceRule.nearestWeekday(), friday, gulf));
        assertEquals(SUNDAY, observe(ObservanceRule.nearestWeekday(), SATURDAY, gulf));
    }

    @Test
    void nearestWeekdaySkipsTakenDays() {
        var context = Context.standard().taking(LocalDate.of(2021, 12, 24));

        assertEquals(LocalDate.of(2021, 12, 23), observe(ObservanceRule.nearestWeekday(), SATURDAY, context));
    }

    @Test
    void customAppliesTheOperator() {
        var rule = ObservanceRule.custom(date -> date.getDayOfWeek() == DayOfWeek.SATURDAY ? date.plusDays(2) : date);

        assertEquals(MONDAY, observe(rule, SATURDAY, Context.standard()));
    }

    @Test
    void ruleThatNeverSettlesFailsAfterSevenSteps() {
        var runaway = ObservanceRule.custom(date -> date.plusDays(1));

        assertThrows(IllegalStateException.class, () -> observe(runaway, SATURDAY, Context.standard()));
    }

    @Test
    void namedGivesTheBuiltInRules() {
        assertSame(ObservanceRule.none(), ObservanceRule.named("NONE"));
        assertSame(ObservanceRule.sundayToMonday(), ObservanceRule.named("SUNDAY_TO_MONDAY"));
        assertSame(ObservanceRule.weekendToMonday(), ObservanceRule.named("WEEKEND_TO_MONDAY"));
        assertSame(ObservanceRule.saturdayToFridaySundayToMonday(), ObservanceRule.named("SATURDAY_TO_FRIDAY_SUNDAY_TO_MONDAY"));
        assertSame(ObservanceRule.nearestWeekday(), ObservanceRule.named("NEAREST_WEEKDAY"));
        assertSame(ObservanceRule.inherit(), ObservanceRule.named("INHERIT"));
    }

    @Test
    void namedRejectsUnknownNames() {
        var error = assertThrows(IllegalArgumentException.class, () -> ObservanceRule.named("sunday"));

        assertTrue(error.getMessage().contains("\"sunday\""));
        assertTrue(error.getMessage().contains("SUNDAY_TO_MONDAY"));
        assertThrows(NullPointerException.class, () -> ObservanceRule.named(null));
    }

    @Test
    void inheritCannotBeAppliedDirectly() {
        assertThrows(UnsupportedOperationException.class, () -> observe(ObservanceRule.inherit(), SATURDAY, Context.standard()));
    }
}
