# Module User — Guideline

Référence des règles du module `user`, tirées du cahier des charges (`README.md`, sections 1, 4.2, 5, 7.1, 7.2, 8.3 et 10) et des choix d'architecture du projet.

Le cahier des charges place ces fonctionnalités dans `booking-service`. Le projet les isole dans un module dédié, en vue du découpage en microservices.

## 1. Responsabilité

Le module `user` gère :

- l'**inscription**, la **connexion** et la **déconnexion**, à travers Keycloak ;
- le **profil** de chaque utilisateur ;
- l'**administration** des comptes : recherche, activation, désactivation et changement de rôle.

Keycloak émet tous les tokens. Les services ne font que les valider, et aucun mot de passe n'est stocké dans le module.

### Découpage interne

| Package | Contenu |
|---|---|
| `user.auth` | Inscription, connexion, déconnexion et utilisateur authentifié (`Auth`) |
| `user.profile` | Profil et administration : `Employee`, `Admin`, `User`, `RoleEnum` |

Chaque package suit l'architecture hexagonale (`domain`, `application`, `infrastructure`). `auth` peut référencer `profile`, car l'inscription crée un profil. `profile` ne connaît pas `auth`.

## 2. Modèle du domaine

Règles communes à toutes les tables (README, section 4) : identifiant technique, horodatages de création et de mise à jour en **UTC**, et **verrouillage optimiste** sur les profils.

Le **profil utilisateur** existe en un exemplaire par utilisateur inscrit. Il contient :

- l'identifiant Keycloak ;
- le nom d'utilisateur et l'email, tous deux uniques ;
- le prénom et le nom ;
- en option, un département, un téléphone et un site par défaut ;
- le rôle (`ADMIN` ou `EMPLOYEE`) et un indicateur actif ou inactif.

Dans le code, ce profil se répartit ainsi :

| Classe | Package | Contenu actuel |
|---|---|---|
| `Employee` | `user.profile` | Possède un `User` et porte directement le rôle `EMPLOYEE`. `promote()` donne un `Admin` |
| `Admin` | `user.profile` | Possède un `User` et porte directement le rôle `ADMIN`. `demote(...)` donne un `Employee`, sauf pour le dernier administrateur actif |
| `User` | `user.profile` | Identifiant, identifiant Keycloak, nom d'utilisateur, email, prénom, nom, département, téléphone, site par défaut, actif |
| `RoleEnum` | `user.profile` | `ADMIN`, `EMPLOYEE` |

Le rôle Keycloak `SERVICE` concerne les appels entre services. Il n'est pas stocké dans le profil.

`Auth` (`user.auth`) représente l'utilisateur authentifié : `userId`, `username` et `active`. Elle bloque un compte désactivé avec `ensureActive()`.

### Principes de conception

- Les tests partent des **cas d'usage**. Les classes du domaine émergent de ces tests : pas de test de classe isolée.
- Un value object n'existe que s'il porte un vrai comportement. Une simple chaîne est validée par la commande.

## 3. Authentification

Chemin de base : `/api/v1`.

| Endpoint | Accès | Description |
|---|---|---|
| `POST /auth/register` | Public | Crée un compte avec le rôle `EMPLOYEE` |
| `POST /auth/login` | Public | Échange un nom d'utilisateur et un mot de passe contre des tokens Keycloak |
| `POST /auth/logout` | Public | Révoque le refresh token |

Règles d'inscription :

- le nom d'utilisateur et l'email ne doivent pas être déjà pris, sinon `409` ;
- le mot de passe et sa confirmation doivent correspondre, vérification écrite sous forme d'**annotation de validation personnalisée** ;
- le mot de passe doit être fort : au moins 8 caractères, avec une majuscule, un chiffre et un symbole ;
- l'inscription crée l'utilisateur dans Keycloak, lui attribue le rôle `EMPLOYEE` et crée le profil ;
- si l'enregistrement du profil échoue, l'utilisateur Keycloak est supprimé, pour ne laisser aucun compte à moitié créé.

## 4. Profil et administration

| Endpoint | Accès | Description |
|---|---|---|
| `GET /profile` | Connecté | Affiche mon profil |
| `PATCH /profile` | Connecté | Modifie mon profil, seuls les champs envoyés changent. Un site par défaut doit exister dans `room` |
| `GET /users/directory` | Connecté | Recherche des collègues actifs par nom, par exemple pour les inviter. Ne renvoie que des champs publics de base |
| `GET /users` | Admin | Liste tous les utilisateurs, avec filtres sur le nom d'utilisateur, l'email, le département, le rôle et le statut |
| `GET /users/{id}` | Admin | Détail d'un utilisateur |
| `PATCH /users/{id}/status` | Admin | Active ou désactive un utilisateur |
| `PUT /users/{id}/role` | Admin | Change le rôle, dans Keycloak et dans le profil |

Règles de statut et de rôle :

- désactiver un utilisateur le désactive aussi dans Keycloak et **annule toutes ses réservations à venir** ;
- un administrateur ne peut pas désactiver son propre compte ;
- le dernier administrateur actif ne peut pas être rétrogradé.

## 5. Sécurité

- Les rôles sont lus dans le claim `realm_access.roles` du token Keycloak, et le nom d'utilisateur dans `preferred_username`.
- Cette logique est écrite une seule fois, dans le socle commun.
- Les rôles sont vérifiés sur chaque endpoint avec `@PreAuthorize`.
- L'inscription, la connexion, la déconnexion et la santé sont publiques. Tout le reste exige un token valide.
- Un utilisateur désactivé reçoit `403` sur tout endpoint, même si son token est encore valide.
- Les secrets sont lus uniquement dans des variables d'environnement.

## 6. Erreurs

Toutes les erreurs sont au format Problem Details, avec un titre clair, le nom du service, un horodatage et, pour les règles métier, un code lisible par une machine.

| Situation | Statut |
|---|---|
| Ressource introuvable | 404 |
| Entrée invalide, chaque champ invalide est listé | 400 |
| Règle métier violée | 409 |
| Mise à jour concurrente | 409 |
| Permission manquante ou compte désactivé | 403 |
| Autre service indisponible | 503 |
| Erreur inattendue, sans détail interne | 500 |

## 7. Relations avec les autres modules

Le module n'appelle directement que `bootstrap`. Le reste passe par des ports, à concevoir avec les cas d'usage concernés :

| Besoin | Module concerné |
|---|---|
| Vérifier qu'un site par défaut existe | `room` |
| Annuler les réservations à venir d'un utilisateur désactivé | `reservation` |
| Vérifier que les invités existent et sont actifs | `reservation` appelle `user` |

## 8. Tests

Scénarios minimaux du cahier des charges :

- **Inscription** : succès, nom d'utilisateur déjà pris, confirmation différente, mot de passe faible, annulation quand l'enregistrement du profil échoue.
- **Profil** : lecture, mise à jour partielle qui laisse les autres champs inchangés.
- **Administration** : les filtres fonctionnent, un employé n'accède pas aux endpoints d'administration, désactiver un utilisateur annule ses réservations, un utilisateur désactivé est bloqué, le dernier administrateur ne peut pas être rétrogradé.

Les tests utilisent des JWT de test qui imitent ceux de Keycloak. Les appels à Keycloak et aux autres modules passent par des ports, remplacés par des stubs en mémoire dans les tests unitaires.

## 9. État d'avancement

| Package | Cas d'usage | Endpoint |
|---|---|---|
| `auth` | `RegisterEmployee` | `POST /auth/register` |
| `auth` | `Login` | `POST /auth/login` |
| `auth` | `Logout` | `POST /auth/logout` |
| `profile` | `GetProfile` | `GET /profile` |
| `profile` | `UpdateProfile` | `PATCH /profile` |
| `profile` | `SearchDirectory` | `GET /users/directory` |
| `profile` | `ListUsers` | `GET /users` |
| `profile` | `GetUser` | `GET /users/{id}` |
| `profile` | `ChangeUserStatus` | `PATCH /users/{id}/status` |
| `profile` | `ChangeUserRole` | `PUT /users/{id}/role` |
| `profile` | `CreateInitialAdmin` | Lancé au démarrage |

Les cas d'usage sont branchés sur MySQL (migration `V2__create_users.sql`) et sur REST, et testés à trois niveaux : unitaire, intégration (`JpaUserRepository`) et E2E.

### Ports et adaptateurs

| Port | Adaptateur actuel | Effet |
|---|---|---|
| `UserRepository`, `UserReader` | MySQL | Réel |
| `SiteCatalog` | `RoomSiteCatalog` dans `bootstrap-app` ; `UnavailableSiteCatalog` si `user` démarre seul | Réel dans le monolithe, `503` sinon |
| `IdentityProvider` | `KeycloakIdentityProvider` si `meetspace.keycloak.base-url` est défini ; `UnavailableIdentityProvider` sinon | Réel en dev, `503` sans configuration |
| `ReservationCanceller` | `NoReservationCanceller` | **Provisoire** : n'annule rien, le module `reservation` étant vide |

Les adaptateurs par défaut sont déclarés dans `UserDefaultAdaptersAutoConfiguration`, avec `@ConditionalOnMissingBean` : un vrai adaptateur les remplace sans autre changement.

### Décisions

- **Administrateur préconfiguré :** son profil est créé au démarrage par `CreateInitialAdmin`, à partir de son compte Keycloak (`meetspace.initial-admin.username`). Rien n'est créé s'il existe déjà. Si Keycloak est injoignable ou ne connaît pas ce compte, l'application démarre quand même et l'écrit dans les logs.
- **Utilisateur authentifié :** `AuthenticatedUserFilter` cherche le profil par `preferred_username`. Il répond `403 PROFILE_NOT_FOUND` sans profil et `403 ACCOUNT_DISABLED` pour un compte désactivé. Sinon, il range l'id du profil dans la requête. L'id du profil et l'id Keycloak restent distincts.
- **Connexion invalide :** `401 INVALID_CREDENTIALS`.
- **`PATCH /profile` :** `null` signifie « non envoyé ». Un champ optionnel ne peut donc pas être vidé.
- **Sécurité :** seuls `/auth/register`, `/auth/login`, `/auth/logout`, la santé, `/error` et la documentation OpenAPI sont publics. Tout le reste exige un token valide.

### Keycloak

- `IdentityProvider` est le seul port vers Keycloak, partagé par `auth` et `profile` : comptes, rôles, statut et tokens. Il reste dans `profile`, car `auth` dépend de `profile` et jamais l'inverse.
- `compose.yaml` démarre Keycloak et importe `keycloak/realm/meetspace-realm.json` : rôles, client et administrateur. Les secrets viennent de `.env`.
- Un utilisateur présent dans Keycloak mais sans profil ne peut pas s'inscrire (`409 IDENTITY_ALREADY_EXISTS`). Son profil est créé à son premier appel authentifié.
- Un échec de Keycloak pendant un changement de rôle ou de statut annule la transaction, mais rien ne compense une opération déjà faite dans Keycloak.
- Chaque appel d'administration demande un nouveau token de service : aucun cache pour l'instant.

### Reste à faire

- Adaptateur réel de `ReservationCanceller`, une fois `reservation` reconstruit.

## 10. Points ouverts

- **Courses à l'inscription :** deux inscriptions simultanées du même nom d'utilisateur passent le contrôle d'unicité. L'index unique refuse la seconde et l'utilisateur Keycloak est supprimé, mais elle répond `500` et non `409`. Il en va de même pour deux premiers appels simultanés d'un utilisateur sans profil.
- **Migrations :** `V2` s'ajoute à l'historique Flyway commun. Une fois appliquée sur une base, `room` lancé seul sur cette base ne connaît pas `V2` et sa validation Flyway devrait échouer, jusqu'à la séparation des schémas par module.
- **Filtres de `GET /users` :** le README ne dit pas si la comparaison est exacte. Le nom d'utilisateur, l'email et le département se comparent partiellement, sans tenir compte de la casse.
- **Nom d'utilisateur et email :** le README ne dit pas s'ils peuvent changer après l'inscription. `PATCH /profile` ne les modifie pas.
- **Champs à l'inscription :** le README ne dit pas si elle accepte le département, le téléphone et le site par défaut. Ils se renseignent avec `PATCH /profile`.
- **Formats :** le README n'en fixe aucun pour l'email, le nom d'utilisateur ou le téléphone.
- **Échec de la compensation :** si la suppression de l'utilisateur Keycloak échoue à son tour, le README ne dit pas quoi faire.
