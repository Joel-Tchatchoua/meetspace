# Module Room — Guideline

Référence de toutes les règles du module `room`, tirées du cahier des charges (`README.md`) et des choix d'architecture du projet (`CLAUDE.md`). En cas de doute, le cahier des charges fait foi ; les points non tranchés sont listés en fin de document.

## 1. Responsabilité

Le module `room` gère :

- les **sites** (bureaux) ;
- les **salles de réunion** et leurs **équipements** ;
- les **disponibilités** des salles par site ;
- les **occupations**, alimentées uniquement par les événements de réservation.

Il ne connaît ni les utilisateurs ni les réservations : il ne stocke que des identifiants (`reservationId`), sans clé étrangère. Il n'appelle jamais le module booking.

## 2. Modèle du domaine

Règles communes à toutes les tables :

- identifiant technique ;
- horodatages de création et de mise à jour, en **UTC** ;
- **verrouillage optimiste** sur les sites et les salles.

### 2.1 Site

`Site` est l'objet central du module : il porte ses propres règles et **gère ses salles**. Toute écriture sur une salle passe par lui.

| Attribut | Règle |
|---|---|
| Identifiant | `SiteId` (UUID), fourni par l'appelant |
| Code | Obligatoire, **unique** parmi les sites non supprimés |
| Nom | Obligatoire, **unique** parmi les sites non supprimés |
| Adresse | Obligatoire |
| Fuseau horaire | Par défaut `Indian/Mauritius` ; doit être un fuseau connu |
| Salles | Les salles du site ; une fois enregistré, un site ne recharge plus ses salles retirées |

- Suppression **logique** : un flag `deleted` rend le site inutilisable, sans le supprimer physiquement.
- Un site supprimé est introuvable : le modifier, le supprimer à nouveau ou y ajouter une salle renvoie `SITE_NOT_FOUND`.
- Le code et le nom d'un site supprimé redeviennent **libres** : un nouveau site peut les reprendre.
- Un site qui a encore des salles non retirées **ne peut pas être supprimé** ; c'est `Site.delete()` qui l'interdit.

Comportements de `Site` :

| Méthode | Règle |
|---|---|
| `addRoom` | Le code doit être libre parmi les salles non retirées du site |
| `changeRoomDetails` | La salle doit exister et ne pas être retirée ; un nouveau code doit être libre dans le site |
| `activateRoom`, `putRoomUnderMaintenance`, `closeRoom` | La salle doit exister et ne pas être retirée |
| `removeRoom` | La salle doit exister et ne pas être retirée ; elle est marquée comme retirée |
| `delete` | Aucune salle non retirée |

### 2.2 Salle de réunion (MeetingRoom)

Une salle appartient à un seul site. Seul `Site` peut la créer ou la modifier : son constructeur est privé, et sa fabrique `MeetingRoom.create(...)` comme ses méthodes de modification ne sont visibles que dans le package `room.domain.entities`.

| Attribut | Règle |
|---|---|
| Identifiant | `RoomId` (UUID), fourni par l'appelant |
| Code | Obligatoire, **unique parmi les salles non retirées de son site** |
| Nom | Obligatoire |
| Étage | Obligatoire, peut être négatif |
| Capacité | Value object `Capacity`, entre **1 et 200** personnes |
| Statut | `ACTIVE`, `MAINTENANCE` ou `CLOSED` ; `ACTIVE` à la création |
| Équipements | Ensemble parmi les valeurs ci-dessous, vide par défaut |

Équipements possibles : `PROJECTOR`, `VIDEO_CONFERENCE`, `WHITEBOARD`, `TV_SCREEN`, `CONFERENCE_PHONE`, `WHEELCHAIR_ACCESS`.

- Suppression **logique** : la salle est marquée comme retirée et reste dans son site.
- Une salle retirée est introuvable, et son code redevient libre dans le site.
- Toutes les transitions de statut sont autorisées.

### 2.3 Occupation (RoomOccupancy)

Enregistrement, propre au module, des créneaux où une salle est occupée.

| Attribut | Règle |
|---|---|
| Identifiant de réservation | Référence externe, sans clé étrangère |
| Salle | La salle occupée |
| Début / fin | Instants en UTC |
| Active | Indique si l'occupation bloque encore la salle |

- Alimentée **uniquement** par les événements du module booking.
- **Aucune modification possible via l'API.**

### 2.4 Événements traités (ProcessedEvent)

- Le module enregistre l'identifiant de chaque événement déjà traité.
- Recevoir deux fois le même événement **n'a aucun effet**.

## 3. Sécurité

| Rôle | Droits sur le module |
|---|---|
| `EMPLOYEE` | Lecture : sites, salles, disponibilités, planning |
| `ADMIN` | Lecture, plus la gestion des sites et des salles |
| `SERVICE` | Lecture des détails d'une salle (appel du module booking) |

- Rôles lus dans le claim `realm_access.roles` du JWT Keycloak ; nom d'utilisateur dans `preferred_username`.
- Contrôle par endpoint avec `@PreAuthorize`.
- Seuls les endpoints de health sont publics ; tous les autres exigent un token valide.

## 4. API

Base : `/api/v1`.

### 4.1 Pagination

Toutes les listes sont paginées avec les paramètres `page`, `size` et `sort`, au format :

```json
{ "content": [ ... ], "page": { "size": 20, "number": 0, "totalElements": 1, "totalPages": 1 } }
```

### 4.2 Sites

| Endpoint | Accès | Nature | Comportement |
|---|---|---|---|
| `GET /sites` | Connecté | Query | Liste les sites non supprimés |
| `GET /sites/{id}` | Connecté | Query | Un site, ou `404` |
| `POST /sites` | Admin | Command | Crée un site ; `409` si le code ou le nom est déjà pris |
| `PUT /sites/{id}` | Admin | Command | Remplace le code, le nom, l'adresse et le fuseau ; `404` si introuvable, `409` si le code ou le nom est pris par un autre site |
| `DELETE /sites/{id}` | Admin | Command | Suppression logique ; `404` si introuvable, `409` s'il reste des salles |

### 4.3 Salles

| Endpoint | Accès | Nature | Comportement |
|---|---|---|---|
| `GET /sites/{siteId}/rooms` | Connecté | Query | Salles d'un site, filtrables par capacité minimale, équipements, étage et statut |
| `GET /rooms/{id}` | Connecté | Query | Détails de la salle, avec son site et le fuseau horaire du site |
| `POST /sites/{siteId}/rooms` | Admin | Command | Ajoute une salle `ACTIVE` au site ; `404` si le site est introuvable, `409` si le code est pris dans le site |
| `PUT /rooms/{id}` | Admin | Command | Remplace le code, le nom, l'étage, la capacité et les équipements, sans toucher au site ni au statut ; `404` si introuvable, `409` si le code est pris dans le site |
| `PATCH /rooms/{id}/status` | Admin | Command | Change le statut (ex. mise en maintenance) ; `404` si introuvable |
| `DELETE /rooms/{id}` | Admin | Command | Retire la salle de son site (suppression logique) ; `404` si introuvable |

### 4.4 Disponibilités (fonctionnalité principale)

**`GET /sites/{siteId}/rooms/available`** (Query) renvoie les salles libres sur **tout** le créneau demandé.

Paramètres :

- **obligatoires** : date, heure de début, heure de fin, exprimées dans l'**heure locale du site** ;
- **optionnels** : capacité minimale, liste d'équipements requis.

Une salle est retournée seulement si **toutes** ces conditions sont vraies :

1. elle appartient au site et n'est pas supprimée ;
2. son statut est `ACTIVE` ;
3. sa capacité est suffisante et elle possède **tous** les équipements demandés ;
4. aucune occupation active ne chevauche le créneau.

Règle de chevauchement : deux créneaux `[a, b)` et `[c, d)` se chevauchent si `a < d` et `c < b`. Une réunion qui se termine **exactement** à l'heure de début demandée **ne chevauche pas**.

Contraintes :

- Le filtrage se fait **dans la requête en base**, jamais en chargeant toutes les salles pour filtrer en Java.
- Un créneau invalide (fin avant ou égale au début) est rejeté avec `400`.
- Les heures locales sont converties en UTC avec le fuseau du site avant la comparaison.

**`GET /rooms/{id}/schedule?date=...`** (Query) renvoie les plages occupées d'une salle pour une journée : **uniquement les plages horaires**, jamais qui a réservé ni pourquoi.

Les disponibilités peuvent avoir quelques secondes de retard sur les réservations, puisqu'elles sont mises à jour par événements. Le module booking revérifie toujours avant de confirmer.

## 5. Événements consommés

Le module **écoute** les événements publiés par booking via RabbitMQ.

| Événement | Effet sur l'occupation |
|---|---|
| Réservation créée | Crée une occupation active |
| Réservation replanifiée | Met à jour le créneau de l'occupation |
| Réservation annulée | Désactive l'occupation (libère la salle) |
| Réservation libérée (no-show) | Désactive l'occupation (libère la salle) |
| Réservation terminée | Désactive l'occupation |
| Réservation transférée | Aucun changement de créneau ni de salle |

Contenu de chaque événement : identifiant unique, type, date d'occurrence, identifiant de réservation, identifiant de salle, début et fin.

Règles de traitement :

- **Idempotence** : un événement déjà traité (même identifiant) est ignoré.
- **Ordre** : un événement plus ancien que le dernier traité pour la même réservation est ignoré.
- **Messages invalides** : après quelques tentatives en échec, le message part dans une **dead-letter queue** ; il ne bloque jamais la file.
- Le traitement d'un événement et l'enregistrement de son identifiant se font dans **la même transaction**.

## 6. Erreurs

Toutes les erreurs suivent le format **Problem Details (RFC 9457)**, avec :

- un titre clair ;
- le nom du service ;
- un horodatage ;
- pour les règles métier, un **code lisible par une machine** (ex. `SITE_HAS_ROOMS`).

| Situation | Exception | Statut |
|---|---|---|
| Ressource introuvable | `NotFoundException` | `404` |
| Commande invalide (champ manquant ou mal formé) | `InvalidCommandException` | `400` |
| Valeur refusée par un value object | `DomainValidationException` | `400` |
| Règle métier violée | `BusinessRuleViolationException` | `409` |
| Conflit de mise à jour (verrouillage optimiste) | `ConcurrentUpdateException` | `409` |
| Autre erreur de base ou de transaction (sans détail interne) | `TransactionalException` | `500` |
| Permission manquante | — | `403` |
| Erreur inattendue (sans détail interne) | — | `500` |

Codes déjà définis :

| Code | Exception | Statut |
|---|---|---|
| `SITE_NOT_FOUND` | `SiteNotFoundException` | `404` |
| `ROOM_NOT_FOUND` | `RoomNotFoundException` | `404` |
| `INVALID_CAPACITY` | `InvalidCapacityException` | `400` |
| `SITE_CODE_ALREADY_USED` | `SiteCodeAlreadyUsedException` | `409` |
| `SITE_NAME_ALREADY_USED` | `SiteNameAlreadyUsedException` | `409` |
| `SITE_HAS_ROOMS` | `SiteHasRoomsException` | `409` |
| `ROOM_CODE_ALREADY_USED` | `RoomCodeAlreadyUsedException` | `409` |

## 7. Health

- Endpoints de health publics, avec **liveness** et **readiness** séparés.
- La readiness couvre la base de données et RabbitMQ.

## 8. Architecture du module

```
com.accenture.meetspace
├── shared
│   ├── domain                Id, DomainException, NotFoundException,
│   │                         BusinessRuleViolationException, DomainValidationException,
│   │                         AggregateRoot, DomainEvent, DomainEventPublisher, DomainEventSubscriber
│   ├── application/command   Command, CommandHandler, CommandBus, CommandHandlerRegistration,
│   │                         TransactionalException, InvalidCommandException, CommandPreconditions
│   └── infrastructure/bus    CommandHandlerBus
└── room
    ├── domain
    │   ├── entities          Site, MeetingRoom
    │   ├── vo                SiteId, RoomId, Capacity
    │   ├── enums             Amenity, RoomStatus
    │   └── exceptions        exceptions métier (SiteNotFoundException, InvalidCapacityException, …)
    ├── application
    │   ├── command           découpé par intention métier (commande + handler)
    │   │   ├── create        CreateSite, CreateRoom
    │   │   ├── update        UpdateSite, UpdateRoom
    │   │   ├── changestatus  ChangeRoomStatus
    │   │   └── delete        DeleteSite, DeleteRoom
    │   ├── query             à venir
    │   └── port/out          SiteRepository
    └── infrastructure
        └── persistence
            ├── models        SiteModel, MeetingRoomModel (entités JPA)
            ├── repositories  SpringDataSiteRepository (Spring Data)
            ├── mappers       SiteModelMapper (modèle JPA ↔ domaine)
            └── adapters      JpaSiteRepository (implémente SiteRepository)
```

### Persistance

- **Socle** (`shared/infrastructure/persistence`) :
  - `BaseModel` porte l'id (`String`, UUID en `char(36)`), `created_at`, `updated_at` et `deleted_at`, en UTC.
  - `VersionModel` ajoute `@Version` et considère une entité comme nouvelle seulement si son id est nul ; sinon Spring Data fait un `merge`.
- **Soft delete systématique** : `BaseModel` porte `@SQLRestriction("deleted_at IS NULL")`. Toutes les lectures JPA ignorent donc les lignes supprimées, sans filtre à écrire dans les requêtes. Le mapper remplit `deleted_at` avec la `Clock` injectée quand le domaine marque un élément comme supprimé.
  - Hibernate n'applique pas cette restriction, héritée de `BaseModel`, au chargement d'une collection. Chaque association `@OneToMany` vers une entité soft-deletée doit donc la répéter : c'est le cas de `SiteModel.rooms`.
  - Les futures requêtes `NamedParameterJdbcTemplate` ne passent pas par Hibernate : elles devront filtrer elles-mêmes `deleted_at is null`.
- **Schéma** : il est créé par Flyway (`db/migration/V1__create_sites_and_meeting_rooms.sql`), avec trois tables : `sites`, `meeting_rooms` et `meeting_room_amenities`. Les contraintes en base doublent les règles du domaine :
  - code et nom uniques parmi les sites non supprimés ;
  - code unique parmi les salles non retirées d'un site ;
  - capacité entre 1 et 200 ;
  - statut parmi les trois valeurs.

  Les unicités sont des index uniques fonctionnels (`case when deleted_at is null then code end`), qui ignorent les lignes supprimées.
- **Restauration** : `Site.restore(Snapshot)` reconstruit le domaine depuis la base, et `MeetingRoom.restore` n'est visible que dans le package.
- **Transactions** : l'adaptateur n'a ni `@Transactional` ni `EntityManager`. C'est le cas d'usage entier qui est transactionnel : `CommandHandlerBus` ouvre la transaction autour du handler, et toute exception levée par le handler l'annule (rollback).
- **Verrouillage optimiste sur tout le site** : Hibernate incrémente `@Version` tout seul, mais seulement pour l'entité dont une colonne change. Ajouter ou modifier une salle ne toucherait donc pas la version du site. Les deux méthodes de chargement utilisées par les commandes (`findForUpdateById` et `findForUpdateByRoomId` dans `SpringDataSiteRepository`) portent donc `@Lock(OPTIMISTIC_FORCE_INCREMENT)` : un site chargé pour modification voit sa version incrémentée au commit, même quand seule une salle change. Ainsi, deux transactions qui modifient le même site, même par des salles différentes, ne peuvent pas toutes les deux réussir. Par exemple, supprimer un site pendant qu'une autre transaction y ajoute une salle échoue. L'incrément a lieu au commit.

### Rôle de chaque couche

- **domain** : aucune dépendance framework. Le domaine est découpé en `entities/`, `vo/`, `enums/` et `exceptions/`. `Site` et `MeetingRoom` sont tous deux dans `entities` pour que seul `Site` puisse modifier ses salles. `Site` n'hérite pas d'`AggregateRoot` pour l'instant, puisqu'il n'émet aucun événement.
- **Entités** : chaque entité a un constructeur privé et une fabrique statique `create(...)`. Elle n'a **pas de getters** : son état se lit par un record public `Snapshot`, déclaré dans l'entité et renvoyé par `snapshot()`.

  | Snapshot | Contenu |
  |---|---|
  | `Site.Snapshot` | `id`, `code`, `name`, `address`, `timeZone`, `deleted`, `rooms` (liste de `MeetingRoom.Snapshot`) ; `room(RoomId)` retrouve une salle |
  | `MeetingRoom.Snapshot` | `id`, `code`, `name`, `floor`, `capacity`, `amenities`, `status`, `deleted` |

  Un snapshot est une copie en lecture seule : le modifier ne change pas l'entité. Les handlers, les stubs et les tests lisent l'état par `snapshot()`. Les quelques questions dont `Site` a besoin sur ses salles (`MeetingRoom.hasCode`, `MeetingRoom.isDeleted`) ne sont visibles que dans le package.
- **application** :
  - Une **commande** est un record qui vérifie ses invariants dans son constructeur et lève `InvalidCommandException` avec le nom du champ en cause. Chaque cas d'usage garde ses propres règles, même si elles ressemblent à celles d'un autre.
  - Un **handler** charge le `Site` (par `findById`, ou par `findByRoomId` quand la commande ne connaît que la salle), appelle une méthode métier de `Site`, puis l'enregistre. Son type de retour est déclaré par la commande : `Command<R>` est traitée par un `CommandHandler<C, R>`, et `CommandBus.dispatch` renvoie ce `R`. Chaque cas d'usage renvoie son propre record de réponse, placé à côté de sa commande. L'infra le reprend tel quel comme réponse HTTP : il ne contient donc que des types simples (`UUID` plutôt que `SiteId`).

  | Cas d'usage | Réponse |
  |---|---|
  | CreateSite | `CreateSiteResponse(UUID siteId, boolean created)` |
  | CreateRoom | `CreateRoomResponse(UUID roomId, boolean created)` |
  | UpdateSite | `UpdateSiteResponse(UUID siteId, boolean updated)` |
  | UpdateRoom | `UpdateRoomResponse(UUID roomId, boolean updated)` |
  | ChangeRoomStatus | `ChangeRoomStatusResponse(UUID roomId, RoomStatus status, boolean changed)` |
  | DeleteSite | `DeleteSiteResponse(UUID siteId, boolean deleted)` |
  | DeleteRoom | `DeleteRoomResponse(UUID roomId, boolean deleted)` |
  - `SiteRepository` (écriture) : `save`, `findById`, `findByRoomId`, `existsByCode`, `existsByName`. `findById` et `findByRoomId` ignorent les sites supprimés ; `existsByCode` et `existsByName` les incluent.
- **infrastructure** : contrôleurs REST, entités JPA et mappers MapStruct, adaptateurs des ports, listener RabbitMQ, migrations Flyway. Un site est chargé avec ses seules salles non retirées.
- **Transactions** : `CommandHandlerBus` exécute tout le handler dans une seule transaction, et toute exception qui sort du handler l'annule. Les erreurs se répartissent en trois familles :

  | Famille | Exception | Où elle naît | Rollback |
  |---|---|---|---|
  | Commande invalide | `InvalidCommandException` | à la construction de la commande, **avant** le bus | aucun, aucune transaction n'est encore ouverte |
  | Erreur métier | `NotFoundException`, `BusinessRuleViolationException`, `DomainValidationException` | dans le domaine, pendant le handler | oui, l'exception remonte telle quelle |
  | Erreur technique | `TransactionalException` ; sa sous-classe `ConcurrentUpdateException` pour un conflit de version | dans le bus, qui traduit au même endroit `TransactionException`, `DataAccessException` et `OptimisticLockingFailureException`, souvent levées au commit | oui |

  Les exceptions métier n'héritent pas de `TransactionalException`. Le domaine ne connaît pas les transactions, et on doit pouvoir distinguer une erreur métier (`404` / `409` avec un code, jamais réessayée) d'une erreur technique (conflit `409` éventuellement réessayable, ou `500`).

Les invariants appartiennent au domaine :

| Invariant | Porté par |
|---|---|
| Capacité entre 1 et 200 | `Capacity` |
| Code de salle unique dans le site | `Site.addRoom`, `Site.changeRoomDetails` |
| Site non supprimable s'il a des salles | `Site.delete` |
| Salle introuvable ou retirée | `Site` (`RoomNotFoundException`) |
| Fin après début (à venir) | `TimeSlot` |
| Chevauchement de créneaux (à venir) | `TimeSlot.overlaps(...)` |

L'unicité du code et du nom d'un site, qui dépend des autres sites, est vérifiée par le handler via `SiteRepository`.

## 9. Tests

Démarche **TDD** : le test est toujours écrit avant le code.

| Niveau | Cible | Outils |
|---|---|---|
| Unitaire | Domaine et handlers | Stubs en mémoire des ports, aucun mock |
| Intégration | Adaptateurs (JPA, RabbitMQ) | MySQL et RabbitMQ réels via Testcontainers |
| E2E | Application complète par HTTP | `WebTestClient`, Testcontainers, JWT de test façon Keycloak |

- `Clock` injectée pour contrôler le temps.
- Tests organisés avec `@Nested` et `@DisplayName` ; chaque test vérifie le statut **et** le corps de la réponse.
- Couverture minimale : **80 %**.
- Les éléments du socle `shared` ne sont pas testés.

### Organisation des tests d'intégration

Les tests d'intégration sont dans `room/integration/`, avec un fichier par adaptateur (`JpaSiteRepositoryTest`) :

- chaque test porte `@JpaIntegrationTest` (dans `shared/integration`) et importe seulement ce qu'il teste (par exemple `@Import({JpaSiteRepository.class, SiteModelMapper.class})`). L'annotation regroupe :
  - `@DataJpaTest` ;
  - le profil `test` ;
  - la `Clock` ;
  - un nettoyage de la base avant chaque test : toutes les tables sont vidées, sauf `flyway_schema_history`, avant même l'ouverture de la transaction du test.
- la configuration de test est dans `src/test/resources/application-test.yaml`. Elle déclare un vrai MySQL 8.4 lancé par Testcontainers via l'URL `jdbc:tc:mysql:8.4:///meetspace`, avec Flyway en validation et `ddl-auto: validate`. La même validation est active en dev ;
- chaque test tourne dans une transaction annulée à la fin, sauf le groupe « optimistic locking », qui utilise de vraies transactions commitées (`TransactionTemplate`) et vide les tables après chaque test.

### Organisation des tests unitaires

Les tests ne reprennent pas les packages du code. Ils sont regroupés dans un dossier `units/`, avec **un fichier par cas d'usage** :

```
src/test/java/com/accenture/meetspace/room/units/
├── CreateSiteTest, UpdateSiteTest, DeleteSiteTest
├── CreateRoomTest, UpdateRoomTest, ChangeRoomStatusTest, DeleteRoomTest
├── CapacityTest
└── stubs/
    └── InMemorySiteRepository
```

Chaque fichier contient deux groupes `@Nested` :

- **command** : les invariants de la commande, cas valides et cas rejetés ;
- **handler** : le cas nominal et les cas d'échec, sur un stub en mémoire.

Un value object qui porte une règle (comme `Capacity`) a son propre fichier de test.

### Scénarios minimum

**CRUD des sites et des salles**

- succès ;
- ressource introuvable ;
- doublons (code ou nom de site, code de salle dans un site) ;
- refus pour un `EMPLOYEE` sur les endpoints admin ;
- suppression d'un site qui a encore des salles.

**Disponibilités**

- les salles libres sont retournées ;
- les salles réservées sont exclues ;
- les créneaux bout à bout sont autorisés ;
- les salles en maintenance sont exclues ;
- les créneaux invalides sont rejetés.

**Événements**

- un événement « créée » rend la salle occupée ;
- un événement « annulée » la libère ;
- un événement en double n'est traité qu'une fois ;
- un événement plus ancien arrivé en retard est ignoré.

## 10. Décisions prises

- Les identifiants sont des **UUID**, générés par l'appelant et passés dans la commande.
- Modifier un site avec un code ou un nom pris par un autre site renvoie `409`, comme à la création.
- **Soft delete systématique** : un site supprimé ou une salle retirée restent en base avec `deleted_at`, mais aucune lecture ne les voit ; leur code (et le nom d'un site) redevient libre.
- `Site` gère ses salles : ajout, modification, statut et retrait passent tous par lui.
- La capacité est un value object (`Capacity`) qui porte la règle 1 à 200.
- Une salle est créée `ACTIVE` ; le statut ne change que par `PATCH /rooms/{id}/status`.
- Toutes les transitions de statut sont autorisées.
- Une commande déclare son type de retour (`Command<R>`) ; elle ne renvoie pas forcément `void`. La création se fait avec un identifiant déjà connu de l'appelant.
- Les entités n'ont pas de getters : leur état se lit par un record `Snapshot` interne, et elles se créent par une fabrique `create(...)` avec un constructeur privé.

## 11. Points à clarifier

- Suppression d'une salle qui a des occupations actives futures : autorisée ?
- Mise à jour d'un site : le fuseau horaire est-il modifiable alors que des occupations existent ?
- `CLOSED` et `MAINTENANCE` : différence de comportement au-delà de l'exclusion des disponibilités ?
- Critère d'ordre des événements : date d'occurrence de l'événement, ou numéro de version de la réservation ?
- Contenu exact de la réponse d'une salle pour le rôle `SERVICE`.
