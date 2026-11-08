# Holidays

A dependency-free Java library for holiday calendars and business day queries.

You describe holidays as rules (a fixed day, an offset from Easter, the fourth
Thursday of November) and stack them in layers (country, region, province,
city). The library resolves the rules once per year, applies the observance
rule of your region (Sunday to Monday, weekend to Monday, nearest weekday) and
answers questions like "is this a business day?" from precomputed indexes.

- Zero dependencies: only `java.base`.
- Immutable and thread-safe calendars, built once with a builder.
- Queries are O(1) or O(log n) over per-year indexes, computed lazily and cached.
- The core knows nothing about any country. Country data is JSON: write your
  own or add the optional `holidays-data` module and ask a registry for
  `ES-AN-MA`.

Requires Java 25.

## Status

Early. Version 0.2.0, the release on Maven Central, covers rules, layers,
observance, the basic queries, navigation (`nextHoliday`, `addBusinessDays`,
`businessDaysBetween`), `TemporalAdjuster` and `TemporalQuery` integration,
`Clock` overloads and `union` of calendars. Since then `main` has gained
payment date conventions and cut-off times, business hours and half days,
Islamic and Hebrew calendar rules, a JSON data format, a calendar registry
with a provider SPI and the `holidays-data` module with Spain and Brazil;
those sections below describe code that is not yet in a release. The public
API may still change before 1.0. See [TODO.md](TODO.md) for the roadmap.

## Install

```xml
<dependency>
    <groupId>io.github.sfesantos</groupId>
    <artifactId>holidays-core</artifactId>
    <version>0.2.0</version>
</dependency>
```

The country packs are a separate, optional artifact:

```xml
<dependency>
    <groupId>io.github.sfesantos</groupId>
    <artifactId>holidays-data</artifactId>
    <version>0.2.0</version>
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

## Thread safety

Builders are not thread-safe. A built `HolidayCalendar` is immutable and may
be shared and queried from any thread, including event loops. Each year's
index is computed on first use and cached; `warmUp(from, until)` precomputes
a range ahead of time.

## License

Apache License 2.0. See [LICENSE](LICENSE).
