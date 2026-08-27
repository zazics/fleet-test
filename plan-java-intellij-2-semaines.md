# Plan d'entraînement — Java 8+ / Spring / Reactive Streams + IntelliJ IDEA
### 2 semaines · remise en jambes ciblée mission D'Ieteren

> **Hypothèses** : vous avez une bonne base Java (POO, collections, JDBC/JPA de l'époque), vous disposez d'une machine avec Docker, et vous pouvez y consacrer ~4h/jour ouvré + un peu de week-end. Angular est volontairement exclu.
>
> **Découpage type d'une journée (4h)**
> - 30 min — lecture / doc ciblée
> - 2h30 — exercices de code
> - 30 min — *drill* IntelliJ (raccourcis, navigation, refactoring)
> - 30 min — notes personnelles dans un fichier `NOTES.md` du repo (mémoire active = rétention)

---

## Sommaire

1. [Préparation (J0, ~1h30)](#j0)
2. [Fil rouge : le projet `fleet`](#fil-rouge)
3. [Semaine 1 — Java moderne, tests, Spring, DB](#semaine-1)
4. [Week-end 1 — Git, Jenkins, JIRA](#weekend-1)
5. [Semaine 2 — Reactive Streams, WebFlux, microservices, perf](#semaine-2)
6. [Week-end 2 — Finalisation & revue](#weekend-2)
7. [Cheat sheet IntelliJ](#cheatsheet)
8. [Checklist « prêt pour la mission »](#checklist)
9. [Auto-évaluation : 30 questions type entretien](#questions)
10. [Ressources](#ressources)

---

<a name="j0"></a>
## 1. Préparation — J0 (~1h30)

### Outils à installer

| Outil | Version / choix | Remarque |
|---|---|---|
| JDK | **Temurin 21 LTS** (+ garder un JDK 17 sous la main) | Java 21 = records, pattern matching, virtual threads. La mission dit « Java 8+ », le marché est en 17/21. |
| IntelliJ IDEA | **Ultimate en trial 30 jours**, démarré le J1 | Le trial couvre vos 2 semaines et débloque Spring, Database, HTTP Client, Profiler. Community fonctionne mais vous perdez 40% de la valeur du plan. |
| Docker Desktop | dernière | Testcontainers, Oracle XE, DB2, Kafka |
| Git | dernière | + un compte GitHub perso pour le repo du fil rouge |
| Maven | inutile d'installer : on utilise le **wrapper** (`mvnw`) | mais sachez lancer `mvn -v` |
| httpie / curl | optionnel | le HTTP Client d'IntelliJ suffit |

### Configuration IntelliJ à faire tout de suite

**Plugins à installer** (`Ctrl+Alt+S` → Plugins) :
- **IDE Features Trainer** (« Learn IntelliJ IDEA ») — *indispensable pour vous* : tutoriel interactif intégré
- **Key Promoter X** — affiche le raccourci à chaque fois que vous cliquez à la souris. Le meilleur outil pour apprendre les raccourcis
- **SonarQube for IDE** (ex-SonarLint) — qualité de code en temps réel
- **Maven Helper** — analyse des conflits de dépendances
- **JPA Buddy** — génération d'entités, migrations Flyway/Liquibase
- **.ignore**, **Docker**, **Rainbow Brackets** (confort)

**Réglages** (`Ctrl+Alt+S`) :
- `Editor → General → Auto Import` : cocher *Add unambiguous imports on the fly* et *Optimize imports on the fly*
- `Editor → File Encodings` : tout en **UTF-8**
- `Build → Build Tools → Maven → Runner` : décocher *Delegate IDE build to Maven* (build IntelliJ plus rapide) — mais savoir le réactiver si un projet a des plugins Maven exotiques
- `Editor → Inlay Hints` : activer les *parameter hints*
- `Keymap` : gardez le keymap par défaut (Windows/Linux ou macOS) — c'est celui que vous retrouverez en mission
- `Editor → Code Style → Java` : importez le style de l'équipe plus tard ; pour l'instant, activez `.editorconfig`

### Créer le repo

```bash
mkdir fleet && cd fleet
git init
# on créera le projet Maven au J1 depuis IntelliJ
```

---

<a name="fil-rouge"></a>
## 2. Fil rouge : le projet `fleet`

Un seul projet, enrichi chaque jour. Domaine volontairement proche de celui d'un importateur automobile : **gestion d'une flotte de véhicules, de concessions et de commandes**.

**Architecture cible en fin de plan** (repo multi-modules Maven) :

```
fleet/
├── pom.xml                      (parent, <packaging>pom</packaging>)
├── fleet-domain/                J1-J3  : modèle métier pur, zéro framework
├── fleet-catalog-service/       J4-J5  : Spring Boot MVC + JPA + Oracle
├── fleet-telemetry-service/     J6-J8  : Spring WebFlux + Reactor + R2DBC
├── fleet-gateway/               J9     : agrégation, Resilience4j, sécurité JWT
├── fleet-perf/                  J10    : benchmarks JMH
├── Jenkinsfile                  WE1
└── docker-compose.yml
```

**Entités principales** : `Vehicle` (VIN, marque, modèle, motorisation, km, statut), `Dealer` (concession), `Order`, `TelemetryEvent` (position, conso, code défaut).

---

<a name="semaine-1"></a>
## 3. SEMAINE 1

---

### J1 — IntelliJ, Maven, et le Java moderne (8 → 21)

**Objectifs** : être capable de créer/naviguer un projet Maven multi-modules sans souris, et écrire du Java « 2026 » et non du Java 2015.

#### Théorie (30 min)
- Ce qui a changé depuis Java 8 : `var` (10), `switch` expression (14), **`record`** (16), **`sealed`** (17), **pattern matching for switch** (21), text blocks (15), `Stream.toList()`, `List.of()/Map.of()`, `HttpClient` (11), **virtual threads** (21).
- Maven : cycle de vie (`validate → compile → test → package → verify → install → deploy`), `dependencyManagement` vs `dependencies`, scopes (`compile/provided/runtime/test`), BOM, `<parent>`, plugin `maven-surefire` (tests unitaires) vs `maven-failsafe` (tests d'intégration).

#### Exercices

**1.1 — Créer le projet multi-modules**
- `File → New → Project → Maven`, groupId `be.dieteren.fleet`, JDK 21.
- Ajoutez un module `fleet-domain` (`File → New → Module`).
- Dans le POM parent : `<packaging>pom</packaging>`, `<properties><maven.compiler.release>21</maven.compiler.release></properties>`, un `<dependencyManagement>` avec le BOM JUnit 5.
- **Critère de réussite** : `./mvnw clean verify` passe en vert depuis le terminal *et* depuis la fenêtre Maven d'IntelliJ (`Alt+Insert` ou l'onglet Maven à droite).

**1.2 — Value objects avec `record`**
```java
public record Vin(String value) {
    private static final Pattern PATTERN = Pattern.compile("[A-HJ-NPR-Z0-9]{17}");
    public Vin {                       // compact constructor
        Objects.requireNonNull(value, "vin");
        if (!PATTERN.matcher(value).matches())
            throw new IllegalArgumentException("VIN invalide: " + value);
    }
    public String worldManufacturerIdentifier() { return value.substring(0, 3); }
}
```
À faire vous-même : `Vin`, `Mileage` (km, non négatif, additionnable), `Money` (montant + devise, refus d'additionner EUR + USD).

**1.3 — Hiérarchie scellée + pattern matching**
```java
public sealed interface FleetEvent
        permits VehicleRegistered, VehicleSold, MileageUpdated, VehicleScrapped {
    Vin vin();
    Instant occurredAt();
}
```
Écrivez un `FleetEventDescriber` qui produit un libellé métier avec un `switch` **exhaustif** (sans `default`) et du *record pattern* :
```java
String describe(FleetEvent e) {
    return switch (e) {
        case VehicleRegistered(Vin vin, var at, var dealer) ->
                "Immatriculé chez %s le %s".formatted(dealer, at);
        case MileageUpdated(var vin, var at, Mileage km) when km.value() > 200_000 ->
                "Kilométrage élevé: " + km;
        case MileageUpdated(var vin, var at, var km) -> "Km mis à jour: " + km;
        case VehicleSold s   -> "Vendu";
        case VehicleScrapped s -> "Détruit";
    };
}
```
- **Le vrai exercice** : supprimez `default`, ajoutez un 5ᵉ type dans le `permits`, et constatez que **le compilateur casse le switch**. C'est tout l'intérêt des types scellés.

**1.4 — Modernisation guidée**
Écrivez volontairement une classe « à l'ancienne » (getters/setters, `equals` manuel, boucles `for` indexées, `StringBuilder`, `if/else if` en cascade) puis laissez IntelliJ vous proposer les modernisations via `Alt+Enter` sur chaque avertissement. Objectif : voir combien l'IDE connaît le langage mieux que vous.

#### Drill IntelliJ (30 min)
Lancez **Help → Learn IDE Features → Onboarding tour**, puis maîtrisez ces 12 raccourcis (Windows/Linux · macOS) :

| Action | Win/Linux | macOS |
|---|---|---|
| Search Everywhere | `Shift Shift` | `Shift Shift` |
| Find Action (« que fait cet IDE ? ») | `Ctrl+Shift+A` | `⇧⌘A` |
| **Show Intention Actions / quick fix** | `Alt+Enter` | `⌥⏎` |
| Aller à la classe | `Ctrl+N` | `⌘O` |
| Aller à la déclaration | `Ctrl+B` | `⌘B` |
| Retour arrière navigation | `Ctrl+Alt+←` | `⌘[` |
| Fichiers récents | `Ctrl+E` | `⌘E` |
| Générer (constructeur, getters…) | `Alt+Insert` | `⌘N` |
| Reformater le code | `Ctrl+Alt+L` | `⌥⌘L` |
| Étendre la sélection | `Ctrl+W` | `⌥↑` |
| Renommer | `Shift+F6` | `⇧F6` |
| Run / Debug contextuel | `Ctrl+Shift+F10` | `⌃⇧R` |

> **Règle du jour** : débranchez mentalement la souris. Quand Key Promoter X vous prend en flagrant délit, refaites l'action au clavier.

---

### J2 — Streams, Collectors, Optional (et le Stream Debugger)

**Objectifs** : écrire n'importe quelle agrégation en Streams sans réfléchir, et savoir quand *ne pas* les utiliser.

#### Théorie (30 min)
- Pipeline = source → opérations intermédiaires (lazy) → opération terminale.
- `map` vs `flatMap` vs `mapMulti`.
- `Collectors` : `groupingBy` (avec downstream !), `partitioningBy`, `toMap` (attention aux clés dupliquées), `joining`, `summarizingInt`, `teeing`, `flatMapping`, `filtering`, `collectingAndThen`.
- `Optional` : c'est un **type de retour**, pas un champ ni un paramètre. Jamais `.get()`, jamais `isPresent()+get()`.
- Streams parallèles : utiles seulement si (grand volume) ∧ (source splittable) ∧ (opération CPU-bound) ∧ (pas d'état partagé). Sinon c'est plus lent.

#### Exercices

**2.1 — Jeu de données**
Générez 50 000 véhicules aléatoires (`Faker` ou un générateur maison) et écrivez-les dans un CSV. Chargez-les en `List<Vehicle>`.

**2.2 — Les 15 agrégations** (une méthode par question, testée)
1. Liste des marques distinctes, triées.
2. Kilométrage moyen par marque → `Map<String, Double>`.
3. Nombre de véhicules par (marque, motorisation) → `Map<String, Map<Fuel, Long>>`.
4. Top 3 des concessions par nombre de véhicules.
5. `Map<Vin, Vehicle>` — gérez explicitement le cas de VIN dupliqué avec le merge function.
6. Véhicules groupés par tranche d'âge (0-2 ans, 3-5, 6+) — `groupingBy` avec fonction de classification calculée.
7. Séparer électriques / thermiques (`partitioningBy`) avec, pour chaque groupe, le kilométrage total (downstream `summingLong`).
8. Chaîne « BMW (12), Audi (8), VW (30) » triée par count décroissant → `joining`.
9. Statistiques complètes (min/max/moyenne/somme) du kilométrage → `summarizingLong`.
10. Le véhicule le plus ancien par concession → `groupingBy` + `minBy` + `collectingAndThen(…, Optional::orElseThrow)`.
11. Toutes les options de tous les véhicules, distinctes → `flatMap`.
12. En une seule passe : moyenne **et** compte des véhicules > 100 000 km → `Collectors.teeing`.
13. Un `Collector` **custom** : accumuler dans une `ImmutableFleetSummary`.
14. Les 5 premiers VIN correspondant à un préfixe, en `limit` sur un `Stream` infini (`Stream.iterate` / `generate`) — vérifiez la lazyness avec un `peek(System.out::println)`.
15. `IntStream.range` + `mapToObj` pour numéroter les résultats.

**2.3 — Optional**
Refactorez un service qui retourne `null` :
```java
// Avant
Vehicle v = repo.find(vin);
if (v != null && v.getDealer() != null && v.getDealer().getAddress() != null)
    return v.getDealer().getAddress().getCity().toUpperCase();
return "INCONNU";

// Après : à vous. Indice : Optional.map enchaîné + orElse
```
Puis : utilisez `Optional.stream()` pour aplatir une `List<Optional<Vehicle>>`.

**2.4 — Mesure**
Comparez `stream()` vs `parallelStream()` vs boucle `for` sur vos 50 000 véhicules pour l'exercice n°2. Notez les temps. Refaites avec 50 éléments. **Écrivez la conclusion dans `NOTES.md`.**

#### Drill IntelliJ (30 min)
- **Stream Debugger** : posez un breakpoint sur une chaîne de streams, `Shift+F9`, puis dans la fenêtre Debug cliquez **« Trace Current Stream Chain »**. Vous voyez chaque élément passer d'étape en étape. C'est la killer feature d'IntelliJ pour les streams — et pour Reactor plus tard.
- **Postfix completion** : tapez `vehicles.stream().filter(...)` puis `.var`, `.nn`, `.for`, `.sout`, `.return`. Essayez `list.for` + Tab.
- **Live templates** : `psvm`, `sout`, `iter`, `fori`, `ifn`. Créez le vôtre : `Ctrl+Alt+S → Editor → Live Templates` → un template `logd` qui insère `log.debug("$VAR$ = {}", $VAR$);`.
- `Ctrl+Alt+M` (Extract Method), `Ctrl+Alt+V` (Extract Variable), `Ctrl+Alt+P` (Extract Parameter) sur vos longues chaînes.

---

### J3 — Tests, debugging et refactoring sous filet

**Objectifs** : écrire des tests que vous seriez fier de mettre en revue, et savoir naviguer dans un code inconnu avec le debugger (exactement ce qu'on vous demandera en bugfix).

#### Théorie (30 min)
- JUnit 5 : `@Test`, `@ParameterizedTest` (+ `@CsvSource`, `@MethodSource`, `@EnumSource`), `@Nested`, `@DisplayName`, `assertAll`, `assertThrows`, `@BeforeEach` vs `@BeforeAll`, `@Tag`.
- AssertJ : `assertThat(x).isEqualTo()`, `.extracting()`, `.containsExactlyInAnyOrder()`, `.satisfies()`, `.usingRecursiveComparison()`.
- Mockito : `@ExtendWith(MockitoExtension.class)`, `@Mock`/`@InjectMocks`, `when/thenReturn`, `thenThrow`, `verify(…, times(2))`, `ArgumentCaptor`, `@Spy`, `mockStatic`. **Ne jamais mocker ce qu'on ne possède pas** (mockez vos interfaces, pas la lib tierce).
- Pyramide de tests : beaucoup d'unitaires rapides, quelques tests d'intégration, très peu d'end-to-end.

#### Exercices

**3.1 — Le code legacy**
Écrivez (oui, écrivez volontairement) une classe horrible de ~80 lignes : `OrderPriceCalculator` avec 6 paramètres, des `if/else` imbriqués sur 4 niveaux, des nombres magiques, une remise selon le type de client, la motorisation, l'ancienneté du modèle et une promotion saisonnière.

**3.2 — Caractérisation**
Avant de toucher au code : écrivez des **tests de caractérisation** qui figent le comportement actuel, y compris les bizarreries. Visez 100% de couverture des branches (`Run with Coverage`, `Ctrl+Alt+F6`).

**3.3 — Refactoring**
Sous filet des tests, appliquez dans cet ordre, **en utilisant les refactorings automatiques d'IntelliJ** :
1. `Ctrl+Alt+V` — extraire les nombres magiques en constantes nommées
2. `Ctrl+Alt+M` — extraire chaque bloc `if` en méthode intention-révélante
3. *Introduce Parameter Object* (`Ctrl+Alt+Shift+T` → menu Refactor This) — regrouper les 6 paramètres
4. *Replace conditional with polymorphism* — une `sealed interface DiscountRule` + implémentations, puis un `switch` pattern matching ou une `List<DiscountRule>`
5. Relancez les tests **après chaque étape**. Ils doivent rester verts sans être modifiés.

**3.4 — Tests paramétrés**
Convertissez 8 tests quasi identiques en un seul `@ParameterizedTest` avec `@CsvSource`. Puis un `@MethodSource` qui fournit des objets `Arguments`.

**3.5 — Mockito**
`VehicleService` dépend de `VehicleRepository` + `AuditPublisher`. Testez :
- le cas nominal (`when(...).thenReturn(...)`)
- le cas « non trouvé » (`assertThrows`)
- que l'événement d'audit publié contient bien le bon VIN → `ArgumentCaptor<FleetEvent>`
- que le repository n'est appelé **qu'une fois** → `verify(repo, times(1))`

#### Drill IntelliJ — le debugger (30 min)
Sur le code legacy :
- Breakpoint **conditionnel** : clic droit sur le point rouge → `mileage > 150000`
- **Evaluate Expression** (`Alt+F8`) : évaluez une expression arbitraire, modifiez une variable à chaud
- **Watches**, **Step Over `F8` / Step Into `F7` / Force Step Into `Alt+Shift+F7` / Step Out `Shift+F8`**
- **Run to Cursor** (`Alt+F9`)
- **Drop Frame** — rejouer la méthode courante depuis le début, magique en bugfix
- **Exception breakpoint** : `Ctrl+Shift+F8` → `Java Exception Breakpoints` → `NullPointerException`
- **Mute breakpoints**, breakpoint « logging » (ne suspend pas, log une expression)
- `Ctrl+Shift+T` : naviguer/créer le test d'une classe. `Alt+F7` : Find Usages. `Ctrl+F12` : structure du fichier. `Ctrl+H` : hiérarchie de types.

---

### J4 — Spring Boot 3 : DI, configuration, REST, tests web

**Objectifs** : reconstruire vos réflexes Spring modernes (le monde des XML et des `@Autowired` sur champs est mort).

#### Théorie (30 min)
- Auto-configuration, starters, `@SpringBootApplication` = `@Configuration + @EnableAutoConfiguration + @ComponentScan`.
- **Injection par constructeur uniquement** (immutabilité, testabilité, détection des dépendances circulaires).
- Scopes, `@Bean` vs stéréotypes, `@Conditional…`, `@Profile`, `@ConfigurationProperties` (typé, validé) > `@Value`.
- `application.yml`, hiérarchie de configuration, variables d'environnement, `spring.config.import`.
- REST : `@RestController`, DTO ≠ entité, `@Valid` + Bean Validation, `@RestControllerAdvice` + **`ProblemDetail` (RFC 7807)**, codes HTTP corrects.
- Actuator, springdoc-openapi.

#### Exercices

**4.1 — `fleet-catalog-service`**
Créez le module avec le **Spring Initializr intégré à IntelliJ** (`File → New → Module → Spring Boot`) : Web, Validation, Actuator, Lombok (optionnel), DevTools.

**4.2 — API CRUD**
```
GET    /api/v1/vehicles?brand=BMW&page=0&size=20   → page de VehicleResponse
GET    /api/v1/vehicles/{vin}                      → 200 / 404
POST   /api/v1/vehicles                            → 201 + Location
PATCH  /api/v1/vehicles/{vin}/mileage              → 200 / 409 si régression du km
DELETE /api/v1/vehicles/{vin}                      → 204
```
Contraintes :
- DTO d'entrée avec `@NotBlank`, `@Positive`, contrainte custom `@ValidVin` (écrivez le `ConstraintValidator`)
- Mapping DTO ↔ domaine dans un mapper dédié (à la main, ou MapStruct — regardez le code généré dans `target/generated-sources`)
- `@RestControllerAdvice` renvoyant un `ProblemDetail` avec `type`, `title`, `detail`, et une liste d'erreurs de champ

**4.3 — Configuration typée**
```java
@ConfigurationProperties("fleet.catalog")
@Validated
public record CatalogProperties(@NotNull Duration cacheTtl,
                                @Positive int maxPageSize,
                                Set<String> allowedBrands) {}
```
Trois profils : `local`, `test`, `prod`, avec des valeurs différentes. Lancez avec `--spring.profiles.active=prod` et vérifiez via `/actuator/env`.

**4.4 — Tests web**
- `@WebMvcTest(VehicleController.class)` + `MockMvc` + `@MockitoBean` sur le service : testez 201, 400 (payload invalide, vérifiez le corps ProblemDetail), 404, 409.
- Un `@SpringBootTest(webEnvironment = RANDOM_PORT)` avec `TestRestTemplate` pour le happy path complet.
- Vérifiez le JSON avec `jsonPath` ou JSONAssert.

#### Drill IntelliJ (30 min)
- **HTTP Client** : créez `src/test/http/vehicles.http`
  ```http
  ### Créer un véhicule
  POST http://localhost:8080/api/v1/vehicles
  Content-Type: application/json

  { "vin": "WVWZZZ1KZAW000001", "brand": "VW", "mileage": 12000 }

  > {% client.global.set("vin", response.body.vin); %}

  ### Relire
  GET http://localhost:8080/api/v1/vehicles/{{vin}}
  ```
  Gérez les environnements avec `http-client.env.json` (local / prod).
- **Fenêtre Spring** (`View → Tool Windows → Spring`) : voir les beans, le graphe de dépendances, les endpoints (onglet **Endpoints**).
- **Run Configurations** : créez-en une avec le profil `local` en variable d'environnement, une autre en mode debug, sauvegardez-les dans le projet (`Store as project file`).
- `Ctrl+Shift+F` (Find in Files) et `Ctrl+Shift+R` (Replace in Files) avec regex et scope.
- Navigation Spring : `Ctrl+B` sur une clé de `application.yml` vous emmène au `@ConfigurationProperties`.

---

### J5 — Persistance : JPA, SQL, Oracle & DB2

**Objectifs** : être opérationnel sur la partie « requêtes DB (Oracle, SQL, DB2) » de la description, et savoir diagnostiquer les problèmes de perf classiques.

#### Théorie (45 min)
- JPA : cycle de vie d'une entité (transient/managed/detached/removed), `EntityManager`, dirty checking, `flush`, cache de 1er niveau.
- **Le problème N+1**, et ses trois remèdes : `JOIN FETCH`, `@EntityGraph`, `@BatchSize`.
- `FetchType.LAZY` par défaut partout, `LazyInitializationException`, DTO projections (interface-based ou constructeur JPQL).
- `@Transactional` : propagation (`REQUIRED`, `REQUIRES_NEW`), `readOnly = true`, **le piège de l'auto-invocation** (appel interne → pas de proxy → pas de transaction).
- Verrouillage optimiste (`@Version`) vs pessimiste.
- Flyway : migrations versionnées, immuables, une par changement.
- SQL avancé : `WITH` (CTE), fonctions de fenêtrage (`ROW_NUMBER`, `RANK`, `LAG/LEAD`, `SUM() OVER (PARTITION BY …)`), `MERGE`, index composites et *leftmost prefix*, plans d'exécution.
- **Spécificités à connaître** :

| | Oracle | DB2 | PostgreSQL/standard |
|---|---|---|---|
| Pagination | `OFFSET n ROWS FETCH NEXT m ROWS ONLY` (12c+), sinon `ROWNUM` | `FETCH FIRST n ROWS ONLY` | `LIMIT/OFFSET` |
| Identifiants | `SEQUENCE` + `IDENTITY` (12c+) | `GENERATED ALWAYS AS IDENTITY` | `SERIAL`/`IDENTITY` |
| Table « vide » | `DUAL` | `SYSIBM.SYSDUMMY1` | (pas nécessaire) |
| Null coalescing | `NVL`, `NVL2` | `COALESCE`, `VALUE` | `COALESCE` |
| Concat | `||` ou `CONCAT` | `||` ou `CONCAT` | `||` |
| Chaîne vide | `''` **est** `NULL` (!) | `''` ≠ `NULL` | `''` ≠ `NULL` |
| Dialecte Hibernate | `OracleDialect` | `DB2Dialect` | `PostgreSQLDialect` |

#### Exercices

**5.1 — Base Oracle en local**
```yaml
# docker-compose.yml
services:
  oracle:
    image: gvenzl/oracle-free:slim-faststart
    environment: { ORACLE_PASSWORD: fleet }
    ports: [ "1521:1521" ]
  db2:
    image: icr.io/db2_community/db2
    privileged: true
    environment: { LICENSE: accept, DB2INST1_PASSWORD: fleet, DBNAME: fleetdb }
    ports: [ "50000:50000" ]
```
Connectez-vous depuis la **fenêtre Database d'IntelliJ** (`View → Tool Windows → Database` → `+` → Data Source → Oracle). Téléchargez le driver quand l'IDE le propose.

**5.2 — Flyway + schéma**
`V1__create_schema.sql` : tables `dealer`, `vehicle`, `vehicle_option`, `order`. Clés étrangères, index sur `vehicle(dealer_id, brand)`, contrainte d'unicité sur le VIN.
`V2__seed_data.sql` : 200 concessions, 100 000 véhicules (générés en SQL avec `CONNECT BY LEVEL <= 100000` sur Oracle).

**5.3 — Repositories**
- Dérivation de nom : `findByDealerIdAndMileageGreaterThan(...)`
- `@Query` JPQL avec pagination
- `@Query(nativeQuery = true)` utilisant une fonction de fenêtrage
- Une `Specification<Vehicle>` composable pour la recherche multi-critères (brand ? fuel ? plage de km ?)
- Une projection DTO : `interface VehicleSummary { String getVin(); String getBrand(); }`

**5.4 — Chasse au N+1**
- Activez `spring.jpa.show-sql=true` + `logging.level.org.hibernate.orm.jdbc.bind=TRACE`, ou mieux, ajoutez **datasource-proxy / p6spy** pour compter les requêtes.
- Écrivez un endpoint qui liste 50 concessions **avec leurs véhicules**. Comptez les requêtes → vous devez en voir 51.
- Corrigez avec `@EntityGraph(attributePaths = "vehicles")`, puis avec `JOIN FETCH`. Recomptez.
- **Écrivez un test qui échoue si le nombre de requêtes dépasse N** (avec un `QueryCountHolder`). C'est le genre de test qui impressionne en revue de code.

**5.5 — SQL pur**
Dans la console SQL d'IntelliJ, écrivez :
1. Le 3ᵉ véhicule le plus cher par concession (`ROW_NUMBER() OVER (PARTITION BY dealer_id ORDER BY price DESC)`)
2. L'évolution mois par mois du nombre de commandes avec la variation vs mois précédent (`LAG`)
3. Le total cumulé des ventes par concession (`SUM(...) OVER (ORDER BY ... ROWS UNBOUNDED PRECEDING)`)
4. Un `MERGE INTO` pour un upsert de véhicules
5. La même requête en syntaxe Oracle **et** DB2, et notez les différences

**5.6 — Testcontainers**
```java
@Testcontainers
@SpringBootTest
class VehicleRepositoryIT {
    @Container @ServiceConnection
    static OracleContainer oracle = new OracleContainer("gvenzl/oracle-free:slim-faststart");
    // vos tests tournent contre un vrai Oracle, jetable
}
```
Faites tourner votre suite d'intégration contre Oracle réel plutôt que H2 — c'est la pratique attendue en entreprise, parce que H2 ment sur les dialectes.

#### Drill IntelliJ (30 min)
- Database tool window : parcourir le schéma, `Ctrl+Alt+Shift+U` sur une table → **diagramme UML** des relations
- Console SQL : exécution (`Ctrl+Entrée`), complétion des colonnes, formatage (`Ctrl+Alt+L`), historique
- **Explain Plan** (`Ctrl+Shift+E` dans la console) sur vos requêtes lourdes
- Édition inline des données dans le résultat, export en CSV/JSON/INSERT
- Injection de langage : IntelliJ colore et complète le SQL **à l'intérieur** de vos `@Query` — vérifiez que ça marche (sinon `Alt+Enter → Inject language → SQL`)
- Générer des entités JPA depuis la base avec JPA Buddy

---

<a name="weekend-1"></a>
## 4. WEEK-END 1 — Git, Jenkins, JIRA (~3h, optionnel mais rentable)

### Git (1h30)
Créez un repo bac à sable et pratiquez **en ligne de commande d'abord, puis dans IntelliJ** :
- `rebase -i` : squash, reword, drop, reorder de 5 commits
- Résoudre un conflit de merge volontaire (modifiez la même ligne sur 2 branches)
- `cherry-pick` d'un commit d'une branche à l'autre
- `stash` / `stash pop`, `git worktree`
- `git bisect` pour trouver le commit fautif sur 10 commits (scriptez-le : `git bisect run ./mvnw -q test`)
- `reflog` pour récupérer un commit « perdu » après un `reset --hard`
- Conventional Commits : `feat(catalog): add mileage endpoint`, `fix(FLEET-123): ...`

**Dans IntelliJ** (`Alt+9` = fenêtre Git) :
- Commit partiel : sélectionner **certaines lignes seulement** dans le diff avant de committer
- `Ctrl+K` commit, `Ctrl+Shift+K` push, `Ctrl+T` update
- **Local History** (clic droit sur un fichier → Local History) : votre filet de sécurité même sans commit
- Annotate / Blame dans la gouttière, avec navigation vers le commit
- Interactive rebase depuis le log (clic droit sur un commit → *Interactively Rebase from Here*)
- Shelve vs Stash
- Résolution de conflits avec le merge à 3 panneaux (`Resolve` → Merge)

### Jenkins (1h)
Écrivez un `Jenkinsfile` déclaratif pour `fleet` :
```groovy
pipeline {
  agent { docker { image 'maven:3.9-eclipse-temurin-21' } }
  options { timeout(time: 30, unit: 'MINUTES'); buildDiscarder(logRotator(numToKeepStr: '20')) }
  stages {
    stage('Build')  { steps { sh './mvnw -B clean compile' } }
    stage('Test')   {
      steps { sh './mvnw -B test' }
      post { always { junit '**/target/surefire-reports/*.xml' } }
    }
    stage('Quality'){ steps { withSonarQubeEnv('sonar') { sh './mvnw sonar:sonar' } } }
    stage('IT')     { steps { sh './mvnw -B verify -Pintegration' } }
    stage('Package'){ steps { sh './mvnw -B -DskipTests package' 
                              archiveArtifacts 'target/*.jar' } }
    stage('Deploy') {
      when { branch 'main' }
      steps { withCredentials([usernamePassword(credentialsId: 'nexus', 
              usernameVariable: 'U', passwordVariable: 'P')]) { sh './mvnw deploy' } }
    }
  }
  post { failure { mail to: 'team@example.com', subject: "Build ${env.BUILD_NUMBER} KO" } }
}
```
À comprendre : `agent`, `stages` vs `steps`, `post` (`always/success/failure/cleanup`), `when`, `parallel`, `environment`, `withCredentials`, `input` pour une validation manuelle, différence *declarative* vs *scripted*, shared libraries.
Bonus : lancez un Jenkins local (`docker run -p 8080:8080 jenkins/jenkins:lts-jdk21`) et faites vraiment tourner le pipeline sur votre repo.

### JIRA (30 min)
- Vocabulaire : Epic → Story → Sub-task, Bug, workflow (To Do / In Progress / In Review / Done), sprint, backlog, story points, board Scrum vs Kanban.
- **Smart commits** — vous les utiliserez tous les jours :
  ```
  git commit -m "FLEET-123 #comment corrige la régression km #time 2h #resolve"
  ```
- Convention de branche : `feature/FLEET-123-mileage-endpoint`, `bugfix/FLEET-456-npe-on-null-dealer`.
- Dans IntelliJ : `Tools → Tasks & Contexts → Configure Servers` → connectez un JIRA. Ensuite `Alt+Shift+N` ouvre un ticket, crée la branche nommée automatiquement, et restaure votre contexte d'onglets par ticket. Fonctionnalité très peu connue et très appréciée en équipe.

---

<a name="semaine-2"></a>
## 5. SEMAINE 2

---

### J6 — Reactive Streams & Project Reactor — fondations

**Objectifs** : comprendre *pourquoi* le réactif existe avant d'en écrire. C'est le point le plus discriminant de l'offre.

#### Théorie (45 min)
- **La spécification Reactive Streams** (4 interfaces : `Publisher`, `Subscriber`, `Subscription`, `Processor`) — intégrée au JDK 9 sous `java.util.concurrent.Flow`. Le point clé : `Subscription.request(n)` → **le consommateur pilote le débit** (backpressure), contrairement au push pur.
- Pourquoi : modèle « thread par requête » = un thread bloqué par I/O en attente. Le réactif libère le thread pendant l'attente → beaucoup plus de connexions concurrentes avec peu de threads. Le gain est sur le **scaling I/O**, pas sur la latence unitaire.
- `Mono<T>` (0..1) et `Flux<T>` (0..N).
- **Rien ne se passe tant qu'on ne souscrit pas.** Un `Flux` non souscrit = code mort.
- Cold vs hot publishers.
- Assembly time vs subscription time vs runtime — d'où viennent les stack traces incompréhensibles.

#### Exercices

**6.1 — Reactive Streams à la main**
Implémentez un `Publisher<Integer>` et un `Subscriber<Integer>` **avec `java.util.concurrent.Flow` uniquement, sans Reactor**. Le subscriber demande 2 éléments à la fois. Loguez chaque `request(n)` et chaque `onNext`. Objectif : *voir* la backpressure fonctionner. Ne passez pas à la suite tant que ce n'est pas clair.

**6.2 — Création**
Produisez le même `Flux<Vehicle>` de 5 façons : `just`, `fromIterable`, `fromStream`, `generate` (synchrone, avec état), `create` (asynchrone, multi-thread), et `defer` (évaluation paresseuse). Pour chacun, expliquez dans `NOTES.md` quand l'utiliser.

**6.3 — Le duel `flatMap` / `concatMap` / `flatMapSequential` / `switchMap`**
```java
Flux.range(1, 5)
    .flatMap(i -> Mono.just(i).delayElement(Duration.ofMillis(random(50, 300))))
    .subscribe(System.out::println);
```
Exécutez avec les 4 opérateurs et notez :
- l'**ordre** des résultats
- la **concurrence** (temps total)
Puis : `flatMap(mapper, 2)` — plafonnez la concurrence. Quand utiliseriez-vous `switchMap` ? (indice : autocomplétion / annulation).

**6.4 — Combinaison**
- `zip` de 3 appels indépendants (détails véhicule + historique + garantie) → un DTO agrégé. Mesurez : c'est parallèle.
- `merge` vs `concat` sur deux `Flux` avec des délais différents.
- `Mono.zip` + `map` vs `Mono.zipWith`.

**6.5 — Tests avec StepVerifier**
```java
StepVerifier.create(service.findByBrand("BMW"))
    .expectNextCount(3)
    .expectNextMatches(v -> v.vin().startsWith("WBA"))
    .verifyComplete();

// Temps virtuel : un test de 1h en 5 ms
StepVerifier.withVirtualTime(() -> Flux.interval(Duration.ofHours(1)).take(3))
    .thenAwait(Duration.ofHours(3))
    .expectNextCount(3)
    .verifyComplete();
```
Écrivez au moins 6 tests, dont un `expectError(...)` et un avec temps virtuel.

#### Drill IntelliJ (30 min)
- **Reactor Debug Agent** : ajoutez `reactor-tools` et `ReactorDebugAgent.init()` en début d'appli → vos stack traces réactives deviennent lisibles. Comparez une stack avant/après.
- `.log()` sur une chaîne : observez `onSubscribe / request / onNext / onComplete` dans la console.
- `checkpoint("nom")` pour marquer un point de la chaîne.
- IntelliJ : `Ctrl+P` (paramètres de la méthode) et `Ctrl+Shift+I` (aperçu de la définition) — vitaux pour explorer l'API Reactor sans quitter le fichier.
- `Ctrl+Q` : Quick Documentation sur un opérateur → vous avez le **diagramme en billes (marble diagram)** directement dans l'IDE. Utilisez-le systématiquement.

---

### J7 — Reactor avancé : schedulers, backpressure, erreurs, contexte

**Objectifs** : savoir écrire du code réactif *de production* — celui qui ne casse pas sous charge.

#### Théorie (45 min)
- **Schedulers** : `immediate`, `single`, `parallel` (CPU-bound, nb cœurs), `boundedElastic` (I/O bloquantes, à utiliser pour wrapper du JDBC/legacy), `fromExecutor`.
- `subscribeOn` (affecte la **source**, un seul compte, où qu'il soit placé) vs `publishOn` (affecte l'**aval**, à partir de son emplacement). Dessinez-le.
- **Ne jamais bloquer un thread event-loop.** `BlockHound` en test le détecte.
- Backpressure : `onBackpressureBuffer / Drop / Latest / Error`, `limitRate(n)`, `sample`, `buffer`, `window`.
- Erreurs : `onErrorReturn`, `onErrorResume`, `onErrorMap`, `doOnError` (side-effect, ne récupère pas), `retryWhen(Retry.backoff(3, ofMillis(200)).jitter(0.5))`, `timeout`. **Piège : `onErrorContinue`** ne fonctionne qu'avec les opérateurs qui le supportent et est déconseillé — préférez `onErrorResume` local.
- `Context` / `contextWrite` : propagation d'un correlation-id, du tenant, du token, là où un ThreadLocal ne marche plus.
- `Sinks` (`Sinks.many().multicast().onBackpressureBuffer()`) pour émettre depuis du code impératif.

#### Exercices

**7.1 — Schedulers, en observant les threads**
```java
Flux.range(1, 4)
    .doOnNext(i -> log("source"))
    .subscribeOn(Schedulers.boundedElastic())
    .doOnNext(i -> log("après subscribeOn"))
    .publishOn(Schedulers.parallel())
    .doOnNext(i -> log("après publishOn"))
    .blockLast();
```
avec `log(s) = System.out.println(Thread.currentThread().getName() + " " + s)`. Déplacez `subscribeOn` à différents endroits. **Rédigez la règle que vous en déduisez.**

**7.2 — Wrapper du bloquant**
Vous avez un repository JDBC bloquant (celui du J5). Exposez-le en `Mono`/`Flux` correctement :
```java
Mono.fromCallable(() -> repo.findByVin(vin))
    .subscribeOn(Schedulers.boundedElastic());
```
Ajoutez **BlockHound** en test et vérifiez qu'il hurle si vous oubliez le `subscribeOn`.

**7.3 — Pipeline de production**
Écrivez un traitement complet sur un `Flux<TelemetryEvent>` :
- producteur rapide (`Flux.interval(ofMillis(1))`), consommateur lent (100 ms) → observez le `OverflowException`
- appliquez `onBackpressureBuffer(1000, dropped -> metrics.increment())`
- enrichissez chaque événement par un appel externe lent, avec **concurrence limitée à 4** (`flatMap(..., 4)`)
- `timeout(ofSeconds(2))` par appel
- `retryWhen(Retry.backoff(3, ofMillis(100)).jitter(0.3).filter(ex -> ex instanceof TransientException))`
- fallback en cas d'échec définitif (`onErrorResume`)
- `contextWrite(ctx -> ctx.put("correlationId", id))` et récupérez-le dans un `doOnNext` via `Mono.deferContextual`
- métriques `.name("telemetry").metrics()` (Micrometer)

**7.4 — Sinks / SSE**
Un `Sinks.Many<TelemetryEvent>` alimenté par un `@Scheduled`, exposé plus tard (J8) en Server-Sent Events. Testez le comportement multicast avec 2 abonnés qui arrivent à des moments différents (`replay` vs `multicast`).

**7.5 — Le quiz des pièges**
Pour chacun de ces snippets, dites ce qui ne va pas (puis vérifiez en l'exécutant) :
```java
// a)
flux.map(v -> { repo.save(v); return v; });          // ?
// b)
Flux.just(1,2,3).subscribe(); flux.doOnNext(...);    // ?
// c)
mono.subscribe(v -> otherMono.subscribe());           // ?
// d)
webClient.get().retrieve().bodyToMono(X.class).block(); // dans un controller WebFlux  ?
// e)
flux.flatMap(this::callApi)  // 10 000 éléments, pas de limite de concurrence  ?
```

#### Drill IntelliJ (30 min)
- Déboguer du réactif : posez un breakpoint dans un `doOnNext` et regardez la pile — comparez avec/sans `ReactorDebugAgent`.
- **Async stack traces** : `Settings → Build → Debugger → Async Stack Traces` (activé par défaut).
- Onglet **Threads** du debugger : voyez les `reactor-http-nio-*`, `boundedElastic-*`, `parallel-*`.
- Créez un live template `sfx` → `Flux.fromIterable($ITER$).subscribeOn(Schedulers.boundedElastic())`.
- `Ctrl+Alt+Shift+T` sur une chaîne d'opérateurs → *Extract Method* pour garder des pipelines lisibles.

---

### J8 — Spring WebFlux, WebClient, R2DBC

#### Théorie (30 min)
- WebFlux sur Netty vs Spring MVC sur servlet. Deux styles : annoté (`@RestController` renvoyant `Mono`/`Flux`) et **fonctionnel** (`RouterFunction` + `HandlerFunction`).
- `WebClient` : `retrieve()` vs `exchangeToMono()`, `onStatus`, timeouts (connexion, réponse, lecture), pool de connexions, `filter()` pour l'authentification et le logging. **`WebClient` remplace `RestTemplate`, même dans une appli MVC.**
- R2DBC : totalement différent de JDBC/JPA — pas de mapping de relations, pas de lazy loading, `DatabaseClient` + repositories réactifs, transactions via `TransactionalOperator`. **Attention** : Oracle a un driver R2DBC officiel ; DB2 n'en a pas de mature → dans ces contextes, le pattern courant est *JDBC + `boundedElastic`*. Sachez le dire en entretien.
- Streaming : `Flux<T>` + `MediaType.TEXT_EVENT_STREAM_VALUE`, `application/x-ndjson`.

#### Exercices

**8.1 — `fleet-telemetry-service`**
Nouveau module Spring Boot avec `spring-boot-starter-webflux`.
- Endpoints annotés : `GET /api/v1/telemetry/{vin}` → `Flux<TelemetryEvent>` en SSE, `POST /api/v1/telemetry` → `Mono<Void>`.
- **Puis la même API en style fonctionnel** :
  ```java
  @Bean RouterFunction<ServerResponse> routes(TelemetryHandler h) {
      return route()
          .GET("/fn/telemetry/{vin}", h::stream)
          .POST("/fn/telemetry", accept(APPLICATION_JSON), h::ingest)
          .filter(loggingFilter())
          .build();
  }
  ```
  Comparez les deux dans `NOTES.md` : lisibilité, testabilité, verbosité.

**8.2 — WebClient**
Le telemetry-service appelle le catalog-service pour enrichir les événements.
- Configurez un `WebClient.Builder` bean avec base URL, timeouts (`HttpClient.create().responseTimeout(...)`), et un `ExchangeFilterFunction` qui logue méthode + URI + statut + durée.
- Gérez les erreurs : `onStatus(HttpStatusCode::is4xxClientError, r -> Mono.error(new VehicleNotFound(vin)))`.
- Testez avec **MockWebServer** (OkHttp) ou WireMock : réponse 200, 404, 500, timeout, réponse lente.

**8.3 — R2DBC**
- Ajoutez `spring-boot-starter-data-r2dbc` + `r2dbc-postgresql` (plus simple pour apprendre) ou `oracle-r2dbc`.
- `interface TelemetryRepository extends ReactiveCrudRepository<TelemetryEventEntity, Long>` + une `@Query`.
- Une transaction réactive avec `TransactionalOperator` : insérez 2 lignes dont la 2ᵉ échoue, vérifiez le rollback.
- Tests avec Testcontainers + `@DataR2dbcTest`.

**8.4 — La comparaison qui compte**
Exposez le **même** endpoint « liste de véhicules » dans le catalog (MVC + JDBC) et dans le telemetry (WebFlux + R2DBC). Chargez les deux avec un outil simple (`hey`, `k6`, ou `ab`) à 50 puis 500 connexions concurrentes. Notez : débit, latence p95, nombre de threads (`jcmd <pid> Thread.print | grep -c '"'`). **C'est l'expérience que vous raconterez en entretien.**

**8.5 — Tests**
`WebTestClient` sur les deux styles, y compris la vérification d'un flux SSE :
```java
webTestClient.get().uri("/api/v1/telemetry/{vin}", vin)
    .accept(TEXT_EVENT_STREAM)
    .exchange()
    .expectStatus().isOk()
    .returnResult(TelemetryEvent.class)
    .getResponseBody()
    .as(StepVerifier::create)
    .expectNextCount(3)
    .thenCancel().verify();
```

#### Drill IntelliJ (30 min)
- HTTP Client sur un endpoint SSE : IntelliJ affiche le flux en continu.
- **Comparer deux fichiers** (`Ctrl+D` dans la vue projet sur 2 fichiers sélectionnés) : comparez votre controller annoté et votre handler fonctionnel.
- **Bookmarks** (`F11`, `Ctrl+F11` avec mnémonique, `Shift+F11` pour la liste) : marquez les points clés de votre projet.
- **Structural Search & Replace** (`Ctrl+Shift+A` → "Structural Search") : trouvez tous les `.block()` du projet. Ajoutez-le comme inspection avec sévérité *Error*.
- Onglet **Services** (`Alt+8`) : gérez vos applis Spring Boot et vos conteneurs Docker au même endroit.

---

### J9 — Microservices : résilience, observabilité, sécurité

#### Théorie (45 min)
- Découpage par capacité métier, base par service, communication synchrone (HTTP) vs asynchrone (événements).
- Contrats d'API : versionning (`/v1`), compatibilité ascendante, OpenAPI, consumer-driven contracts (Spring Cloud Contract / Pact — notion).
- **Idempotence** (clé d'idempotence sur les POST), *at-least-once* et duplicats.
- Cohérence : Saga (chorégraphie/orchestration), **pattern Outbox** — à connaître conceptuellement.
- **Resilience4j** : `CircuitBreaker` (closed/open/half-open, seuil, fenêtre glissante), `Retry`, `TimeLimiter`, `Bulkhead`, `RateLimiter`. Ordre d'application des décorateurs.
- Observabilité : Micrometer (metrics) + Micrometer Tracing (OpenTelemetry), **traceId propagé** entre services, logs structurés JSON, `/actuator/health` avec probes `liveness`/`readiness`.
- Sécurité : OAuth2/OIDC — le service est un **Resource Server** qui valide un JWT (signature via JWKS, `iss`, `aud`, `exp`, scopes → authorities). Fait le lien avec l'`openid connect` côté Angular mentionné dans l'offre.

#### Exercices

**9.1 — Circuit breaker**
Sur l'appel telemetry → catalog :
```yaml
resilience4j.circuitbreaker.instances.catalog:
  slidingWindowSize: 20
  failureRateThreshold: 50
  waitDurationInOpenState: 10s
  permittedNumberOfCallsInHalfOpenState: 3
```
```java
@CircuitBreaker(name = "catalog", fallbackMethod = "fallbackVehicle")
@Retry(name = "catalog")
public Mono<Vehicle> fetch(Vin vin) { ... }
```
Faites tomber le catalog-service (arrêtez-le), observez : erreurs → ouverture du circuit → fallback immédiat → half-open → fermeture. Vérifiez les métriques sur `/actuator/metrics/resilience4j.circuitbreaker.calls`.

**9.2 — Sécurité JWT**
- Lancez un **Keycloak** en Docker, créez un realm `fleet`, un client, des rôles.
- Configurez les deux services en resource server :
  ```yaml
  spring.security.oauth2.resourceserver.jwt.issuer-uri: http://localhost:8081/realms/fleet
  ```
- `@PreAuthorize("hasAuthority('SCOPE_vehicle:write')")` sur le POST.
- Propagez le token de service à service (filtre WebClient).
- Testez avec `@WithMockJwtAuth` / `SecurityMockServerConfigurers.mockJwt()`.

**9.3 — Tracing distribué**
- Ajoutez `micrometer-tracing-bridge-otel` + `opentelemetry-exporter-zipkin`, lancez Zipkin en Docker.
- Faites un appel qui traverse gateway → telemetry → catalog. Retrouvez la trace complète dans Zipkin.
- Configurez les logs pour inclure `[%X{traceId},%X{spanId}]`. Vérifiez que ça marche **aussi en réactif** (c'est le piège : le MDC ne se propage pas tout seul — d'où `contextWrite` et le context-propagation de Micrometer).

**9.4 — Asynchrone (optionnel, si le temps)**
Kafka en Docker, un producteur d'événements `VehicleSold` dans le catalog, un consommateur dans le telemetry. Notion de *consumer group*, offsets, at-least-once.

**9.5 — Conteneurisation**
`Dockerfile` multi-stage, ou mieux : `./mvnw spring-boot:build-image` (Buildpacks, layered jar). Comparez la taille et le temps de démarrage. `docker-compose up` doit lancer toute la stack.

#### Drill IntelliJ (30 min)
- Plugin **Docker** : gérer conteneurs/images/compose depuis `Alt+8` (Services). Lancez `docker-compose.yml` depuis la gouttière.
- **Remote debug** : créez une run configuration `Remote JVM Debug`, lancez votre service avec `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005` dans Docker, attachez le debugger. **Compétence directement utile en mission.**
- **Run Dashboard / Compound configuration** : démarrer 3 services d'un coup.
- Explorateur **Endpoints** avec les deux services actifs.

---

### J10 — Performance, concurrence, JVM

#### Théorie (45 min)
- Modèle mémoire Java : visibilité, `volatile`, `synchronized`, `happens-before`, immutabilité comme stratégie n°1.
- `java.util.concurrent` : `ExecutorService`, `CompletableFuture` (`thenCompose` vs `thenApply`, `allOf`, `exceptionally`), `ConcurrentHashMap`, `AtomicLong`, `CountDownLatch`, `Semaphore`.
- **Virtual threads (Java 21)** : `Executors.newVirtualThreadPerTaskExecutor()`, `spring.threads.virtual.enabled=true`. Quand ils remplacent le réactif (code bloquant simple à scaler) et quand ils ne le remplacent pas (backpressure, composition de flux, streaming). **Question d'entretien très probable.**
- GC (G1 par défaut, ZGC pour la faible latence), heap/metaspace, `-Xmx`, OOM et heap dump, JFR.
- Coûts classiques : allocations dans les boucles chaudes, concaténation de String, boxing, sérialisation JSON, N+1, absence de connection pool tuning.

#### Exercices

**10.1 — JMH**
Module `fleet-perf` avec `jmh-core` + `jmh-generator-annprocess`. Benchmarkez :
- `String +` vs `StringBuilder` vs `String.join` vs `Collectors.joining` (10 / 10 000 éléments)
- boucle `for` vs `stream()` vs `parallelStream()` sur une somme
- `HashMap` vs `ConcurrentHashMap` en accès mono-thread
- votre mapper DTO fait main vs MapStruct

Règles : `@BenchmarkMode(Throughput)`, warmup 5 itérations, mesure 5 itérations, `Blackhole` pour éviter l'élimination par le JIT. **Le vrai apprentissage : constater à quel point l'intuition se trompe.**

**10.2 — Virtual threads vs réactif**
Écrivez trois versions du même agrégateur (3 appels HTTP lents à 200 ms) :
1. séquentiel bloquant
2. `CompletableFuture.allOf` sur un pool de plateformes
3. virtual threads (`newVirtualThreadPerTaskExecutor` + `StructuredTaskScope`)
4. Reactor (`Mono.zip`)

Chargez à 1 000 requêtes concurrentes. Comparez latence, threads, mémoire. Rédigez une synthèse d'une demi-page — c'est votre réponse d'entretien.

**10.3 — Profilage d'un endpoint lent**
Introduisez volontairement trois pathologies dans le catalog-service : un N+1, une sérialisation d'un graphe d'objets trop gros, et un `synchronized` sur un chemin chaud.
- Lancez le **IntelliJ Profiler** (Ultimate) : `Run → Profile`, ou *Attach profiler to process* dans l'onglet Services.
- Lisez le **flame graph** : identifiez les trois causes.
- Regardez l'onglet **Memory allocation**.
- Corrigez, re-profilez, mesurez le gain.
- Alternative gratuite : **async-profiler** ou **JFR** (`java -XX:StartFlightRecording=duration=60s,filename=rec.jfr`) ouvert dans JDK Mission Control.

**10.4 — Fuite mémoire**
Créez une fuite (une `static Map` qui accumule), lancez avec `-Xmx256m`, provoquez l'`OutOfMemoryError` avec `-XX:+HeapDumpOnOutOfMemoryError`, puis analysez le dump (Eclipse MAT ou le *Memory* tab d'IntelliJ). Trouvez le chemin de rétention.

**10.5 — Tuning DB**
Configurez HikariCP (`maximum-pool-size`, `connection-timeout`, `leak-detection-threshold`), activez les métriques Micrometer du pool, et observez la saturation sous charge.

#### Drill IntelliJ (30 min)
- Profiler : CPU flame graph, *Call Tree*, comparaison de deux snapshots.
- **Analyze → Run Inspection by Name**, et `Analyze → Inspect Code` sur tout le projet — traitez les 20 premières inspections.
- **Dependency analysis** : `Analyze → Analyze Dependencies` et le diagramme Maven (`Ctrl+Alt+Shift+U` sur le pom).
- Maven Helper : onglet *Dependency Analyzer*, résolution d'un conflit de version.
- `Ctrl+Alt+Shift+Insert` : Scratch file — pour tester un bout de Java ou de SQL sans polluer le projet. **Astuce très pratique au quotidien.**

---

<a name="weekend-2"></a>
## 6. WEEK-END 2 — Finalisation & revue (~3h)

1. **Nettoyage du repo** : README propre (architecture, comment lancer, décisions techniques), `docker-compose up` doit tout démarrer, `./mvnw verify` doit tout tester. Poussez sur GitHub — c'est votre portfolio.
2. **Revue de code de vous-même** : relisez le J1 avec les yeux du J10. Refactorez ce qui vous fait honte. Notez ce que vous avez appris.
3. **Relisez `NOTES.md` en entier** et transformez-le en fiche d'une page.
4. **Répondez à l'oral** aux 30 questions ci-dessous, à voix haute, chronomètre à 2 min par question.
5. **Relisez l'offre**, ligne par ligne, et cochez ce que vous savez démontrer avec du code de votre repo.

---

<a name="cheatsheet"></a>
## 7. Cheat sheet IntelliJ IDEA

> Imprimez-la. Le plus rentable : `Shift Shift`, `Alt+Enter`, `Ctrl+Shift+A`. Avec ces trois-là, vous trouvez tout le reste.

### Navigation
| Action | Win/Linux | macOS |
|---|---|---|
| Search Everywhere | `Shift Shift` | `Shift Shift` |
| Find Action | `Ctrl+Shift+A` | `⇧⌘A` |
| Classe / Fichier / Symbole | `Ctrl+N` / `Ctrl+Shift+N` / `Ctrl+Alt+Shift+N` | `⌘O` / `⇧⌘O` / `⌥⌘O` |
| Fichiers récents / Emplacements récents | `Ctrl+E` / `Ctrl+Shift+E` | `⌘E` / `⇧⌘E` |
| Déclaration / Implémentation / Super | `Ctrl+B` / `Ctrl+Alt+B` / `Ctrl+U` | `⌘B` / `⌥⌘B` / `⌘U` |
| Type de l'expression | `Ctrl+Shift+B` | `⌃⇧B` |
| Arrière / Avant | `Ctrl+Alt+←/→` | `⌘[` / `⌘]` |
| Dernière édition | `Ctrl+Shift+Backspace` | `⇧⌘⌫` |
| Structure du fichier | `Ctrl+F12` | `⌘F12` |
| Hiérarchie de types / d'appels | `Ctrl+H` / `Ctrl+Alt+H` | `⌃H` / `⌃⌥H` |
| Find Usages | `Alt+F7` | `⌥F7` |
| Aller à la ligne | `Ctrl+G` | `⌘L` |
| Bookmark | `F11` | `F3` |
| Fermer l'onglet / tout sauf actif | `Ctrl+F4` | `⌘W` |

### Édition
| Action | Win/Linux | macOS |
|---|---|---|
| **Intention actions / quick fix** | `Alt+Enter` | `⌥⏎` |
| Complétion basique / type-aware | `Ctrl+Espace` / `Ctrl+Shift+Espace` | `⌃Space` / `⌃⇧Space` |
| Compléter l'instruction | `Ctrl+Shift+Entrée` | `⇧⌘⏎` |
| Générer | `Alt+Insert` | `⌘N` |
| Override / Implement | `Ctrl+O` / `Ctrl+I` | `⌃O` / `⌃I` |
| Surround With | `Ctrl+Alt+T` | `⌥⌘T` |
| Étendre / réduire sélection | `Ctrl+W` / `Ctrl+Shift+W` | `⌥↑` / `⌥↓` |
| Dupliquer / supprimer ligne | `Ctrl+D` / `Ctrl+Y` | `⌘D` / `⌘⌫` |
| Déplacer ligne / instruction | `Alt+Shift+↑↓` / `Ctrl+Shift+↑↓` | `⌥⇧↑↓` / `⇧⌘↑↓` |
| Commenter ligne / bloc | `Ctrl+/` / `Ctrl+Shift+/` | `⌘/` / `⌥⌘/` |
| Curseurs multiples | `Alt+J` (occurrence suivante), `Alt+Shift+clic` | `⌃G`, `⌥⇧clic` |
| Reformater / optimiser imports | `Ctrl+Alt+L` / `Ctrl+Alt+O` | `⌥⌘L` / `⌃⌥O` |
| Quick doc / paramètres | `Ctrl+Q` / `Ctrl+P` | `F1` / `⌘P` |
| Définition en popup | `Ctrl+Shift+I` | `⌥Space` |

### Refactoring
| Action | Win/Linux | macOS |
|---|---|---|
| **Refactor This** | `Ctrl+Alt+Shift+T` | `⌃T` |
| Renommer | `Shift+F6` | `⇧F6` |
| Extract Variable / Method / Field / Constant / Parameter | `Ctrl+Alt+V` / `M` / `F` / `C` / `P` | `⌥⌘V` / `M` / `F` / `C` / `P` |
| Inline | `Ctrl+Alt+N` | `⌥⌘N` |
| Déplacer / Copier | `F6` / `F5` | `F6` / `F5` |
| Changer signature | `Ctrl+F6` | `⌘F6` |
| Sécuriser la suppression | `Alt+Suppr` | `⌘⌫` |

### Run / Debug
| Action | Win/Linux | macOS |
|---|---|---|
| Run / Debug (dernier) | `Shift+F10` / `Shift+F9` | `⌃R` / `⌃D` |
| Run contextuel | `Ctrl+Shift+F10` | `⌃⇧R` |
| Toggle breakpoint | `Ctrl+F8` | `⌘F8` |
| Step over / into / out | `F8` / `F7` / `Shift+F8` | idem |
| Resume | `F9` | `⌥⌘R` |
| Evaluate Expression | `Alt+F8` | `⌥F8` |
| Run to cursor | `Alt+F9` | `⌥F9` |
| Voir les breakpoints | `Ctrl+Shift+F8` | `⇧⌘F8` |
| Naviguer vers/créer le test | `Ctrl+Shift+T` | `⇧⌘T` |

### Git
| Action | Win/Linux | macOS |
|---|---|---|
| Commit / Push / Update | `Ctrl+K` / `Ctrl+Shift+K` / `Ctrl+T` | `⌘K` / `⇧⌘K` / `⌘T` |
| Fenêtre Git | `Alt+9` | `⌘9` |
| Opérations VCS (menu) | `Alt+`` ` | `⌃V` |
| Comparer avec la branche | via `Alt+9` → clic droit | idem |

### Fenêtres
`Alt+1` Projet · `Alt+4` Run · `Alt+5` Debug · `Alt+7` Structure · `Alt+8` Services · `Alt+9` Git · `Alt+0` Commit · `Ctrl+Shift+F12` maximiser l'éditeur · `Shift+Échap` fermer la fenêtre active

---

<a name="checklist"></a>
## 8. Checklist « prêt pour la mission »

Cochez seulement si vous pouvez **le faire de mémoire, sans tutoriel**.

**Java**
- [ ] Écrire une agrégation multi-niveaux en `Collectors.groupingBy` sans chercher
- [ ] Expliquer `flatMap` vs `map`, et le coût des streams parallèles
- [ ] Utiliser `record` + `sealed` + pattern matching pour modéliser un domaine
- [ ] Expliquer `equals`/`hashCode`, immutabilité, `Optional` correctement utilisé
- [ ] Expliquer quand utiliser des virtual threads plutôt que du réactif

**Reactive**
- [ ] Dessiner le contrat Reactive Streams et expliquer la backpressure
- [ ] Différencier `subscribeOn` et `publishOn`, et savoir sur quel Scheduler mettre du bloquant
- [ ] Écrire un pipeline avec limite de concurrence, timeout, retry avec backoff et fallback
- [ ] Tester avec `StepVerifier`, y compris en temps virtuel
- [ ] Citer 3 pièges du réactif (bloquer l'event loop, ne pas souscrire, `onErrorContinue`)

**Spring**
- [ ] Monter un service Boot 3 complet (REST, validation, gestion d'erreurs, config typée, Actuator) en < 1h
- [ ] `@WebMvcTest`, `@DataJpaTest`, `@SpringBootTest`, Testcontainers, WebTestClient
- [ ] Expliquer les transactions, la propagation, et le piège de l'auto-invocation
- [ ] Configurer un resource server OAuth2/JWT

**DB**
- [ ] Diagnostiquer et corriger un N+1
- [ ] Écrire une fonction de fenêtrage et lire un plan d'exécution
- [ ] Citer 4 différences entre Oracle et DB2
- [ ] Écrire une migration Flyway propre

**Outillage**
- [ ] Naviguer et refactorer dans IntelliJ sans souris
- [ ] Debug conditionnel, evaluate expression, drop frame, remote debug
- [ ] Rebase interactif, résolution de conflit, bisect
- [ ] Lire et modifier un Jenkinsfile déclaratif
- [ ] Profiler une appli et lire un flame graph

---

<a name="questions"></a>
## 9. Auto-évaluation — 30 questions type entretien

**Java**
1. Que change `sealed` par rapport à une classe abstraite ?
2. Pourquoi `Optional` ne doit-il pas être un champ d'entité ?
3. Streams parallèles : dans quels cas dégradent-ils les performances ?
4. Différence entre `Collectors.toMap` et `groupingBy` avec un downstream `mapping` ?
5. Qu'est-ce qu'un `record` ne peut pas faire ?
6. `HashMap` : que se passe-t-il en cas de collision, et depuis Java 8 ?
7. Virtual threads : quel problème résolvent-ils, et lequel ne résolvent-ils pas ?
8. `volatile` vs `synchronized` vs `AtomicInteger`.

**Reactive**
9. Expliquez la backpressure à quelqu'un qui ne connaît pas le réactif.
10. Où placer `subscribeOn` dans une chaîne ? Et si j'en mets deux ?
11. Quand utiliser `concatMap` plutôt que `flatMap` ?
12. Que fait `Mono.defer` et pourquoi en aurait-on besoin ?
13. Comment propager un correlationId dans une chaîne réactive ?
14. Cold vs hot publisher, avec un exemple métier.
15. Quels sont les risques d'un `.block()` et où est-ce acceptable ?
16. Comment testeriez-vous un `Flux` qui émet toutes les heures ?

**Spring**
17. Pourquoi l'injection par constructeur plutôt que par champ ?
18. Que fait `@Transactional(readOnly = true)` concrètement ?
19. Pourquoi `@Transactional` ne marche pas sur un appel de méthode interne ?
20. `@WebMvcTest` vs `@SpringBootTest` : que charge-t-on, et pourquoi ça compte ?
21. Comment gérez-vous les erreurs d'API de façon uniforme ?
22. WebFlux : quel bénéfice réel, et à quel coût ?

**DB**
23. Décrivez un N+1 que vous avez rencontré et comment vous l'avez trouvé.
24. `JOIN FETCH` vs `@EntityGraph` vs `@BatchSize`.
25. Que fait `ROW_NUMBER() OVER (PARTITION BY …)` ?
26. Comment décideriez-vous d'ajouter un index ?
27. Verrouillage optimiste : mécanisme et cas d'usage.

**Pratiques**
28. Comment abordez-vous un bugfix dans un code que vous ne connaissez pas ?
29. Que mettez-vous dans un pipeline CI pour un microservice Java ?
30. Comment garantissez-vous le respect des patterns existants d'une équipe que vous rejoignez ?

> La question 30 est celle de l'offre (« *en suivant les patterns déjà utilisés* »). Préparez une vraie réponse : lecture du code existant avant de proposer, ADR/documentation interne, revue de code, question à l'architecte plutôt qu'initiative solitaire.

---

<a name="ressources"></a>
## 10. Ressources

**Documentation de référence** (privilégiez-la aux tutoriels)
- Reactor : `projectreactor.io/docs/core/release/reference/` — la section *Which operator do I need?* est à garder ouverte
- Spring Boot / Spring Framework reference docs
- Reactive Streams : `reactive-streams.org` (la spec fait 3 pages, lisez-la)
- IntelliJ : `jetbrains.com/help/idea/` + `Help → Keyboard Shortcuts PDF`

**Livres** (si vous en prenez un seul : le premier)
- *Effective Java*, Joshua Bloch — les items 42-48 (lambdas & streams) et 78-84 (concurrence)
- *Hands-On Reactive Programming in Spring 5* ou *Reactive Spring* (Josh Long)
- *Java Concurrency in Practice* (daté mais toujours juste sur le modèle mémoire)
- *SQL Performance Explained*, Markus Winand (+ `use-the-index-luke.com`, gratuit)

**Pratique complémentaire**
- Exercism (track Java) pour des katas courts
- Le repo `reactor-core` lui-même : les tests sont une excellente documentation

---

### Une dernière chose

Ne cherchez pas à tout finir. Si vous devez sacrifier quelque chose, sacrifiez le J9 (microservices, que vous rattraperez sur le terrain) et le J10 (perf) plutôt que **J6-J8 (réactif)** : c'est le seul point de l'offre qui distingue vraiment les candidats, et le seul qu'on n'improvise pas en mission.
