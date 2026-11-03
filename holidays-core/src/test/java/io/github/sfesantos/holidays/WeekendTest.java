package io.github.sfesantos.holidays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.util.EnumSet;

import org.junit.jupiter.api.Test;

class WeekendTest {

    @Test
    void standardIsSaturdayAndSunday() {
        assertEquals(EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY), Weekend.standard().days());
    }

    @Test
    void gulfWeekend() {
        var gulf = Weekend.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);

        assertTrue(gulf.contains(DayOfWeek.FRIDAY));
        assertTrue(gulf.contains(DayOfWeek.SATURDAY));
        assertFalse(gulf.contains(DayOfWeek.SUNDAY));
    }

    @Test
    void singleDayAndEmpty() {
        assertEquals(EnumSet.of(DayOfWeek.SUNDAY), Weekend.of(DayOfWeek.SUNDAY).days());
        assertTrue(Weekend.none().isEmpty());
        assertTrue(Weekend.of().isEmpty());
    }

    @Test
    void sameDaysGiveEqualValues() {
        assertEquals(Weekend.standard(), Weekend.of(EnumSet.of(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY)));
    }

    @Test
    void rejectsBitsOutsideTheWeek() {
        assertThrows(IllegalArgumentException.class, () -> new Weekend(1 << 7));
    }
}
