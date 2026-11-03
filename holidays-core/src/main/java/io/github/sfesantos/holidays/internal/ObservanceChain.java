package io.github.sfesantos.holidays.internal;

import java.time.LocalDate;

import io.github.sfesantos.holidays.ObservanceContext;
import io.github.sfesantos.holidays.ObservanceRule;

/**
 * Applies an observance rule until the date stops moving. Seven steps cover a
 * full week; a rule that still moves after that is broken and we fail loudly.
 */
public final class ObservanceChain {

    static final int MAX_STEPS = 7;

    private ObservanceChain() {}

    public static LocalDate observe(ObservanceRule rule, LocalDate original, ObservanceContext context) {
        var current = original;

        for (int step = 0; step <= MAX_STEPS; step++) {
            var next = rule.observe(current, context);

            if (next.equals(current)) {
                return current;
            }

            current = next;
        }

        throw new IllegalStateException("observance rule did not settle within " + MAX_STEPS + " steps from " + original);
    }
}
