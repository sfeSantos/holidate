# Holidays — biblioteca Java de feriados, dias úteis e fins de semana

Biblioteca Java pura (zero dependências externas, só `java.base`) para definir calendários de feriados por camadas (nacional → regional → provincial → local), com regras de observância configuráveis e consultas rápidas sobre feriados e dias úteis.

## 1. Princípios não negociáveis

- [x] **Zero dependências.** Apenas `java.base` (`java.time`, `java.util`, `java.util.function`, `java.util.concurrent`). Nem Jackson, nem Guava, nem Lombok, nem SLF4J.
- [x] **Performance é prioridade total.** Toda consulta (`isHoliday`, `nextHoliday`, `isBusinessDay`) deve ser O(1) ou O(log n) sobre dados pré-computados por ano. Nunca recalcular regras numa consulta.
- [x] **Imutabilidade.** Calendários, regras e ocorrências são imutáveis e thread-safe. Construção via builder; depois disso, só leitura.
- [x] **Regras, não datas.** Um feriado é `Year -> List<LocalDate>` (zero ou uma data nas regras gregorianas; até duas nas lunares). As datas concretas são derivadas e cacheadas.
- [x] **Core genérico, dados opcionais.** O motor não conhece Espanha nem Málaga. Catálogos de países são módulos separados ou classes utilitárias opcionais.
- [x] **Integração nativa com `java.time`.** Expor `TemporalAdjuster` e `TemporalQuery` para que `date.with(calendar.nextBusinessDayAdjuster())` funcione.
---

## 2. Regras de negócio

### 2.1 Tipos de regra de feriado (`sealed interface HolidayRule`)

- [x] `FixedDate` — dia e mês fixos (25/12, 1/1, 1/11).
- [x] `EasterRelative` — deslocamento em dias relativo ao Domingo de Páscoa (Sexta-feira Santa = −2, Quinta Santa = −3, Segunda de Páscoa = +1, Corpus Christi = +60).
- [x] `NthWeekdayOfMonth` — enésimo dia da semana do mês (4ª quinta de novembro, 1ª segunda de maio). Suportar `n` negativo para "último".
- [x] `WeekdayAfter` / `WeekdayBefore` — primeiro dia da semana X depois/antes de uma data base.
- [x] `Custom` — `Function<Year, Optional<LocalDate>>` para casos que não encaixam.
- [x] `Hijri` — dia do calendário islâmico via `HijrahChronology` (tabelas Umm al-Qura do JDK, anos 1883 a 2173). Pode dar duas datas no mesmo ano gregoriano.
- [x] `Hebrew` — dia do calendário hebraico (`HebrewMonth`, `ADAR` resolve para Adar II em ano bissexto), aritmética própria em `hebrew.HebrewCalendar` validada contra Rosh Hashaná 1939 a 2026.
- [x] Toda regra aceita **intervalo de validade** (`validFrom`, `validUntil`, ambos opcionais, em `Year`). Fora do intervalo, a regra devolve `Optional.empty()`.
- [x] Toda regra tem **nome** e **tipo** (`PUBLIC`, `BANK`, `OBSERVANCE`, `HALF_DAY`) para o usuário filtrar o que conta como "dia não útil".
### 2.2 Cálculo da Páscoa

- [x] Algoritmo anónimo gregoriano (Meeus/Jones/Butcher). Implementado como função pura `GregorianEaster.of(int year)`.
- [x] Preparar ponto de extensão para Páscoa juliana/ortodoxa (`EasterAlgorithm` como interface com duas implementações). Não é prioridade, mas o design não deve impedir.
- [x] Testar contra tabela conhecida (2000–2100).
### 2.3 Observância (`ObservanceRule`)

Regra aplicada **depois** de calcular a data original. Não pertence ao feriado; pertence ao calendário (ou camada).

- [x] `none()` — sem deslocamento.
- [x] `sundayToMonday()` — regra típica de Espanha (comunidades autónomas decidem; Andalucía costuma aplicar).
- [x] `weekendToMonday()` — sábado ou domingo → segunda seguinte (Reino Unido, Austrália).
- [x] `saturdayToFridaySundayToMonday()` — modelo EUA federal.
- [x] `nearestWeekday()` — dia útil mais próximo.
- [x] `custom(UnaryOperator<LocalDate>)`.
- [x] Observância deve ser **composável** e **encadeável**: se o dia de destino também for feriado, avançar de novo (ex.: 25/12 e 26/12 ambos no fim de semana).
- [x] Permitir **override por feriado**: um feriado específico pode declarar `observance(ObservanceRule.none())` para ignorar a regra do calendário.
- [x] `HolidayOccurrence` guarda **data original** e **data observada** separadamente.
### 2.4 Camadas (`Layer`) e composição

- [x] Um `HolidayCalendar` é a composição ordenada de camadas: `Spain → Andalucía → Málaga (província) → Málaga (cidade)`.
- [x] Cada camada pode:
  - `add(rule)` — adicionar feriado.
  - `override(name, rule)` — substituir feriado da camada inferior pelo nome.
  - `remove(name)` — remover feriado herdado (ex.: Catalunha troca nacionais por regionais).
  - definir a sua própria `ObservanceRule` (sobrepõe a da camada abaixo).
- [x] Nomes de feriado são a chave de identidade entre camadas. Documentar isso.
- [x] Uma camada pode declarar o seu **fim de semana** (a mais alta que declarar vence; o do builder é fallback) e o nome da camada que **estende** (`extending("ES")`), usado pelo formato de dados e pelo registry; o builder continua a empilhar na ordem dada.
- [x] Camadas são reutilizáveis: `Layer SPAIN` pode ser partilhada por 17 calendários regionais.
### 2.5 Fim de semana

- [x] `Set<DayOfWeek>` configurável. Default `SATURDAY, SUNDAY`.
- [x] Suportar `FRIDAY, SATURDAY` (Golfo) e `FRIDAY` só, ou `SUNDAY` só, ou vazio.
- [x] Armazenar internamente como `EnumSet` / bitmask de 7 bits.
### 2.6 Consultas públicas (`HolidayCalendar`)

- [x] `boolean isHoliday(LocalDate)`
- [x] `boolean isWeekend(LocalDate)`
- [x] `boolean isBusinessDay(LocalDate)` — não é feriado nem fim de semana.
- [x] `Optional<HolidayOccurrence> holidayOn(LocalDate)`
- [x] `Optional<HolidayOccurrence> nextHoliday(LocalDate)` / `previousHoliday(LocalDate)` — exclusivos, com variantes `nextHolidayInclusive` / `previousHolidayInclusive`. Busca limitada a 100 anos; além disso devolve `Optional.empty()`.
- [x] `OptionalLong daysUntilNextHoliday(LocalDate)` — vazio quando não há próximo feriado (calendário vazio ou regras que já não disparam).
- [x] `List<HolidayOccurrence> holidaysIn(Year)` / `holidaysBetween(LocalDate, LocalDate)` — `from` inclusivo, `to` exclusivo, ordenado por data observada.
- [x] `LocalDate nextBusinessDay(LocalDate)` / `previousBusinessDay(LocalDate)`, mais `nextOrSameBusinessDay` / `previousOrSameBusinessDay`.
- [x] `LocalDate addBusinessDays(LocalDate, int)` — suportar negativos.
- [x] `long businessDaysBetween(LocalDate, LocalDate)` — `from` inclusivo, `to` exclusivo, negativo quando `from > to` (convenção de `ChronoUnit.DAYS.between`).
- [x] `TemporalAdjuster nextBusinessDayAdjuster()` etc., e `TemporalQuery` para `isHoliday`, `isBusinessDay` e `holidayOn`.
- [x] `BusinessDayConvention` (`UNADJUSTED`, `FOLLOWING`, `PRECEDING`, `MODIFIED_FOLLOWING`, `MODIFIED_PRECEDING`) com `adjust(LocalDate, convention)` e `adjuster(convention)`: uma cobrança agendada num feriado ou fim de semana move para o dia útil seguinte ou anterior; as variantes `MODIFIED_*` não saem do mês.
- [x] Versões com `Clock` injetável para "hoje" (`isHolidayToday(Clock)`, `holidayToday`, `nextHoliday(Clock)`, `daysUntilNextHoliday(Clock)`, `isBusinessDayToday`, `nextBusinessDay(Clock)`, `addBusinessDays(Clock, int)`), nunca `LocalDate.now()` hardcoded dentro da lib. Sem sobrecarga sem argumento: o chamador escolhe o relógio.
### 2.7 Exemplo de uso da API (referência de design)

Este é o alvo. Qualquer decisão de implementação que torne este código mais verboso deve ser justificada.

```java
// Camadas reutilizáveis — vivem em holidays-data-es, mas o usuário pode definir as suas
var spain = Layer.named("ES")
    .add(Holidays.NEW_YEAR, Holidays.EPIPHANY, Holidays.GOOD_FRIDAY,
         Holidays.LABOUR_DAY, Holidays.ASSUMPTION, Holidays.NATIONAL_DAY_ES,
         Holidays.ALL_SAINTS, Holidays.CONSTITUTION_ES, Holidays.IMMACULATE,
         Holidays.CHRISTMAS)
    .build();
 
var andalucia = Layer.named("ES-AN")
    .add(Holiday.fixed("Día de Andalucía", Month.FEBRUARY, 28))
    .add(Holiday.easterRelative("Jueves Santo", -3))
    .observance(Observance.sundayToMonday())   // a comunidade decide mover
    .build();
 
var malaga = Layer.named("ES-AN-MA")
    .add(Holiday.fixed("San Ciriaco y Santa Paula", Month.JUNE, 18))
    .add(Holiday.fixed("Virgen de la Victoria", Month.SEPTEMBER, 8))
    .build();
 
var calendar = HolidayCalendar.builder()
    .weekend(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    .layers(spain, andalucia, malaga)          // ordem = precedência
    .build();
 
// Consultas
var today = LocalDate.now(clock);
calendar.isHoliday(today);
calendar.isBusinessDay(today);
calendar.holidayOn(today);                     // Optional<HolidayOccurrence>
calendar.nextHoliday(today);                   // Optional<HolidayOccurrence>
calendar.daysUntilNextHoliday(today);          // OptionalLong
calendar.holidaysIn(Year.of(2027));            // List<HolidayOccurrence>
calendar.addBusinessDays(today, 10);
calendar.businessDaysBetween(today, today.plusMonths(3));
 
// Integração com java.time
var deadline = today.with(calendar.nextBusinessDayAdjuster());
 
// Override e remoção numa camada (ex.: Catalunha)
var catalunya = Layer.named("ES-CT")
    .remove("Constitución")                    // nome herdado da camada ES
    .add(Holiday.fixed("Sant Joan", Month.JUNE, 24))
    .add(Holiday.fixed("Diada", Month.SEPTEMBER, 11))
    .build();
 
// Observância custom e override por feriado
var uk = Layer.named("GB")
    .observance(Observance.weekendToMonday())
    .add(Holidays.CHRISTMAS.withObservance(Observance.none()))
    .build();
 
// Regra custom
var rule = Holiday.custom("Election Day",
    year -> Optional.of(LocalDate.of(year.getValue(), Month.NOVEMBER, 1)
                                 .with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY))
                                 .plusDays(1)))
    .validFrom(Year.of(1845));
```

### 2.8 Horário, fusos e outros calendários

Eram "fora de escopo" até o 0.2.0. Entraram depois, em `main`, sem release próprio. Continua fora: download de dados de fontes externas em runtime (o usuário carrega os ficheiros; a lib não faz HTTP).

- [x] **Horário de corte (cut-off) e fusos horários.** `CutOff.at(LocalTime, ZoneId)` transforma um `Instant` ou `LocalDateTime` na data de registo (dia seguinte quando recebido na hora de corte ou depois); `calendar.adjust(instant, cutOff, convenção)` aplica a convenção a essa data.

- [x] **Horário comercial / meio-dia.** `BusinessHours` no builder (default: dia inteiro), `HALF_DAY` com hora de fecho via `Holiday.closingAt` (default meio-dia), `businessHoursOn(LocalDate)`, `isHalfDay`, `isOpen(LocalDateTime)` e `isOpen(Instant, ZoneId)`. Na união as horas são a interseção dos membros.

- [x] **Calendários não gregorianos.** Regras islâmicas (`Holiday.hijri`) via `HijrahChronology` (`java.base`, tabela Umm al-Qura, anos 1883 a 2173); `datesIn` permite duas datas no mesmo ano gregoriano. Calendário chinês analisado e adiado: é astronómico, não aritmético; se voltar, a via é uma tabela de inícios de mês lunar (1900 a 2100) mais tabela de termos solares para o Qingming.

- [x] **Calendário hebraico.** `Holiday.hebrew(nome, HebrewMonth, dia)`; aritmética própria em `hebrew.HebrewCalendar` (o JDK não traz), validada contra Rosh Hashaná de 1939 a 2026, Pessach, Yom Kippur, Hanucá e Purim.
### 2.9 Country data: text format, registry and composition

Why: a bank present in ten countries deals with hundreds of calendars (branches,
clearing houses, exchanges) that change every year and come from its own
operations teams or vendors, not from a community catalog. One Java class per
country does not scale and turns every yearly update into a release. Holiday
data is data, not code: the core parses a plain text format with zero
dependencies, ships country packs as resource files in that same format, and
lets the user load their own files, directories or database rows through the
same door.

- [x] **JSON format** (`ES.json`, one file per country, regions inside). Same model as the Java API; the JSON Schema ships in the jar at `META-INF/holidays/holidays.schema.json`. Shape (plus `closesAt` on half days and the `hijri`/`hebrew` rule kinds):
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
        { "name": "Thanksgiving",  "rule": { "nth": 4, "weekday": "THURSDAY", "month": "NOVEMBER" } },
        { "name": "Election Day",  "rule": { "weekdayAfter": "MONDAY", "base": "11-01" }, "type": "OBSERVANCE", "until": 2030 }
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
- [x] JSON needs a parser and the core has no dependencies, so `internal.json` gets a hand-written one: objects, arrays, strings with escapes, numbers, booleans, null. About 200 lines, strict (RFC 8259), fails with line and column. No reflection, no generic binding: the reader walks the tree and calls the `Layer`/`Holiday` builders directly.
- [ ] **Plain text alternative** (`.holidays`, `#` comments), kept as an open option because it is far easier to read and edit by hand:
```
layer ES
  weekend          = SATURDAY SUNDAY
  Año Nuevo        = fixed 01-01
  Viernes Santo    = easter -2
  Constitución     = fixed 12-06 from 1978
  Thanksgiving     = nth 4 THURSDAY NOVEMBER        # example of the other rule kinds
  Election Day     = weekday-after MONDAY 11-01 type OBSERVANCE until 2030

layer ES-AN extends ES
  observance       = sunday-to-monday
  Día de Andalucía = fixed 02-28
  Jueves Santo     = easter -3

layer ES-CT extends ES
  remove Constitución
  override Sant Joan = fixed 06-24
```
- [ ] Grammar: `layer <id> [extends <id>]`, then one entry per line: `<name> = <rule> [type <T>] [from <year>] [until <year>] [observance <rule>]`, plus `remove <name>`, `override <name> = <rule> ...`, `weekend = ...`, `observance = ...`. Names may contain spaces; `=` splits name from rule. Rule kinds: `fixed MM-DD`, `easter <offset>`, `nth <n> <DOW> <MONTH>`, `weekday-after <DOW> MM-DD`, `weekday-before <DOW> MM-DD`. `custom` stays Java-only.
- [x] `HolidayFormat.json()` (and `HolidayFormat.text()` if the plain text option goes ahead): `parse(Reader)` / `parse(Path)` / `parse(String)` → `List<Layer>`; `write(List<Layer>)` round-trips so a calendar built in Java can be exported. Hand-written, zero deps, fails fast with line and column (`HolidayFormatException`). `custom` rules and observances cannot be serialised and are rejected by `write`.
- [x] **Registry**: `CalendarRegistry` resolves an id to a `HolidayCalendar` by composing the `extends` chain (`ES-AN-MA` → `ES`, `ES-AN`, `ES-AN-MA`). Ids follow ISO 3166-1/3166-2 where one exists (`ES`, `ES-AN`, `BR`, `BR-RJ`); below that, free text (`ES-AN-MA`, `BR-RJ-RIO`). Calendars are cached by id.
- [x] `HolidayDataProvider` (`ServiceLoader`): `Set<String> layerIds()` + `Optional<Layer> layer(String id)`. Built-in providers: classpath resources (`META-INF/holidays/*.holidays`), a directory on disk. Users write their own (database, vendor feed). Registry asks providers in order; the first that answers wins, so a user's provider overrides the shipped pack.
- [x] **Exception layers**: a user stacks `SANTANDER-ES` (one-off closures, `validBetween(2027, 2027)`) on top of `ES-AN-MA` without touching the base data. Already supported by layers; document the pattern.
- [x] **Composition**: `HolidayCalendar.union(a, b, ...)`: holiday if a holiday in any; business day only if a business day in all. Needed for cross-market settlement (EUR/BRL is a business day in `ES` and `BR`). Implemented as a composed calendar with its own `YearIndex` built by OR-ing the members, so queries stay O(1).
- [x] **Yearly validation**: one test per year and per official source (BOE 2027, BOJA 2027, B3 2027), as `MalagaExampleTest` does. Updating a pack is "edit the file, run the test".
- [x] Java constants (`Holidays.CHRISTMAS`, `Holidays.GOOD_FRIDAY`) stay for the handful of definitions every country shares, and `Layer`/`Holiday` builders stay for users who prefer code. Both options remain first-class; the JSON format is the recommended one for bulk data.
---

## 3. Arquitetura

### 3.1 Módulos / pacotes

```
holidays-core/                      # motor de regras, sem dados de país (estado atual)
  io.github.sfesantos.holidays
    HolidayCalendar                 # fachada pública (interface): builder(), union(...)
    HolidayQueries, BusinessDayQueries   # as consultas, com defaults para Clock, convenções e cut-off
    HolidayCalendarBuilder
    Layer, LayerBuilder
    Holiday (record), YearRange (record)
    HolidayRule (sealed) + records: FixedDate, EasterRelative, NthWeekdayOfMonth, WeekdayAfter, WeekdayBefore, Custom, Hijri, Hebrew
    ObservanceRule (interface funcional) + fábricas estáticas, ObservanceContext
    HolidayOccurrence (record)
    HolidayType (enum)
    Weekend (record sobre máscara de 7 bits)
    BusinessDayConvention (enum), CutOff (record), BusinessHours (record)
  io.github.sfesantos.holidays.easter
    EasterAlgorithm, GregorianEaster
  io.github.sfesantos.holidays.hebrew
    HebrewCalendar, HebrewMonth
  io.github.sfesantos.holidays.internal  # não exportado no module-info
    IndexedCalendar                 # consultas sobre um YearIndex por ano, cache incluído
    CompiledCalendar, UnionCalendar # fornecem o índice de cada ano
    YearIndex, YearIndexBuilder, UnionIndexBuilder
    CalendarCache, LayerResolver, ObservanceChain
 
  io.github.sfesantos.holidays.format    # data formats (section 2.9)
    HolidayFormat                   # json(): parse and write; text() if the plain text option goes ahead
    HolidayFormatException          # line and column in the message
  io.github.sfesantos.holidays.internal.json
    JsonValue, JsonReader, JsonWriter          # hand-written, strict, positions on every node
    JsonHolidayFormat, LayerJsonReader, LayerJsonWriter   # tree <-> Layer, calling the builders
  (internal.ObservanceNames: data names of the built-in observance rules)
  io.github.sfesantos.holidays.registry
    CalendarRegistry                # id -> HolidayCalendar, composes the extends chain, caches
    HolidayDataProvider             # ServiceLoader SPI
    ClasspathDataProvider, DirectoryDataProvider   # JSON resources next to a class; *.json files in a directory

holidays-data/        (opcional, módulo separado; depende só do core)
  META-INF/holidays/ES.json         # one resource file per country, regions inside
  META-INF/holidays/BR.json
  io.github.sfesantos.holidays.data
    BundledDataProvider             # ServiceLoader provider over the two files
    Holidays                        # CHRISTMAS, NEW_YEAR, GOOD_FRIDAY... shared definitions
    Spain, Brazil                   # the pack layers as constants for code-first users (no separate holidays-data-es)
  (the JSON Schema stays in the core jar, META-INF/holidays/holidays.schema.json)
 
holidays-spring-boot-starter/   (opcional, módulo separado — ÚNICO com dependências externas)
  HolidayCalendarAutoConfiguration
  HolidayCalendarProperties           # @ConfigurationProperties("holidays")
  HolidayCalendarCustomizer           # interface funcional para o usuário ajustar o builder
  (ver secção 3.4)
```

- [x] `module-info.java` em cada módulo; exportar só a API pública; `internal` nunca exportado.
- [x] `ServiceLoader<HolidayDataProvider>` para descoberta opcional de packs de dados sem acoplar o core.
- [x] Build: Maven multi-módulo, Java 25, `--release 25`. Plugins: compiler, surefire, jar, enforcer (zero dependências), e só no perfil `release` source, javadoc, gpg e central-publishing.
### 3.2 Fluxo interno

```
Builder → valida → CompiledCalendar (imutável)
                       └─ CalendarCache: ConcurrentHashMap<Integer /*year*/, YearIndex>
                                           └─ computeIfAbsent(year, this::buildIndex)
 
YearIndex (imutável, por ano):
  - long[] / BitSet de 366 bits: isHoliday em O(1) por dayOfYear
  - HolidayOccurrence[] ordenado por data observada: nextHoliday via Arrays.binarySearch em O(log n)
  - Map<LocalDate, HolidayOccurrence> (ou array indexado por dayOfYear) para holidayOn em O(1)
```

- [x] `buildIndex(year)`: para cada regra efetiva (após resolver camadas) → calcula data original → aplica observância → resolve colisões → ordena → congela.
- [x] Resolução de camadas (`add/override/remove`) acontece **uma vez** no `build()`, não por ano. Resultado: lista plana de regras efetivas + observância efetiva.
- [x] `nextHoliday` que cruza a fronteira do ano consulta o índice do ano seguinte (lazy). Busca limitada a 100 anos para evitar loop infinito em calendário vazio; devolver `Optional.empty()`. A navegação de dias úteis usa o mesmo limite e lança `IllegalStateException`.
- [x] `businessDaysBetween` sobre intervalos grandes: contar por ano inteiro usando contagem pré-computada no `YearIndex` (`businessDaysInYear`) e só iterar nas pontas.
### 3.3 Modelo de tipos (Java 25)

- [x] `sealed interface HolidayRule permits FixedDate, EasterRelative, NthWeekdayOfMonth, WeekdayAfter, WeekdayBefore, Custom, Hijri, Hebrew` + `switch` com pattern matching exaustivo em vez de `instanceof` em cascata.
- [x] `record` para tudo o que é valor: `HolidayOccurrence(name, original, observed, type, layer, closesAt)`, `Holiday`, `Weekend`, `YearRange`, `CutOff`, `BusinessHours`.
- [x] Compact constructors nos records para validação (`Objects.requireNonNull`, ranges).
- [x] `ObservanceRule` como `@FunctionalInterface` para permitir lambdas.
- [x] `Optional` só como tipo de retorno; nunca como campo ou parâmetro.
---

### 3.4 Integração com Spring (módulo opcional)

O core continua com zero dependências. Spring entra num starter separado que só agrega conveniência; quem não usa Spring nunca o vê.

- [ ] Módulo `holidays-spring-boot-starter`, Spring Boot 4, dependência `spring-boot-autoconfigure` + `spring-boot-starter` com `optional`/`provided` onde fizer sentido.
- [ ] `HolidayCalendarAutoConfiguration` registada em `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
- [ ] `@ConditionalOnMissingBean(HolidayCalendar.class)` — se o usuário declarar o seu próprio bean, o starter recua.
- [ ] `@ConfigurationProperties("holidays")` com:
```yaml
  holidays:
    weekend: [SATURDAY, SUNDAY]
    layers: [ES, ES-AN, ES-AN-MA]      # ids resolvidos via ServiceLoader<HolidayDataProvider>
    observance: SUNDAY_TO_MONDAY
    warm-up-years: 3                   # pré-computar ano atual ± N
    clock: SYSTEM                      # ou zone id
```
- [ ] `HolidayCalendarCustomizer` (`@FunctionalInterface`, recebe o `HolidayCalendarBuilder`): o usuário regista um bean para adicionar camadas/regras em código sem perder a configuração por propriedades. Aplicar todos os customizers por `@Order`.
- [ ] Registar como bean o `HolidayCalendar` e também um `Clock` se não existir (`@ConditionalOnMissingBean(Clock.class)`).
- [ ] Suporte a **múltiplos calendários** nomeados: `holidays.calendars.es-malaga.layers: [...]`, `holidays.calendars.gb.layers: [...]` → beans qualificados por nome. Útil para apps multi-região.
- [ ] `WebFlux`-friendly: o calendário é imutável e síncrono (nanosegundos), não precisa de `Mono`. Documentar explicitamente que é seguro chamar em qualquer thread, incluindo event loop do Netty/Vert.x.
- [ ] Actuator (opcional, `@ConditionalOnClass`): `HealthIndicator` trivial + `InfoContributor` com camadas carregadas e anos em cache.
- [ ] Metadata para o IDE: `spring-boot-configuration-processor` como dependência de compilação, gera `additional-spring-configuration-metadata.json` para autocomplete no `application.yml`.
- [ ] Testes com `ApplicationContextRunner` (sem subir contexto web): auto-config liga/desliga, propriedades mapeadas, customizers aplicados em ordem, `@ConditionalOnMissingBean` respeitado.
- [ ] Sem `@Scheduled`, sem reload automático, sem cache de Spring — o cache é o do core.
- [ ] Nunca referenciar classes Spring fora deste módulo. Verificar no CI com um teste que o jar do core não tem `import org.springframework`.
  Exemplo de uso para o usuário final:

```java
@Bean
HolidayCalendarCustomizer localHolidays() {
    return builder -> builder.layer(Layer.named("my-office")
        .add(Holiday.fixed("Company Day", Month.MARCH, 15))
        .build());
}
 
@Service
class SettlementService {
    private final HolidayCalendar calendar;
    SettlementService(HolidayCalendar calendar) { this.calendar = calendar; }
 
    LocalDate settlementDate(LocalDate trade) {
        return calendar.addBusinessDays(trade, 2);   // T+2
    }
}
```

## 4. Boas práticas de código

### 4.1 Clean Code

- [ ] **Métodos de 10 a 15 linhas no máximo.** Se passar, extrair. Builders e `switch` de pattern matching podem ser a exceção documentada.
- [ ] Uma responsabilidade por classe (SRP). `YearIndex` só indexa; `GregorianEaster` só calcula Páscoa; `HolidayCalendarBuilder` só constrói.
- [ ] Nomes que dizem o quê, não o como: `nextBusinessDay`, não `findNextNonHolidayNonWeekendDate`.
- [x] Sem flags booleanas em parâmetros (`nextHoliday(date, true)`). Criar dois métodos ou um enum.
- [ ] Máximo 3 parâmetros por método; acima disso, record de parâmetros.
- [ ] Sem comentários que explicam o código; comentários só para o *porquê* (ex.: referência ao BOE, ao algoritmo da Páscoa).
- [x] Sem `null` na API pública. Entrada: `Objects.requireNonNull` com mensagem. Saída: `Optional` ou coleção vazia.
- [x] Fail-fast no `build()`: nomes duplicados na mesma camada, `remove` de nome inexistente, `validFrom > validUntil` → `IllegalArgumentException` / `IllegalStateException` com mensagem clara.
- [x] Exceções próprias só se agregarem valor (`HolidayCalendarException` opcional); nunca checked.
### 4.2 Uso de `var`

- [x] Usar `var` em variáveis locais **quando o tipo é óbvio pelo lado direito** (`var calendar = HolidayCalendar.builder()...build();`, `var easter = GregorianEaster.of(year);`).
- [x] Não usar `var` quando o tipo não é evidente (`var result = service.compute(x);`) ou quando o tipo explícito documenta a intenção (`List<HolidayOccurrence> sorted = ...`).
- [x] Nunca `var` com literais numéricos ambíguos (`var n = 1;` vs `long`).
- [x] Nunca `var` em campos, parâmetros ou retornos (a linguagem não permite, mas fica a regra).
### 4.3 SOLID aplicado

- [ ] **S** — ver 4.1.
- [x] **O** — novas regras de feriado entram como novo `permits` do sealed interface + um case no switch; novas observâncias entram como lambda sem tocar no core.
- [x] **L** — `HolidayCalendar` é interface; qualquer implementação (a compilada, uma composta, uma decorada com logging) deve respeitar o contrato documentado no Javadoc.
- [x] **I** — separar interfaces de leitura: `HolidayQueries`, `BusinessDayQueries`, e `HolidayCalendar extends` ambas. Quem só precisa de dias úteis não vê o resto.
- [x] **D** — `Clock` injetável; `EasterAlgorithm` injetável no builder; nada instancia dependências concretas dentro de métodos.
### 4.4 Imutabilidade e concorrência

- [x] Todos os campos `final`. Coleções expostas via `List.copyOf` / `Collections.unmodifiableList`; nunca devolver o array interno.
- [x] `ConcurrentHashMap.computeIfAbsent` no cache de anos; `YearIndex` imutável após construção → publicação segura garantida.
- [x] Sem `synchronized`, sem locks explícitos.
- [x] Builders **não** são thread-safe (documentar). Calendários construídos **são**.
### 4.5 API pública

- [x] Fluente, estática e descobrível: `HolidayCalendar.builder()`, `Holiday.fixed(...)`, `Holiday.easterRelative(...)`, `Observance.sundayToMonday()`.
- [x] Nomes em inglês na API; nomes de feriados em texto livre (o usuário escolhe o idioma).
- [ ] Javadoc onde acrescenta algo que o nome não diz (semântica, limites, convenções); não em todo método `public`. Exemplo de uso na classe principal.
- [x] Tudo o que não é API é `package-private` ou está em `internal` não exportado.
- [ ] Versionamento semântico; nada em `public` muda sem major bump.
- [x] `toString`, `equals`, `hashCode` consistentes (records dão de graça).
---

## 5. Performance (prioridade total)

### 5.1 Objetivos mensuráveis

- [ ] `isHoliday(date)` com ano já cacheado: < 20 ns, zero alocação.
- [ ] `nextHoliday(date)`: < 100 ns, no máximo uma alocação (o `Optional`).
- [ ] `buildIndex(year)` para calendário de ~20 feriados: < 50 µs.
- [ ] `businessDaysBetween` sobre 10 anos: < 5 µs usando contagens anuais pré-computadas.
- [ ] Cache de 100 anos de um calendário com 20 feriados: < 500 KB de heap.
### 5.2 Técnicas

- [x] **Índice por `dayOfYear`**: arrays de tamanho 367 (índice 1..366) em vez de `Map<LocalDate, ...>` no hot path. `BitSet` ou `long[6]` para `isHoliday`; `HolidayOccurrence[367]` para `holidayOn`.
- [x] **Bitmask para fim de semana**: `int weekendMask` com 7 bits; `isWeekend = (mask & (1 << dow.ordinal())) != 0`.
- [x] **Binary search** sobre `int[] observedDayOfYear` ordenado para `nextHoliday`/`previousHoliday`.
- [x] **Contagens anuais pré-computadas** no `YearIndex`: `businessDaysInYear`, `holidaysInYear`, e array de prefix sums `businessDaysUntil[dayOfYear]` para `businessDaysBetween` em O(1) dentro do mesmo ano.
- [x] **`addBusinessDays(date, n)`** com `n` grande: saltar anos inteiros via `businessDaysInYear` e só iterar na ponta.
- [x] **Lazy por ano** — nunca pré-computar intervalos que o usuário não pediu. Oferecer `warmUp(Year from, Year to)` opcional.
- [x] **Sem Streams no hot path.** Streams só no `build()` e em métodos que já devolvem listas. Loops `for` indexados nas consultas.
- [ ] **Sem boxing**: `int year` como chave do cache (`ConcurrentHashMap<Integer, ...>` boxa; avaliar array esparso ou `Int2ObjectMap` caseiro com open addressing se o benchmark justificar).
- [x] **Evitar `LocalDate` intermédios** nas consultas quando `dayOfYear`/`year` chegam.
- [x] **Resolução de camadas no `build()`**, nunca na consulta (ver 3.2).
- [x] **Observância encadeada** limitada a 7 iterações (uma semana) para garantir terminação.
- [x] **Sem reflexão, sem proxies, sem `String.format` em caminhos quentes.**
- [ ] Medir tudo com **JMH** (dependência só de teste/benchmark, fora do artefacto publicado) antes e depois de cada otimização. Não otimizar sem número.
- [ ] Testar com `-XX:+UseSerialGC` e heap pequeno para confirmar que o uso de memória é previsível.
---

## 6. Testes

- [x] JUnit 5 (dependência só de teste). Sem Mockito — tudo é valor imutável, não precisa de mocks.
- [x] Páscoa: tabela 2000–2100 contra fonte oficial.
- [x] Cada tipo de regra: casos de ano bissexto, 29/02, limites de `validFrom/validUntil`.
- [x] Observância: colisões (25/12 + 26/12 no fim de semana), encadeamento, override por feriado, terminação em 7 passos.
- [x] Camadas: `add/override/remove`, ordem de precedência, remoção de nome inexistente falha no build.
- [x] Consultas: fronteira de ano (`nextHoliday` em 30/12), calendário vazio devolve `Optional.empty()`, `addBusinessDays` negativo, `businessDaysBetween` com `a > b`.
- [x] Fim de semana sexta-sábado.
- [x] Concorrência: 100 threads a consultar o mesmo calendário em anos diferentes sem erro e com um único `buildIndex` por ano (contador atómico no teste).
- [x] Dados de Espanha/Andalucía/Málaga validados contra o calendário laboral do BOE e BOJA de pelo menos 3 anos (2025, 2026, 2027). Testes nomeados pela fonte.
- [ ] Cobertura mínima 90% no core (JaCoCo, só em CI).
- [ ] Benchmarks JMH num módulo `holidays-benchmarks` fora da publicação.
---

## 7. Roadmap

### v0.1 — core
- [x] `module-info`, estrutura multi-módulo, CI (build + testes).
- [x] `GregorianEaster`.
- [x] `HolidayRule` sealed + 5 implementações + `validFrom/validUntil`.
- [x] `ObservanceRule` + fábricas + encadeamento.
- [x] `Layer` + `add/override/remove`.
- [x] `HolidayCalendarBuilder` com validação fail-fast.
- [x] `YearIndex` + `CalendarCache`.
- [x] Consultas básicas: `isHoliday`, `isWeekend`, `isBusinessDay`, `holidayOn`, `holidaysIn`.
### v0.2 — navegação
- [x] `nextHoliday`, `previousHoliday`, `daysUntilNextHoliday`.
- [x] `nextBusinessDay`, `previousBusinessDay`, `addBusinessDays`, `businessDaysBetween` com prefix sums.
- [x] `TemporalAdjuster` / `TemporalQuery`.
- [x] `Clock` injetável.
- [x] `HolidayCalendar.union(...)` for cross-market calendars (section 2.9).
- [x] `BusinessDayConvention` para datas de pagamento (entrou em `main` depois do 0.2.0, sem release próprio).
- [x] Seção 2.8: `CutOff`, `BusinessHours` e meio-dia, regras `hijri` e `hebrew` (em `main` depois do 0.2.0, sem release próprio).
### v0.3 — data format and registry (section 2.9)
- [x] `HolidayFormat.json()` parser and writer over a hand-written `JsonReader`/`JsonWriter`, round-trip tests, JSON Schema published, error messages with line and column.
- [x] `HolidayDataProvider` SPI + `CalendarRegistry` resolving the `extends` chain, classpath and directory providers.
- [x] `holidays-data`: `ES.json` (Spain, Andalucía, Málaga, Catalunya) and `BR.json` (Brazil, RJ, Rio) validated against BOE/BOJA and the Brazilian federal/state laws for 2025, 2026 and 2027.
- [x] Java constants for shared definitions (`Holidays.CHRISTMAS`...) in `holidays-data`. The code-first alternative is `Spain.NATIONAL`, `Spain.MALAGA`, `Brazil.RIO_DE_JANEIRO_CITY`... constants that read the pack layers, so there is no separate `holidays-data-es` module to keep in sync.
- [x] Document the exception-layer pattern and the registry in the README.
### v0.4 — Spring
- [ ] `holidays-spring-boot-starter` com auto-config, properties, customizer e múltiplos calendários.
- [ ] Testes com `ApplicationContextRunner`.
- [ ] Teste de CI que garante zero `org.springframework` no jar do core.
### v1.0 — estabilização
- [ ] Benchmarks JMH publicados no README.
- [ ] Revisão de API pública (nada muda depois).
- [ ] Publicação em Maven Central.
### Depois
- [ ] Páscoa juliana/ortodoxa.
- [x] Meio-dia / `HALF_DAY` com semântica real: `BusinessHours` no builder, `Holiday.closingAt`, `businessHoursOn`, `isHalfDay`, `isOpen`.
- [ ] Mais packs de países (JSON files; community contributions are data edits plus a yearly test).
- [ ] Plain text `.holidays` format as a second reader, if users ask for something easier to edit by hand.
- [ ] Loading packs from a database or vendor feed as reference `HolidayDataProvider` implementations.
---

## 8. Decisões em aberto

- [x] `nextHoliday(date)` é inclusivo ou exclusivo? Exclusivo, com `nextHolidayInclusive` e `previousHolidayInclusive`.
- [x] Data format: JSON (interoperable, schema, tooling, bank systems already speak it; costs a hand-written parser and is verbose to edit by hand) vs own plain text syntax (easy to read and edit, no tooling, no schema) vs both. Decided: JSON first (shipped in v0.3); the plain text reader stays in "Depois" until users ask for it.
- [x] Registry ids: should the shipped pack ship city ids (`ES-AN-MA`) or stop at ISO 3166-2 and leave cities to users? Decided: ship one city per country as an example (`ES-AN-MA`, `BR-RJ-RIO`); the README shows how to add the rest with a provider of your own.
- [x] Feriados de tipo `OBSERVANCE` (não folga) contam ou não em `isBusinessDay`? (sugestão: não; só `PUBLIC` e `BANK`, configurável no builder)
- [x] Chave de identidade entre camadas: `String name` ou um `HolidayId` tipado? (sugestão: começar com `String`, trocar se doer)
- [x] Nome do groupId / namespace do projeto: `io.github.sfesantos`, pacote `io.github.sfesantos.holidays`.
- [x] Maven vs Gradle: Maven.