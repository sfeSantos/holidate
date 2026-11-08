# Holidays

A dependency-free Java library for holiday calendars and business day queries.

You describe holidays as rules (a fixed day, an offset from Easter, the fourth
Thursday of November) and stack them in layers (country, region, province,
city). The library resolves the rules once per year, applies the observance
rule of your region (Sunday to Monday, weekend to Monday, nearest weekday) and
answers questions like "is this a business day?" from precomputed indexes.

- Zero dependencies: only `java.base`.
- Immutable and thread-safe calendars, built once with a builder.
- Queries are O(1) or O(log n) over per-year indexes, computed lazily and
  cached: `isHoliday` runs in about a nanosecond without allocating, and an
  indexed year costs about 2 KB of heap. See [Performance](#performance).
- The core knows nothing about any country. Country data is JSON: write your
  own or add the optional `holidays-data` module and ask a registry for
  `ES-AN-MA`.

Requires Java 25.

## Status

Early. Version 0.3.0, the release on Maven Central, covers rules, layers,
observance, the queries, navigation (`nextHoliday`, `addBusinessDays`,
`businessDaysBetween`), `TemporalAdjuster` and `TemporalQuery` integration,
`Clock` overloads, `union` of calendars, payment date conventions and cut-off
times, business hours and half days, Islamic and Hebrew calendar rules, the
JSON data format, a calendar registry with a provider SPI and the
`holidays-data` module with Spain and Brazil. Since then `main` has gained a
JMH benchmark module and a leaner year index that holds less than half the
heap it did in 0.3.0; both are measured in [Performance](#performance). The
public API may still change before 1.0. The roadmap is the
[issue list](https://github.com/sfeSantos/holidate/issues): `P1` is what
stands between `main` and 1.0, `P3` is what may come after.

## Install

```xml
<dependency>
    <groupId>io.github.sfesantos</groupId>
    <artifactId>holidays-core</artifactId>
    <version>0.3.0</version>
</dependency>
```

The country packs are a separate, optional artifact:

```xml
<dependency>
    <groupId>io.github.sfesantos</groupId>
    <artifactId>holidays-data</artifactId>
    <version>0.3.0</version>
</dependency>
```

## Example: Málaga, Spain

Spain has national holidays, each autonomous community adds its own and
decides whether a holiday that falls on a Sunday moves to Monday (Andalucía
does), and each city adds two local holidays. That maps directly to three layers.

```java
import io.github.sfesantos.holidays.*;

import java.time.*;

var spain = Layer.named("ES")
        .add(Holiday.fixed("Año Nuevo", Month.JANUARY, 1),
                Holiday.fixed("Epifanía del Señor", Month.JANUARY, 6),
                Holiday.easterRelative("Viernes Santo", -2),
                Holiday.fixed("Fiesta del Trabajo", Month.MAY, 1),
                Holiday.fixed("Asunción de la Virgen", Month.AUGUST, 15),
                Holiday.fixed("Fiesta Nacional de España", Month.OCTOBER, 12),
                Holiday.fixed("Todos los Santos", Month.NOVEMBER, 1),
                Holiday.fixed("Día de la Constitución", Month.DECEMBER, 6),
                Holiday.fixed("Inmaculada Concepción", Month.DECEMBER, 8),
                Holiday.fixed("Navidad", Month.DECEMBER, 25))
        .build();

var andalucia = Layer.named("ES-AN")
        .add(Holiday.fixed("Día de Andalucía", Month.FEBRUARY, 28),
                Holiday.easterRelative("Jueves Santo", -3))
        .observance(ObservanceRule.sundayToMonday())
        .build();

var malaga = Layer.named("ES-AN-MA")
        .add(Holiday.fixed("Incorporación de Málaga a la Corona de Castilla", Month.AUGUST, 19),
                Holiday.fixed("Virgen de la Victoria", Month.SEPTEMBER, 8))
        .build();

var calendar = HolidayCalendar.builder()
        .weekend(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        .layers(spain, andalucia, malaga)   // order is precedence, last wins
        .build();
```

Then ask:

```java
calendar.isHoliday(LocalDate.of(2026, 4, 2));        // true, Jueves Santo
calendar.isBusinessDay(LocalDate.of(2026, 4, 4));    // false, Saturday

// 1 November 2026 is a Sunday; Andalucía observes it on Monday
calendar.isHoliday(LocalDate.of(2026, 11, 1));       // false
calendar.holidayOn(LocalDate.of(2026, 11, 2));
// Optional[HolidayOccurrence[name=Todos los Santos, original=2026-11-01,
//                            observed=2026-11-02, type=PUBLIC, layer=ES]]

calendar.holidaysIn(Year.of(2026));                  // 14 occurrences, by date

// Navigation. "Next" and "previous" are exclusive; there are *Inclusive variants
var dec9 = LocalDate.of(2026, 12, 9);
calendar.nextHoliday(dec9);                          // Navidad, 2026-12-25
calendar.daysUntilNextHoliday(dec9);                 // OptionalLong[16]
calendar.nextBusinessDay(LocalDate.of(2026, 12, 24)); // 2026-12-28
calendar.addBusinessDays(LocalDate.of(2026, 12, 24), 3); // 2026-12-30
calendar.businessDaysBetween(LocalDate.of(2026, 12, 21), LocalDate.of(2027, 1, 1)); // 8

// java.time integration: adjusters keep the time of day, queries work on any temporal
LocalDateTime.of(2026, 12, 24, 18, 0).with(calendar.nextBusinessDayAdjuster()); // 2026-12-28T18:00
LocalDate.of(2026, 12, 25).query(calendar.isHolidayQuery());                   // true

// "Today" always takes a Clock; the library never calls LocalDate.now() itself
var clock = Clock.fixed(Instant.parse("2026-12-25T10:00:00Z"), ZoneId.of("Europe/Madrid"));
calendar.isHolidayToday(clock);                      // true
calendar.nextBusinessDay(clock);                     // 2026-12-28
```

The 2026 output matches the calendars published in the BOE and the BOJA.
The test [MalagaExampleTest](holidays-core/src/test/java/io/github/sfesantos/holidays/examples/MalagaExampleTest.java)
keeps it that way.

## Example: Rio de Janeiro, Brazil

Brazil never moves a holiday, so no observance rule is needed. What it has
instead is holiday types: Carnival and Corpus Christi are a national "ponto
facultativo" (optional day off), the state of Rio de Janeiro makes Carnival
Tuesday a real holiday, and the city gives Ash Wednesday off until noon.
Black Awareness Day was a state holiday for years before it became national
in 2024.

```java
var brazil = Layer.named("BR")
        .add(Holiday.fixed("Confraternização Universal", Month.JANUARY, 1),
                Holiday.easterRelative("Segunda-feira de Carnaval", -48).ofType(HolidayType.OBSERVANCE),
                Holiday.easterRelative("Terça-feira de Carnaval", -47).ofType(HolidayType.OBSERVANCE),
                Holiday.easterRelative("Sexta-feira Santa", -2),
                Holiday.fixed("Tiradentes", Month.APRIL, 21),
                Holiday.fixed("Dia do Trabalho", Month.MAY, 1),
                Holiday.easterRelative("Corpus Christi", 60).ofType(HolidayType.OBSERVANCE),
                Holiday.fixed("Independência do Brasil", Month.SEPTEMBER, 7),
                Holiday.fixed("Nossa Senhora Aparecida", Month.OCTOBER, 12),
                Holiday.fixed("Finados", Month.NOVEMBER, 2),
                Holiday.fixed("Proclamação da República", Month.NOVEMBER, 15),
                Holiday.fixed("Dia da Consciência Negra", Month.NOVEMBER, 20).validFrom(Year.of(2024)),
                Holiday.fixed("Natal", Month.DECEMBER, 25))
        .build();

var rioState = Layer.named("BR-RJ")
        .add(Holiday.fixed("São Jorge", Month.APRIL, 23))
        .override(Holiday.easterRelative("Terça-feira de Carnaval", -47))                  // now PUBLIC
        .override(Holiday.fixed("Dia da Consciência Negra", Month.NOVEMBER, 20).validFrom(Year.of(2002)))
        .build();

var rioCity = Layer.named("BR-RJ-RIO")
        .add(Holiday.fixed("São Sebastião", Month.JANUARY, 20),
                Holiday.easterRelative("Quarta-feira de Cinzas", -46).ofType(HolidayType.HALF_DAY))
        .build();

var rio = HolidayCalendar.builder()
        .layers(brazil, rioState, rioCity)
        .build();
```

```java
rio.isHoliday(LocalDate.of(2026, 2, 17));       // true, Carnival Tuesday
rio.isBusinessDay(LocalDate.of(2026, 2, 17));   // false, PUBLIC in the state layer
rio.isBusinessDay(LocalDate.of(2026, 2, 16));   // true, Monday is only OBSERVANCE
rio.isBusinessDay(LocalDate.of(2026, 2, 18));   // true, HALF_DAY still counts as working

rio.isHoliday(LocalDate.of(2023, 11, 20));      // true, state holiday before it went national
rio.holidayOn(LocalDate.of(2028, 2, 29));       // Carnival Tuesday lands on a leap day
```

See [RioDeJaneiroExampleTest](holidays-core/src/test/java/io/github/sfesantos/holidays/examples/RioDeJaneiroExampleTest.java).

## Payment dates

A direct debit due on a holiday or a weekend has to move. `BusinessDayConvention`
names the usual choices: `FOLLOWING` takes the next business day, `PRECEDING`
the one before, and the `MODIFIED_*` variants do the same unless that would
leave the month, in which case they go the other way. A date that is already
a business day never moves.

```java
import static io.github.sfesantos.holidays.BusinessDayConvention.*;

var due = LocalDate.of(2026, 8, 15);                 // Saturday, and Asunción

calendar.adjust(due, FOLLOWING);                     // 2026-08-17
calendar.adjust(due, PRECEDING);                     // 2026-08-14
calendar.adjust(LocalDate.of(2026, 5, 31), MODIFIED_FOLLOWING); // 2026-05-29, June 1 would leave May
due.atTime(9, 0).with(calendar.adjuster(FOLLOWING)); // 2026-08-17T09:00
```

A `CutOff` carries the market's cut-off time and zone. An instruction received
at or after it is booked on the next day, and the convention then moves that
date off holidays and weekends:

```java
var madrid = CutOff.at(LocalTime.of(16, 0), ZoneId.of("Europe/Madrid"));

calendar.adjust(Instant.parse("2026-12-24T15:30:00Z"), madrid, FOLLOWING);   // 16:30 in Madrid -> 25 Dec -> 2026-12-28
calendar.adjust(LocalDateTime.of(2026, 12, 24, 15, 59), madrid, FOLLOWING); // before cut-off -> 2026-12-24
```

## Business hours and half days

A calendar may declare opening hours; without them a business day is open all
day. A `HALF_DAY` holiday keeps the day a business day but closes it early, at
noon unless `closingAt` says otherwise.

```java
var calendar = HolidayCalendar.builder()
        .businessHours(LocalTime.of(9, 0), LocalTime.of(17, 30))
        .layer(Layer.named("ES")
                .add(Holiday.fixed("Navidad", Month.DECEMBER, 25),
                        Holiday.fixed("Nochebuena", Month.DECEMBER, 24).closingAt(LocalTime.of(13, 0)))
                .build())
        .build();

calendar.businessHoursOn(LocalDate.of(2026, 12, 23)); // Optional[BusinessHours[09:00 to 17:30]]
calendar.businessHoursOn(LocalDate.of(2026, 12, 24)); // Optional[BusinessHours[09:00 to 13:00]]
calendar.businessHoursOn(LocalDate.of(2026, 12, 25)); // Optional.empty
calendar.isHalfDay(LocalDate.of(2026, 12, 24));       // true
calendar.isOpen(LocalDateTime.of(2026, 12, 24, 12, 59)); // true
calendar.isOpen(LocalDateTime.of(2026, 12, 24, 13, 0));  // false
calendar.isOpen(Instant.parse("2026-12-23T08:30:00Z"), ZoneId.of("Europe/Madrid")); // true, 09:30 local
```

## Combining calendars

Settlement between two markets needs both to be open. `union` composes
calendars: a day is a holiday when it is a holiday in any member and a
business day only when it is a business day in all of them. The weekend is
the union of the members' weekends, and every query, navigation included,
works on the result.

```java
var eurBrl = HolidayCalendar.union(spain, brazil);

eurBrl.isBusinessDay(LocalDate.of(2026, 11, 2));    // false, Finados in Brazil
eurBrl.isBusinessDay(LocalDate.of(2026, 12, 8));    // false, Inmaculada in Spain
eurBrl.addBusinessDays(LocalDate.of(2026, 12, 23), 2); // 2026-12-28, T+2 across both
eurBrl.holidaysIn(Year.of(2026));                   // every member's occurrences, each with its layer
```

When two members share a day, `holidayOn` returns the first member's
occurrence. Members built with this library hand over their indexes, so a
union costs the same per query as a single calendar; any other
`HolidayCalendar` implementation is read through its public queries once per
year.

## Lunar calendars

`Holiday.hijri(name, month, day)` places a day of the Islamic calendar using
the Umm al-Qura tables that ship with the JDK (`HijrahChronology`), valid for
Gregorian years 1883 to 2173. Local moon sightings may differ from the tables
by a day. Because the Islamic year is eleven days shorter, a holiday can fall
twice in one Gregorian year, and the calendar lists both occurrences.

```java
var gulf = HolidayCalendar.builder()
        .weekend(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
        .layer(Layer.named("AE")
                .add(Holiday.hijri("Eid al-Fitr", 10, 1),
                        Holiday.hijri("Arafat Day", 12, 9),
                        Holiday.hijri("Eid al-Adha", 12, 10),
                        Holiday.hijri("Islamic New Year", 1, 1))
                .build())
        .build();

gulf.holidayOn(LocalDate.of(2026, 3, 20));           // Eid al-Fitr
gulf.holidaysIn(Year.of(2008));                       // Islamic New Year on 10 Jan and again on 29 Dec
```

`Holiday.hebrew(name, HebrewMonth, day)` does the same for the Hebrew
calendar, computed by the library since the JDK does not ship it. `HebrewMonth.ADAR`
is the Adar that carries the festivals (Adar II in a leap year) and `ADAR_I`
exists only in leap years.

```java
var israel = Layer.named("IL")
        .add(Holiday.hebrew("Rosh Hashanah", HebrewMonth.TISHRI, 1),
                Holiday.hebrew("Yom Kippur", HebrewMonth.TISHRI, 10),
                Holiday.hebrew("Pesach", HebrewMonth.NISAN, 15),
                Holiday.hebrew("Purim", HebrewMonth.ADAR, 14).ofType(HolidayType.OBSERVANCE))
        .build();
// Pesach 2026-04-02, Rosh Hashanah 2026-09-12, Yom Kippur 2026-09-21, Purim 2026-03-03
```

## Data files

Holiday data is data, not code. `HolidayFormat.json()` reads and writes layers
as JSON, with zero dependencies: the parser is hand-written and strict, and
every error names the line and column. One file per country, regions inside;
`extends` names the layer a region builds on.

```json
{
  "layers": [
    {
      "id": "ES",
      "weekend": ["SATURDAY", "SUNDAY"],
      "holidays": [
        { "name": "Año Nuevo",     "rule": { "fixed": "01-01" } },
        { "name": "Viernes Santo", "rule": { "easter": -2 } },
        { "name": "Constitución",  "rule": { "fixed": "12-06" }, "from": 1978 },
        { "name": "Nochebuena",    "rule": { "fixed": "12-24" }, "closesAt": "13:00" }
      ]
    },
    {
      "id": "ES-AN",
      "extends": "ES",
      "observance": "SUNDAY_TO_MONDAY",
      "holidays": [
        { "name": "Día de Andalucía", "rule": { "fixed": "02-28" } },
        { "name": "Jueves Santo",     "rule": { "easter": -3 } }
      ]
    },
    {
      "id": "ES-CT",
      "extends": "ES",
      "remove": ["Constitución"],
      "override": [ { "name": "Sant Joan", "rule": { "fixed": "06-24" } } ]
    }
  ]
}
```

```java
var layers = HolidayFormat.json().parse(Path.of("ES.json"));
var andalucia = HolidayCalendar.builder().layers(layers.get(0), layers.get(1)).build();

HolidayFormat.json().write(List.of(spain, andalucia), Path.of("export.json")); // from Java to data
```

Rule kinds: `fixed` (`"MM-DD"`), `easter` (offset in days), `nth` with
`weekday` and `month`, `weekdayAfter` or `weekdayBefore` with `base`, `hijri`
(`"MM-DD"`) and `hebrew` (`"NISAN-15"`). Optional members on a holiday:
`type`, `from`, `until`, `observance` and `closesAt`. Observance names are
`NONE`, `SUNDAY_TO_MONDAY`, `WEEKEND_TO_MONDAY`,
`SATURDAY_TO_FRIDAY_SUNDAY_TO_MONDAY` and `NEAREST_WEEKDAY`. `custom` rules
and observances exist only in Java and cannot be written. The JSON Schema
ships in the jar at `META-INF/holidays/holidays.schema.json`, so files can be
validated with any tool before loading.

## Country packs and the registry

`holidays-data` ships one JSON file per country under `META-INF/holidays`
and registers itself as a `HolidayDataProvider` service. A `CalendarRegistry`
resolves an id to a calendar by stacking the layer on the chain it extends,
builds it once and caches it:

```java
import io.github.sfesantos.holidays.registry.*;

var registry = CalendarRegistry.load();          // every provider on the module path or classpath

var malaga = registry.calendar("ES-AN-MA");      // ES, then ES-AN, then ES-AN-MA
var rio = registry.calendar("BR-RJ-RIO");
registry.layerIds();                             // [BR, BR-RJ, BR-RJ-RIO, ES, ES-AN, ES-AN-MA, ES-CT]
```

Ids follow ISO 3166-1 and 3166-2 where one exists (`ES`, `ES-AN`, `ES-CT`,
`BR`, `BR-RJ`) and are free text below that (`ES-AN-MA`, `BR-RJ-RIO`). The
shipped packs cover Spain with Andalucía, Málaga and Catalunya, and Brazil
with the state and the city of Rio de Janeiro. Each pack is pinned by one test
per year and per official source (BOE, BOJA, DOGC, Brazilian federal and state
law) for 2025, 2026 and 2027; updating a pack is editing the file and running
the tests. The same layers are available as Java constants for calendars
built in code, read from the pack rather than duplicated:

```java
import io.github.sfesantos.holidays.data.*;

var malaga = HolidayCalendar.builder()
        .layers(Spain.NATIONAL, Spain.ANDALUCIA, Spain.MALAGA)
        .build();

var germany = Layer.named("DE")
        .add(Holidays.NEW_YEAR, Holidays.GOOD_FRIDAY, Holidays.EASTER_MONDAY, Holidays.LABOUR_DAY,
                Holidays.ASCENSION, Holidays.WHIT_MONDAY, Holidays.CHRISTMAS, Holidays.BOXING_DAY)
        .build();
```

**Your own data.** Providers are asked in the order given and the first that
knows an id wins, so a provider placed ahead of the shipped pack overrides it.
`DirectoryDataProvider` reads every `.json` file in a directory,
`ClasspathDataProvider` reads named resources, and `HolidayDataProvider` is
two methods to implement over a database or a vendor feed:

```java
var mine = DirectoryDataProvider.of(Path.of("/etc/holidays"));   // my ES.json replaces the shipped one
var registry = CalendarRegistry.load(mine);                       // mine first, then the discovered providers

var standalone = CalendarRegistry.of(mine, HolidayDataProvider.of(spain, andalucia));  // no ServiceLoader
```

**Exception layers.** A branch that closes on days the region does not can
keep those closures in a layer of its own, stacked on top of the registry's
chain without touching the base data. A one-off closure is a holiday valid
for a single year:

```java
var closures = Layer.named("SANTANDER-ES")
        .add(Holiday.fixed("Inventory", Month.JULY, 3).validBetween(Year.of(2027), Year.of(2027)))
        .build();

var branch = registry.builder("ES-AN-MA")        // the chain, ready for more layers
        .layer(closures)
        .businessHours(LocalTime.of(8, 30), LocalTime.of(14, 30))
        .build();
```

## Concepts

**Rules.** A `HolidayRule` turns a year into dates:
`fixed`, `easterRelative`, `nthWeekdayOfMonth` (negative `n` counts from the
end), `weekdayAfter`, `weekdayBefore` and `custom(Function<Year, Optional<LocalDate>>)`
give at most one date a year; `hijri` and `hebrew` follow the Islamic and
Hebrew calendars, whose shorter years can put the same holiday twice in one
Gregorian year.
A `Holiday` is a rule plus a name, a `HolidayType` and a validity range
(`validFrom`, `validUntil`).

**Types.** `PUBLIC` and `BANK` make a day non-working. `OBSERVANCE` and
`HALF_DAY` show up in `isHoliday` and `holidayOn` but leave `isBusinessDay`
true; a `HALF_DAY` also shortens that day's business hours. Change the set
with `nonWorkingTypes(...)` on the builder.

**Layers.** A `Layer` adds holidays, overrides inherited ones by name or
removes them. Adding a name that a lower layer already defines is an error:
say `override` or `remove` explicitly. A layer may also declare its own
weekend or observance rule, which apply from that layer up, and may name the
layer it extends (`extending("ES")`) for data files and registries; the
builder itself stacks layers in the order given. Layers are immutable and can
be shared between calendars.

**Observance.** An `ObservanceRule` moves a holiday off a day on which it is
not observed. The rule of the highest layer that declares one applies to the
whole calendar; a single holiday can opt out with
`withObservance(ObservanceRule.none())`. Rules chain, so when Christmas and
Boxing Day both fall on a weekend under `weekendToMonday()` they land on
Monday and Tuesday. A holiday may even move into the neighbouring year (1
January on a Saturday under the US rule is observed on 31 December).

**Occurrences.** Every query returns `HolidayOccurrence` records carrying
the original date, the observed date, the type, the layer that defined the
holiday and, for a `HALF_DAY`, the time business closes.

**Navigation.** `nextHoliday`, `previousHoliday`, `nextBusinessDay` and
`previousBusinessDay` are exclusive: they never return the date you pass in.
`nextHolidayInclusive`, `nextOrSameBusinessDay` and their `previous`
counterparts do. `holidaysBetween` and `businessDaysBetween` take `from`
inclusive and `to` exclusive, like `ChronoUnit.DAYS.between`, and
`businessDaysBetween` is negative when `from` is after `to`. `addBusinessDays`
accepts negative counts and skips whole years using precomputed counts. Holiday
searches look at most one hundred years ahead or back and return empty beyond
that; business day searches throw `IllegalStateException` when every day in
that window is a weekend day or a holiday.

**Weekend.** Any set of days: `weekend(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)`
for the Gulf, a single day, or none. The builder's weekend is the fallback;
the highest layer that declares one wins.

**Easter.** Gregorian by default. Plug a different `EasterAlgorithm` into the
builder for Julian or Orthodox dates.

## Classes

What each public type is for and how it is used. Where an earlier section
already shows a class at work, this only points there.

### Building a calendar (`io.github.sfesantos.holidays`)

**`HolidayCalendar`** is the calendar: an immutable, thread-safe object that
answers every query in `HolidayQueries` and `BusinessDayQueries`. It is built
with `HolidayCalendar.builder()`, composed with `HolidayCalendar.union(...)`
or taken from a `CalendarRegistry`. `warmUp(from, until)` precomputes years.

**`HolidayCalendarBuilder`** stacks layers in precedence order and sets the
calendar-wide defaults: the weekend and observance that apply when no layer
declares one, the business hours, the Easter algorithm and which holiday
types make a day non-working.

```java
var calendar = HolidayCalendar.builder()
        .layers(spain, andalucia, malaga)
        .weekend(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)      // fallback, a layer may override it
        .observance(ObservanceRule.none())                   // fallback, a layer may override it
        .businessHours(LocalTime.of(9, 0), LocalTime.of(17, 0))
        .easter(EasterAlgorithm.gregorian())
        .nonWorkingTypes(HolidayType.PUBLIC)                 // BANK days stay business days here
        .build();

calendar.warmUp(Year.of(2026), Year.of(2030));
```

**`Layer`** and **`LayerBuilder`** group holidays by jurisdiction. A layer
adds holidays, overrides or removes inherited ones by name, may declare its
own weekend or observance, and may name the layer it extends. See
[Málaga](#example-málaga-spain) and [Rio de Janeiro](#example-rio-de-janeiro-brazil).

```java
var catalunya = Layer.named("ES-CT")
        .extending("ES")                                     // used by the registry, ignored by the builder
        .add(Holiday.fixed("Sant Joan", Month.JUNE, 24))
        .override(Holiday.fixed("Día de la Constitución", Month.DECEMBER, 6).withObservance(ObservanceRule.none()))
        .remove("Epifanía del Señor")
        .weekend(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        .observance(ObservanceRule.sundayToMonday())
        .build();
```

**`Holiday`** is a named rule plus a `HolidayType`, a `YearRange` and an
optional observance of its own. The name is the identity across layers.
Factories cover the common rules; `validFrom`, `validUntil`, `validBetween`,
`ofType`, `closingAt` and `withObservance` return modified copies.

**`HolidayRule`** is the sealed family of rules behind a holiday: `FixedDate`,
`EasterRelative`, `NthWeekdayOfMonth`, `WeekdayAfter`, `WeekdayBefore`,
`Custom`, `Hijri` and `Hebrew`. Use it directly when `Holiday` has no shortcut
for the rule you need, or to ask a rule for its dates.

```java
var thanksgiving = Holiday.of("Thanksgiving", HolidayRule.nthWeekdayOfMonth(4, DayOfWeek.THURSDAY, Month.NOVEMBER));
var memorialDay = Holiday.of("Memorial Day", HolidayRule.nthWeekdayOfMonth(-1, DayOfWeek.MONDAY, Month.MAY));
var electionDay = Holiday.of("Election Day", HolidayRule.weekdayAfter(DayOfWeek.TUESDAY, MonthDay.of(11, 1)))
        .ofType(HolidayType.OBSERVANCE)
        .validUntil(Year.of(2030));
var juneteenth = Holiday.fixed("Juneteenth", Month.JUNE, 19).validFrom(Year.of(2021));
var leapDay = Holiday.custom("Leap Day", year -> year.isLeap() ? Optional.of(year.atDay(60)) : Optional.empty());

thanksgiving.dateIn(Year.of(2026), EasterAlgorithm.gregorian());   // Optional[2026-11-26]
HolidayRule.easterRelative(-2).datesIn(Year.of(2026), EasterAlgorithm.gregorian()); // [2026-04-03]
```

**`HolidayType`** is `PUBLIC`, `BANK`, `OBSERVANCE` or `HALF_DAY`. The first
two make a day non-working by default; the builder's `nonWorkingTypes` changes
that. See [Rio de Janeiro](#example-rio-de-janeiro-brazil).

**`YearRange`** is the closed range of years a holiday is in force:
`always()`, `from(year)`, `until(year)` or `between(from, until)`. Usually
set through `Holiday.validFrom` and friends.

**`Weekend`** is the set of non-working weekdays, stored as a bit mask:
`Weekend.standard()` is Saturday and Sunday, `Weekend.none()` has no weekend,
`Weekend.of(days...)` anything else. `contains(day)` and `days()` read it.

**`ObservanceRule`** moves a holiday off a day on which it is not observed.
Built-in rules: `none()`, `sundayToMonday()`, `weekendToMonday()`,
`saturdayToFridaySundayToMonday()` (the US federal model), `nearestWeekday()`
and `inherit()`, which means "use the layer's or calendar's rule". A custom
rule is a lambda that gets an **`ObservanceContext`** to ask whether a date is
a weekend or already taken by another holiday.

```java
ObservanceRule skipSaturdayToo = (date, context) -> switch (date.getDayOfWeek()) {
    case SATURDAY -> date.plusDays(2);
    case SUNDAY -> date.plusDays(1);
    default -> context.isHoliday(date) ? date.plusDays(1) : date;   // repeated until the date settles
};

var layer = Layer.named("GB")
        .add(Holiday.fixed("Christmas Day", Month.DECEMBER, 25),
                Holiday.fixed("Boxing Day", Month.DECEMBER, 26),
                Holiday.fixed("Remembrance Sunday", Month.NOVEMBER, 11).withObservance(ObservanceRule.none()))
        .observance(ObservanceRule.weekendToMonday())
        .build();
```

**`HolidayOccurrence`** is what queries return: `name`, `original` date,
`observed` date, `type`, the `layer` that defined it and `closesAt` for a
half day. `isMoved()` tells whether the observance rule shifted it.

```java
for (var occurrence : calendar.holidaysIn(Year.of(2026))) {
    if (occurrence.isMoved()) {
        System.out.println(occurrence.name() + " moved from " + occurrence.original() + " to " + occurrence.observed());
    }
}
```

### Asking questions (`io.github.sfesantos.holidays`)

**`HolidayQueries`** holds everything about holidays: `isHoliday`,
`holidayOn`, `holidaysIn`, `holidaysBetween`, `nextHoliday`,
`previousHoliday`, `daysUntilNextHoliday`, their `Inclusive` variants, the
`Clock` overloads for today and the `TemporalQuery` factories. See the
[Málaga example](#example-málaga-spain).

**`BusinessDayQueries`** holds everything about working days: `isBusinessDay`,
`nextBusinessDay`, `addBusinessDays`, `businessDaysBetween`, `adjust` with a
convention or a cut-off, `businessHoursOn`, `isOpen`, `isHalfDay`, the
`TemporalAdjuster` factories and the `Clock` overloads. See
[Payment dates](#payment-dates) and [Business hours](#business-hours-and-half-days).

**`BusinessDayConvention`** is `UNADJUSTED`, `FOLLOWING`, `PRECEDING`,
`MODIFIED_FOLLOWING` or `MODIFIED_PRECEDING`. It is normally passed to
`calendar.adjust(date, convention)`, and can be applied on its own with
`convention.adjust(date, calendar)`. See [Payment dates](#payment-dates).

**`CutOff`** is a time of day in a market's zone after which an instruction
counts for the next day. `dateOf(instant)` and `dateOf(localDateTime)` give the
booking date; `calendar.adjust(instant, cutOff, convention)` goes on to the
settlement date. See [Payment dates](#payment-dates).

**`BusinessHours`** is an open and a close time, `open` inclusive and `close`
exclusive. `allDay()` is the default; `contains(time)` and `closingAt(time)`
help when you read `businessHoursOn`. See
[Business hours](#business-hours-and-half-days).

```java
var hours = calendar.businessHoursOn(LocalDate.of(2026, 12, 24)).orElseThrow();   // 09:00 to 13:00 on a half day

hours.contains(LocalTime.of(12, 30));   // true
hours.contains(LocalTime.of(13, 0));    // false, close is exclusive
hours.isAllDay();                       // false
```

### Easter and other calendars

**`EasterAlgorithm`** (`io.github.sfesantos.holidays.easter`) turns a year
into Easter Sunday. `EasterAlgorithm.gregorian()` is the default; any
implementation plugs into the builder, which is how Julian or Orthodox dates
would come in. **`GregorianEaster.of(year)`** is the computation on its own.

```java
GregorianEaster.of(2026);                                 // 2026-04-05

EasterAlgorithm julianOnGregorian = year -> /* your Julian computation */ LocalDate.of(year, 4, 12);
var orthodox = HolidayCalendar.builder()
        .easter(julianOnGregorian)
        .layer(Layer.named("GR").add(Holiday.easterRelative("Μεγάλη Παρασκευή", -2)).build())
        .build();
```

**`HebrewCalendar`** and **`HebrewMonth`** (`io.github.sfesantos.holidays.hebrew`)
are the arithmetic behind `Holiday.hebrew`, usable on their own:
`HebrewCalendar.toLocalDate(year, month, day)`, `yearOf(date)`,
`isLeapYear(year)`, `daysInMonth(year, month)`; `HebrewMonth.ADAR` is the Adar
that carries the festivals and `ADAR_I` exists only in leap years. See
[Lunar calendars](#lunar-calendars).

```java
HebrewCalendar.yearOf(LocalDate.of(2026, 4, 2));                   // 5786
HebrewCalendar.isLeapYear(5787);                                   // true
HebrewCalendar.toLocalDate(5786, HebrewMonth.NISAN.numberIn(5786).orElseThrow(), 15); // 2026-04-02
```

### Data files (`io.github.sfesantos.holidays.format`)

**`HolidayFormat`** reads and writes layers as data. `HolidayFormat.json()`
is the only format today; `parse(String | Reader | Path)` returns
`List<Layer>` and `write(layers)` or `write(layers, Path)` goes the other way.
**`HolidayFormatException`** carries the `line()` and `column()` of the first
problem. See [Data files](#data-files).

```java
try {
    var layers = HolidayFormat.json().parse(Path.of("ES.json"));
} catch (HolidayFormatException e) {
    log.error("ES.json line {} column {}: {}", e.line(), e.column(), e.getMessage());
}
```

### Registry (`io.github.sfesantos.holidays.registry`)

**`CalendarRegistry`** turns an id into a calendar by stacking the layer on
the chain it extends, caching the result. `of(providers...)` takes explicit
providers, `load(first...)` adds every `ServiceLoader` provider after the
given ones. `layerIds()`, `layer(id)`, `layers(id)`, `builder(id)` and
`calendar(id)` read it. See [Country packs](#country-packs-and-the-registry).

**`HolidayDataProvider`** is the two-method SPI a source of layers
implements: `layerIds()` and `layer(id)`. `HolidayDataProvider.of(layers...)`
wraps layers already in memory. Register your own class in
`META-INF/services/io.github.sfesantos.holidays.registry.HolidayDataProvider`
(or `provides ... with` in `module-info.java`) and `CalendarRegistry.load()`
picks it up.

```java
public final class JdbcHolidayProvider implements HolidayDataProvider {

    private final Map<String, Layer> layers;

    public JdbcHolidayProvider(DataSource dataSource) {
        layers = loadLayers(dataSource);   // one SELECT at startup, rows mapped onto Layer.named(...).add(...)
    }

    @Override
    public Set<String> layerIds() {
        return layers.keySet();
    }

    @Override
    public Optional<Layer> layer(String id) {
        return Optional.ofNullable(layers.get(id));
    }
}

var registry = CalendarRegistry.load(new JdbcHolidayProvider(dataSource));   // database first, shipped packs after
```

**`DirectoryDataProvider.of(path)`** reads every `.json` file directly in a
directory; **`ClasspathDataProvider.of(anchor, resources...)`** reads named
resources as `Class.getResourceAsStream` resolves them. Both parse at
construction and fail on a missing file or a duplicate id.

```java
var ops = DirectoryDataProvider.of(Path.of("/etc/holidays"));
var bundled = ClasspathDataProvider.of(MyApp.class, "/holidays/branches.json", "/holidays/exchanges.json");
var registry = CalendarRegistry.of(ops, bundled);
```

### Country packs (`io.github.sfesantos.holidays.data`, artifact `holidays-data`)

**`BundledDataProvider`** is the service that exposes `ES.json` and `BR.json`
from the jar. You rarely name it: `CalendarRegistry.load()` finds it.
**`Spain`** and **`Brazil`** hold the same layers as constants for code-first
calendars, and **`Holidays`** holds the definitions most countries share
(`NEW_YEAR`, `GOOD_FRIDAY`, `EASTER_MONDAY`, `ASCENSION`, `WHIT_MONDAY`,
`CORPUS_CHRISTI`, `LABOUR_DAY`, `ASSUMPTION`, `ALL_SAINTS`, `CHRISTMAS`,
`BOXING_DAY` and more). See [Country packs](#country-packs-and-the-registry).

## Spring

There is no Spring module and none is planned. A `HolidayCalendar` is an
immutable, thread-safe object, so it is an ordinary bean. With `holidays-data`
on the classpath this is the whole integration:

```yaml
# application.yml
holidays:
  calendar: ES-AN-MA
```

```java
@Configuration
class HolidayConfiguration {

    @Bean
    CalendarRegistry calendarRegistry() {
        return CalendarRegistry.load();
    }

    @Bean
    HolidayCalendar holidayCalendar(CalendarRegistry registry, @Value("${holidays.calendar}") String id) {
        return registry.calendar(id);
    }
}

@Service
class SettlementService {

    private final HolidayCalendar calendar;

    SettlementService(HolidayCalendar calendar) {
        this.calendar = calendar;
    }

    LocalDate settlementDate(LocalDate trade) {
        return calendar.addBusinessDays(trade, 2);   // T+2
    }
}
```

An application that serves several regions declares one bean per calendar and
injects them by qualifier, or exposes the registry itself and lets each
service ask for the id it needs:

```java
@Bean
HolidayCalendar madrid(CalendarRegistry registry) {
    return registry.builder("ES").layer(headOfficeClosures).build();
}

@Bean
HolidayCalendar rio(CalendarRegistry registry) {
    return registry.calendar("BR-RJ-RIO");
}

@Service
class PaymentService {

    PaymentService(@Qualifier("madrid") HolidayCalendar madrid, @Qualifier("rio") HolidayCalendar rio) {
        ...
    }
}
```

Nothing here blocks or allocates per call, so the same bean is safe in a
WebFlux handler or any other event loop. Keep the `Clock` a bean too and pass
it to the `*Today` queries, and tests can freeze time without touching the
calendar.

## Performance

Every query runs against a per-year index computed once and cached, so the
hot path is a bit test or a binary search over precomputed arrays. The
`holidays-benchmarks` module measures it with JMH; it is built only with
`-Pbenchmarks` and never published.

```sh
mvn verify -Pbenchmarks
java -jar holidays-benchmarks/target/benchmarks.jar                  # every benchmark
java -jar holidays-benchmarks/target/benchmarks.jar -prof gc QueryBenchmark   # with bytes allocated per call
java -XX:+UseSerialGC -Xmx32m -cp holidays-benchmarks/target/benchmarks.jar \
    io.github.sfesantos.holidays.benchmarks.MemoryFootprint          # heap held by 100 indexed years
```

Measured on the Málaga calendar (14 holidays, three layers) with Temurin
25.0.1 on a Ryzen 9 9950X3D under WSL2, one fork, five iterations of one
second each. The targets are the ones the project set for itself at the start.

| Query | Time | Allocated | Target |
|---|---|---|---|
| `isHoliday(date)` | 1.3 ns | 0 B | < 20 ns, zero allocation |
| `isBusinessDay(date)` | 1.3 ns | 0 B | |
| `holidayOn(date)` | 5.4 ns | 16 B (the `Optional`) | |
| `nextHoliday(date)` | 4.7 ns | 16 B (the `Optional`) | < 100 ns, one allocation |
| `nextBusinessDay(date)` | 6.6 ns | 24 B (the `LocalDate`) | |
| `addBusinessDays(date, 2)` | 10.8 ns | 24 B | |
| `businessDaysBetween` over 10 years | 63 ns | 176 B | < 5 µs |
| `union` of two calendars, `isBusinessDay` | 1.3 ns | 0 B | |
| First touch of a year (build the index) | 2.9 µs | 10.2 KB | < 50 µs |
| `HolidayCalendar.builder().build()` | 0.3 µs | 5.4 KB | |
| Heap for 100 indexed years | 197 KB | 2.0 KB per year | < 500 KB |

A year's index is three bit sets of six 64-bit words (any holiday,
non-working holiday, business day), the holidays observed in the year sorted
by observed date next to an `int[]` of their days, the same holidays listed
by original date for `holidaysIn`, and a seven-entry running count of
business days at each word boundary. `isHoliday` and `isBusinessDay` are one
bit test; `businessDaysBetween` is two lookups in that running count plus two
`Long.bitCount` calls; `holidayOn` and `nextHoliday` are a binary search over
the year's handful of holidays. Nothing is stored per day.

That last point is recent. Up to 0.3.0 the index also kept two arrays with
one slot per day of the year, a `HolidayOccurrence[367]` behind `holidayOn`
and an `int[367]` of business day prefix sums, and together they were about
60% of a year's footprint. Replacing them with the binary search and the
six-entry prefix count changed the numbers like this, on the same machine
and the same run settings:

| | 0.3.0 | `main` |
|---|---|---|
| Heap for 100 indexed years | 490 KB | 197 KB |
| First touch of a year | 3.3 µs, 13.8 KB allocated | 2.9 µs, 10.2 KB allocated |
| `holidayOn(date)` | 1.8 ns | 5.4 ns |
| Every other query | | unchanged |

`holidayOn` is the one query that pays for it, and it stays in single-digit
nanoseconds. For one calendar none of this matters; for an application that
keeps hundreds of calendars warm for decades it is the difference between
tens and hundreds of megabytes.

Numbers from another machine will differ; the point is the order of
magnitude and that the hot queries do not allocate.

## Design decisions

Choices that are not visible in the code and are not going to be revisited
without a reason:

- **Zero dependencies.** Published artifacts depend on `java.base` only; the
  Maven enforcer rule and `ModuleTest` keep it that way. Anything that needs
  another JDK module (`java.sql`, `java.net.http`) goes in an optional module
  of its own.
- **Data is JSON.** It was chosen over a bespoke text format because tooling,
  schemas and bank systems already speak it. A plain text reader stays an open
  issue, to be built only if users ask for something easier to edit by hand.
- **Packs ship one example city per country.** Regions follow ISO 3166-2 and
  ship in full; below that, one city (`ES-AN-MA`, `BR-RJ-RIO`) shows the
  pattern and users add the rest through a provider of their own.
- **No Spring module.** See [Spring](#spring).
- **Layer names are identities.** Adding a holiday a lower layer already
  defines is an error; `override` or `remove` have to be said. It makes a
  pack's intent explicit and catches typos in names.
- **Rules are strict where the calendar is strict.** `weekdayAfter` and
  `weekdayBefore` exclude the base day, `nextHoliday` and `nextBusinessDay`
  exclude the given date; inclusive variants exist under their own names
  rather than behind a flag.
- **Lunisolar calendars with astronomical rules are out for now.** Hijri and
  Hebrew are arithmetic and ship; the Chinese calendar needs tables of new
  moons and solar terms and is deferred until there is a concrete need.

## Thread safety

Builders are not thread-safe. A built `HolidayCalendar` is immutable and may
be shared and queried from any thread, including event loops. Each year's
index is computed on first use and cached; `warmUp(from, until)` precomputes
a range ahead of time.

## License

Apache License 2.0. See [LICENSE](LICENSE).
