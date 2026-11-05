package io.github.sfesantos.holidays.internal.json;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.github.sfesantos.holidays.Holiday;
import io.github.sfesantos.holidays.HolidayRule;
import io.github.sfesantos.holidays.HolidayType;
import io.github.sfesantos.holidays.Layer;
import io.github.sfesantos.holidays.LayerBuilder;
import io.github.sfesantos.holidays.ObservanceRule;
import io.github.sfesantos.holidays.Weekend;
import io.github.sfesantos.holidays.format.HolidayFormatException;
import io.github.sfesantos.holidays.hebrew.HebrewMonth;
import io.github.sfesantos.holidays.internal.ObservanceNames;
import io.github.sfesantos.holidays.internal.json.JsonValue.JsonObject;

/**
 * Walks a parsed JSON tree and calls the layer and holiday builders. Every
 * failure, including the ones the builders raise, is reported with the
 * position of the member that caused it.
 */
final class LayerJsonReader {

    private static final List<String> RULE_KINDS =
            List.of("fixed", "easter", "nth", "weekdayAfter", "weekdayBefore", "hijri", "hebrew");

    private LayerJsonReader() {}

    static List<Layer> read(JsonValue root) {
        var object = root.asObject();
        object.allowOnly("layers");
        var layers = new ArrayList<Layer>();

        for (var element : object.require("layers").asArray().elements()) {
            layers.add(readLayer(element.asObject()));
        }

        return List.copyOf(layers);
    }

    private static Layer readLayer(JsonObject object) {
        object.allowOnly("id", "extends", "weekend", "observance", "holidays", "override", "remove");
        var id = object.require("id");
        var builder = guarded(id, () -> Layer.named(id.asString()));

        object.get("extends").ifPresent(parent -> guarded(parent, () -> builder.extending(parent.asString())));
        object.get("weekend").ifPresent(weekend -> builder.weekend(readWeekend(weekend)));
        object.get("observance").ifPresent(observance -> builder.observance(readObservance(observance, false)));
        object.get("holidays").ifPresent(holidays -> forEachObject(holidays, holiday -> builder.add(readHoliday(holiday))));
        object.get("override").ifPresent(holidays -> forEachObject(holidays, holiday -> builder.override(readHoliday(holiday))));
        object.get("remove").ifPresent(names -> {
            for (var name : names.asArray().elements()) {
                builder.remove(name.asString());
            }
        });

        return guarded(object, builder::build);
    }

    private static Holiday readHoliday(JsonObject object) {
        object.allowOnly("name", "rule", "type", "from", "until", "observance", "closesAt");
        var name = object.require("name");
        var holiday = guarded(name, () -> Holiday.of(name.asString(), readRule(object.require("rule").asObject())));

        var explicitType = object.get("type").map(type -> enumValue(type, HolidayType.class));

        if (explicitType.isPresent()) {
            holiday = holiday.ofType(explicitType.get());
        }

        if (object.get("from").isPresent()) {
            var from = object.require("from");
            var current = holiday;
            holiday = guarded(from, () -> current.validFrom(Year.of(from.asInt())));
        }

        if (object.get("until").isPresent()) {
            var until = object.require("until");
            var current = holiday;
            holiday = guarded(until, () -> current.validUntil(Year.of(until.asInt())));
        }

        if (object.get("observance").isPresent()) {
            holiday = holiday.withObservance(readObservance(object.require("observance"), true));
        }

        if (object.get("closesAt").isPresent()) {
            var closesAt = object.require("closesAt");

            if (explicitType.isPresent() && explicitType.get() != HolidayType.HALF_DAY) {
                throw closesAt.error("closesAt needs type HALF_DAY but the type is " + explicitType.get());
            }

            var current = holiday;
            holiday = guarded(closesAt, () -> current.closingAt(LocalTime.parse(closesAt.asString())));
        }

        return holiday;
    }

    private static HolidayRule readRule(JsonObject rule) {
        var kinds = rule.members().keySet().stream().filter(RULE_KINDS::contains).toList();

        if (kinds.size() != 1) {
            throw rule.error("a rule needs exactly one of " + RULE_KINDS + " but has " + kinds);
        }

        return switch (kinds.get(0)) {
            case "fixed" -> only(rule, "fixed").andThen(() -> HolidayRule.fixed(monthDay(rule.require("fixed"))));
            case "easter" -> only(rule, "easter").andThen(() -> HolidayRule.easterRelative(rule.require("easter").asInt()));
            case "nth" -> only(rule, "nth", "weekday", "month").andThen(() -> HolidayRule.nthWeekdayOfMonth(
                    rule.require("nth").asInt(), enumValue(rule.require("weekday"), DayOfWeek.class),
                    enumValue(rule.require("month"), Month.class)));
            case "weekdayAfter" -> only(rule, "weekdayAfter", "base").andThen(() -> HolidayRule.weekdayAfter(
                    enumValue(rule.require("weekdayAfter"), DayOfWeek.class), monthDay(rule.require("base"))));
            case "weekdayBefore" -> only(rule, "weekdayBefore", "base").andThen(() -> HolidayRule.weekdayBefore(
                    enumValue(rule.require("weekdayBefore"), DayOfWeek.class), monthDay(rule.require("base"))));
            case "hijri" -> only(rule, "hijri").andThen(() -> hijri(rule.require("hijri")));
            case "hebrew" -> only(rule, "hebrew").andThen(() -> hebrew(rule.require("hebrew")));
            default -> throw rule.error("unknown rule kind");
        };
    }

    private static Weekend readWeekend(JsonValue weekend) {
        var days = new ArrayList<DayOfWeek>();

        for (var day : weekend.asArray().elements()) {
            days.add(enumValue(day, DayOfWeek.class));
        }

        return Weekend.of(days.toArray(DayOfWeek[]::new));
    }

    private static ObservanceRule readObservance(JsonValue value, boolean inheritAllowed) {
        var name = value.asString();
        var rule = ObservanceNames.parse(name).orElseThrow(() -> value.error("unknown observance \"" + name + "\"; expected one of " + ObservanceNames.names()));

        if (rule == ObservanceRule.inherit() && !inheritAllowed) {
            throw value.error("a layer cannot declare observance INHERIT; leave the member out instead");
        }

        return rule;
    }

    private static MonthDay monthDay(JsonValue value) {
        var text = value.asString();

        if (!text.matches("\\d{2}-\\d{2}")) {
            throw value.error("expected a day as \"MM-DD\" but found \"" + text + "\"");
        }

        return guarded(value, () -> MonthDay.of(Integer.parseInt(text.substring(0, 2)), Integer.parseInt(text.substring(3))));
    }

    private static HolidayRule hijri(JsonValue value) {
        var text = value.asString();

        if (!text.matches("\\d{2}-\\d{2}")) {
            throw value.error("expected a Hijri day as \"MM-DD\" but found \"" + text + "\"");
        }

        return guarded(value, () -> HolidayRule.hijri(Integer.parseInt(text.substring(0, 2)), Integer.parseInt(text.substring(3))));
    }

    private static HolidayRule hebrew(JsonValue value) {
        var text = value.asString();
        int dash = text.indexOf('-');

        if (dash < 1 || !text.substring(dash + 1).matches("\\d{1,2}")) {
            throw value.error("expected a Hebrew day as \"MONTH-DD\", for example \"NISAN-15\", but found \"" + text + "\"");
        }

        var month = enumValue(new JsonValue.JsonString(text.substring(0, dash), value.line(), value.column()), HebrewMonth.class);

        return guarded(value, () -> HolidayRule.hebrew(month, Integer.parseInt(text.substring(dash + 1))));
    }

    private static <E extends Enum<E>> E enumValue(JsonValue value, Class<E> type) {
        var name = value.asString();

        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException e) {
            throw value.error("unknown " + type.getSimpleName() + " \"" + name + "\"; expected one of " + List.of(type.getEnumConstants()));
        }
    }

    private static void forEachObject(JsonValue array, Consumer<JsonObject> action) {
        for (var element : array.asArray().elements()) {
            action.accept(element.asObject());
        }
    }

    /** Runs a builder call and turns its validation failure into a positioned error. */
    private static <T> T guarded(JsonValue at, Supplier<T> call) {
        try {
            return call.get();
        } catch (IllegalArgumentException | IllegalStateException | DateTimeException e) {
            throw at.error(e.getMessage());
        }
    }

    private static Guard only(JsonObject rule, String... members) {
        rule.allowOnly(members);

        return new Guard(rule);
    }

    private record Guard(JsonObject at) {

        HolidayRule andThen(Supplier<HolidayRule> call) {
            return guarded(at, call);
        }
    }
}
