package io.github.sfesantos.holidays.internal.json;

import java.time.MonthDay;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.sfesantos.holidays.Holiday;
import io.github.sfesantos.holidays.HolidayRule;
import io.github.sfesantos.holidays.HolidayType;
import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.ObservanceRule;
import io.github.sfesantos.holidays.Weekend;
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
        members.put("rule", writeRule(holiday));

        if (holiday.type() != HolidayType.PUBLIC) {
            members.put("type", string(holiday.type().name()));
        }

        if (holiday.validity().from().getValue() != Year.MIN_VALUE) {
            members.put("from", number(holiday.validity().from().getValue()));
        }

        if (holiday.validity().until().getValue() != Year.MAX_VALUE) {
            members.put("until", number(holiday.validity().until().getValue()));
        }

        if (!holiday.inheritsObservance()) {
            members.put("observance", writeObservance(holiday.observance(), "holiday '" + holiday.name() + "'"));
        }

        if (holiday.type() == HolidayType.HALF_DAY) {
            members.put("closesAt", string(holiday.closesAt().toString()));
        }

        return object(members);
    }

    private static JsonValue writeRule(Holiday holiday) {
        var members = new LinkedHashMap<String, JsonValue>();

        switch (holiday.rule()) {
            case HolidayRule.FixedDate r -> members.put("fixed", monthDay(r.monthDay()));
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
                    "holiday '" + holiday.name() + "' uses a custom rule, which has no data form");
        }

        return object(members);
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
