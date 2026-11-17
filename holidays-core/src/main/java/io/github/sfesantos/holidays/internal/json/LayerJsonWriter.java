package io.github.sfesantos.holidays.internal.json;

import java.time.MonthDay;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.sfesantos.holidays.Holiday;
import io.github.sfesantos.holidays.HolidayPeriod;
import io.github.sfesantos.holidays.HolidayRule;
import io.github.sfesantos.holidays.HolidayType;
import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.ObservanceRule;
import io.github.sfesantos.holidays.Weekend;
import io.github.sfesantos.holidays.YearRange;
import io.github.sfesantos.holidays.format.HolidayFormatException;
import io.github.sfesantos.holidays.internal.ObservanceNames;
import io.github.sfesantos.holidays.internal.json.JsonValue.JsonArray;
import io.github.sfesantos.holidays.internal.json.JsonValue.JsonNumber;
import io.github.sfesantos.holidays.internal.json.JsonValue.JsonObject;
import io.github.sfesantos.holidays.internal.json.JsonValue.JsonString;

/** Turns layers into the JSON tree the reader accepts. Defaults are left out so files stay short. */
final class LayerJsonWriter {

    private LayerJsonWriter() {}

    static JsonValue write(List<Layer> layers) {
        var elements = new ArrayList<JsonValue>();

        for (var layer : layers) {
            elements.add(writeLayer(layer));
        }

        return object(Map.of("layers", array(elements)));
    }

    private static JsonValue writeLayer(Layer layer) {
        var members = new LinkedHashMap<String, JsonValue>();
        members.put("id", string(layer.name()));
        layer.parent().ifPresent(parent -> members.put("extends", string(parent)));
        layer.weekend().ifPresent(weekend -> members.put("weekend", writeWeekend(weekend)));

        if (layer.declaresObservance()) {
            members.put("observance", writeObservance(layer.observance(), "layer " + layer.name()));
        }

        if (!layer.additions().isEmpty()) {
            members.put("holidays", writeHolidays(layer.additions()));
        }

        if (!layer.overrides().isEmpty()) {
            members.put("override", writeHolidays(List.copyOf(layer.overrides().values())));
        }

        if (!layer.removals().isEmpty()) {
            members.put("remove", array(layer.removals().stream().map(LayerJsonWriter::string).toList()));
        }

        return object(members);
    }

    private static JsonValue writeHolidays(List<Holiday> holidays) {
        var elements = new ArrayList<JsonValue>();

        for (var holiday : holidays) {
            elements.add(writeHoliday(holiday));
        }

        return array(elements);
    }

    private static JsonValue writeHoliday(Holiday holiday) {
        var members = new LinkedHashMap<String, JsonValue>();
        members.put("name", string(holiday.name()));
        var periods = writePeriods(holiday);

        if (periods.size() == 1) {
            members.putAll(periods.get(0));
        } else {
            members.put("periods", array(periods.stream().map(LayerJsonWriter::object).toList()));
        }

        return object(members);
    }

    /** One member map per period; a run of single dates that differ only in the date becomes one {@code dates}. */
    private static List<Map<String, JsonValue>> writePeriods(Holiday holiday) {
        var written = new ArrayList<Map<String, JsonValue>>();
        var periods = holiday.periods();
        var index = 0;

        while (index < periods.size()) {
            var first = periods.get(index);
            var end = index + 1;

            while (end < periods.size() && isDated(first) && sameButTheDate(first, periods.get(end))) {
                end++;
            }

            var members = new LinkedHashMap<String, JsonValue>();

            if (end - index > 1) {
                members.put("dates", array(periods.subList(index, end)
                        .stream()
                        .map(period -> string(((HolidayRule.OnDate) period.rule()).date().toString()))
                        .toList()));
            } else {
                members.put("rule", writeRule(first.rule(), holiday.name()));
            }

            putMembers(members, first, holiday.name());
            written.add(members);
            index = end;
        }

        return written;
    }

    private static boolean isDated(HolidayPeriod period) {
        return period.rule() instanceof HolidayRule.OnDate && period.validity().equals(YearRange.always());
    }

    private static boolean sameButTheDate(HolidayPeriod first, HolidayPeriod other) {
        return isDated(other) && other.type() == first.type() && other.observance().equals(first.observance())
                && other.closesAt().equals(first.closesAt());
    }

    private static void putMembers(Map<String, JsonValue> members, HolidayPeriod period, String holiday) {
        if (period.type() != HolidayType.PUBLIC) {
            members.put("type", string(period.type().name()));
        }

        if (period.validity().from().getValue() != Year.MIN_VALUE) {
            members.put("from", number(period.validity().from().getValue()));
        }

        if (period.validity().until().getValue() != Year.MAX_VALUE) {
            members.put("until", number(period.validity().until().getValue()));
        }

        if (!period.inheritsObservance()) {
            members.put("observance", writeObservance(period.observance(), "holiday '" + holiday + "'"));
        }

        if (period.type() == HolidayType.HALF_DAY) {
            members.put("closesAt", string(period.closesAt().toString()));
        }
    }

    private static JsonValue writeRule(HolidayRule rule, String holiday) {
        var members = new LinkedHashMap<String, JsonValue>();
        var shift = putRule(members, rule, holiday);

        if (shift != 0) {
            members.put("shift", number(shift));
        }

        return object(members);
    }

    /**
     * Puts the members of the rule and returns its shift in days. Nested shifts are written as their sum,
     * so they read back as one flat rule that yields the same dates but is not equal to the original.
     */
    private static int putRule(Map<String, JsonValue> members, HolidayRule rule, String holiday) {
        switch (rule) {
            case HolidayRule.FixedDate r -> members.put("fixed", monthDay(r.monthDay()));
            case HolidayRule.OnDate r -> members.put("date", string(r.date().toString()));
            case HolidayRule.EasterRelative r -> members.put("easter", number(r.offsetDays()));
            case HolidayRule.NthWeekdayOfMonth r -> {
                members.put("nth", number(r.n()));
                members.put("weekday", string(r.dayOfWeek().name()));
                members.put("month", string(r.month().name()));
            }
            case HolidayRule.WeekdayAfter r -> {
                members.put("weekdayAfter", string(r.dayOfWeek().name()));
                members.put("base", monthDay(r.base()));
            }
            case HolidayRule.WeekdayBefore r -> {
                members.put("weekdayBefore", string(r.dayOfWeek().name()));
                members.put("base", monthDay(r.base()));
            }
            case HolidayRule.Hijri r -> members.put("hijri", string(String.format("%02d-%02d", r.month(), r.dayOfMonth())));
            case HolidayRule.Hebrew r -> members.put("hebrew", string(r.month().name() + "-" + String.format("%02d", r.dayOfMonth())));
            case HolidayRule.Custom r -> throw new HolidayFormatException(
                    "holiday '" + holiday + "' uses a custom rule, which has no data form");
            case HolidayRule.Shifted r -> {
                return r.days() + putRule(members, r.base(), holiday);
            }
        }

        return 0;
    }

    private static JsonValue writeObservance(ObservanceRule rule, String owner) {
        return string(ObservanceNames.nameOf(rule)
                .orElseThrow(() -> new HolidayFormatException(owner + " uses a custom observance rule, which has no data form")));
    }

    private static JsonValue writeWeekend(Weekend weekend) {
        return array(weekend.days().stream().map(day -> (JsonValue) string(day.name())).toList());
    }

    private static JsonValue monthDay(MonthDay monthDay) {
        return string(String.format("%02d-%02d", monthDay.getMonthValue(), monthDay.getDayOfMonth()));
    }

    private static JsonString string(String value) {
        return new JsonString(value, 0, 0);
    }

    private static JsonNumber number(int value) {
        return new JsonNumber(Integer.toString(value), 0, 0);
    }

    private static JsonArray array(List<? extends JsonValue> elements) {
        return new JsonArray(List.copyOf(elements), 0, 0);
    }

    private static JsonObject object(Map<String, JsonValue> members) {
        return new JsonObject(members, 0, 0);
    }
}
