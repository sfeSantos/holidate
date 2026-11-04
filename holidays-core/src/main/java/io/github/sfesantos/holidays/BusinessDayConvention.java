package io.github.sfesantos.holidays;

import java.time.LocalDate;
import java.util.Objects;

/**
 * What to do with a date that is not a business day, for example a direct
 * debit or a coupon that falls on a holiday or a weekend. A date that is
 * already a business day is never moved.
 */
public enum BusinessDayConvention {

    /** Keep the date as it is. */
    UNADJUSTED {
        @Override
        public LocalDate adjust(LocalDate date, BusinessDayQueries calendar) {
            return date;
        }
    },

    /** Move forward to the first business day. */
    FOLLOWING {
        @Override
        public LocalDate adjust(LocalDate date, BusinessDayQueries calendar) {
            return calendar.nextOrSameBusinessDay(date);
        }
    },

    /** Move back to the last business day. */
    PRECEDING {
        @Override
        public LocalDate adjust(LocalDate date, BusinessDayQueries calendar) {
            return calendar.previousOrSameBusinessDay(date);
        }
    },

    /** Move forward, unless that lands in the next month; then move back instead. */
    MODIFIED_FOLLOWING {
        @Override
        public LocalDate adjust(LocalDate date, BusinessDayQueries calendar) {
            var following = calendar.nextOrSameBusinessDay(date);

            return sameMonth(date, following) ? following : calendar.previousOrSameBusinessDay(date);
        }
    },

    /** Move back, unless that lands in the previous month; then move forward instead. */
    MODIFIED_PRECEDING {
        @Override
        public LocalDate adjust(LocalDate date, BusinessDayQueries calendar) {
            var preceding = calendar.previousOrSameBusinessDay(date);

            return sameMonth(date, preceding) ? preceding : calendar.nextOrSameBusinessDay(date);
        }
    };

    /** The business day this convention picks for the date in the given calendar. */
    public abstract LocalDate adjust(LocalDate date, BusinessDayQueries calendar);

    static LocalDate apply(BusinessDayConvention convention, LocalDate date, BusinessDayQueries calendar) {
        Objects.requireNonNull(convention, "convention must not be null");
        Objects.requireNonNull(date, "date must not be null");

        return convention.adjust(date, calendar);
    }

    private static boolean sameMonth(LocalDate a, LocalDate b) {
        return a.getYear() == b.getYear() && a.getMonth() == b.getMonth();
    }
}
