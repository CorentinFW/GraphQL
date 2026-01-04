# ✅ Migration GraphQL - Module Hotellerie (Module 1) - TERMINÉ

## 📋 Résumé de la migration

La migration du module Hotellerie de REST vers GraphQL a été **complétée avec succès** ! 

### ✅ Tâches accomplies

#### 1.1 ✅ Configuration Maven (pom.xml)
- ✅ Ajouté les dépendances GraphQL Spring Boot
  - `spring-boot-starter-graphql`
  - `spring-graphql-test` (pour les tests)
- ✅ Retiré la dépendance Swagger/OpenAPI (`springdoc-openapi-ui`)
- ✅ Conservé `spring-boot-starter-web` (nécessaire pour GraphQL over HTTP)
- ✅ Configuré le plugin Maven resources pour gérer correctement les fichiers binaires (images PNG)

#### 1.2 ✅ Schéma GraphQL
- ✅ Créé le dossier `src/main/resources/graphql/`
- ✅ Créé le fichier `hotel.graphqls` avec :
  - **Queries** : `hotelInfo`, `rechercherChambres`, `chambre`, `reservations`
  - **Mutations** : `creerReservation`, `annulerReservation`
  - **Types** : `HotelInfo`, `Chambre`, `Reservation`, `ReservationResponse`
  - **Inputs** : `RechercheInput`, `ReservationInput`

#### 1.3 ✅ Resolvers GraphQL
- ✅ Créé le package `org.tp1.hotellerie.graphql`
- ✅ Créé `HotelQueryResolver.java` avec les méthodes :
  - `hotelInfo()` → remplace `GET /api/hotel/info`
  - `rechercherChambres()` → remplace `POST /api/hotel/chambres/rechercher`
  - `chambre(id)` → nouvelle fonctionnalité
  - `reservations()` → remplace `GET /api/hotel/reservations`
- ✅ Créé `HotelMutationResolver.java` avec les méthodes :
  - `creerReservation()` → remplace `POST /api/hotel/reservations`
  - `annulerReservation()` → nouvelle fonctionnalité

#### 1.4 ✅ Adaptation des services
- ✅ Ajouté la méthode `getChambreById()` dans `HotelService.java`
- ✅ Conservé toute la logique métier existante
- ✅ Conservé les repositories JPA sans modification

#### 1.5 ✅ Configuration GraphQL
- ✅ Mis à jour `application.properties` avec :
  ```properties
  spring.graphql.graphiql.enabled=true
  spring.graphql.graphiql.path=/graphiql
  spring.graphql.path=/graphql
  ```
- ✅ Corrigé les problèmes d'encodage UTF-8 dans application.properties
- ✅ Conservé toutes les configurations H2, JPA et profils existants

#### 1.6 ✅ Suppression du code REST
- ✅ Renommé `HotelController.java` en `HotelController.java.old` (backup)
- ✅ Conservé tous les DTOs (compatibles avec GraphQL)
- ✅ Conservé le package `model/` (entités JPA)
- ✅ Conservé le package `repository/` (JPA repositories)
- ✅ Conservé `HotelService.java` (logique métier réutilisée par GraphQL)

#### 1.7 ✅ Compilation et Build
- ✅ Build Maven réussi : `mvn clean compile` ✅
- ✅ Package JAR créé : `mvn package` ✅
- ✅ Toutes les classes compilent sans erreur

---

## 🎯 Ce qui a été conservé

### ✅ Base de données H2
- Configuration H2 inchangée
- Entités JPA (`@Entity`) inchangées
- Repositories JPA inchangés
- Les 3 profils (Paris, Lyon, Montpellier) fonctionnent toujours

### ✅ Logique métier
- `HotelService.java` : réutilisé directement par les resolvers GraphQL
- Recherche de chambres avec critères
- Gestion des réservations
- Calcul des disponibilités
- Images des chambres

### ✅ Configuration multi-profils
- `application-paris.properties`
- `application-lyon.properties`
- `application-montpellier.properties`

---

## 🔄 Ce qui a changé

### ❌ REST → ✅ GraphQL

| Avant (REST) | Après (GraphQL) |
|--------------|-----------------|
| `HotelController.java` | `HotelQueryResolver.java` + `HotelMutationResolver.java` |
| `GET /api/hotel/info` | `query { hotelInfo { ... } }` |
| `POST /api/hotel/chambres/rechercher` | `query { rechercherChambres(criteres: {...}) { ... } }` |
| `POST /api/hotel/reservations` | `mutation { creerReservation(reservation: {...}) { ... } }` |
| `GET /api/hotel/reservations` | `query { reservations { ... } }` |
| Swagger UI `/swagger-ui.html` | GraphiQL `/graphiql` |

---

## 📝 Prochaines étapes

### Module 2 : Agence
- Migrer les controllers REST vers GraphQL
- Créer `HotelGraphQLClient` pour appeler les hôtels via GraphQL
- Créer les resolvers d'agrégation
- Conserver la logique d'application des coefficients

### Module 3 : Client (Swing)
- Créer `AgenceGraphQLClient` pour remplacer `AgenceRestClient`
- Adapter l'interface Swing (aucun changement visuel)
- Tester l'application complète end-to-end

---

## 🚀 Démarrage de l'application

### Commandes disponibles

```bash
# Démarrer l'hôtel Paris (port 8082)
cd /home/corentinfay/Bureau/GraphQL/Hotellerie
mvn spring-boot:run -Dspring-boot.run.profiles=paris

# Démarrer l'hôtel Lyon (port 8083)
mvn spring-boot:run -Dspring-boot.run.profiles=lyon

# Démarrer l'hôtel Montpellier (port 8084)
mvn spring-boot:run -Dspring-boot.run.profiles=montpellier
```

### URLs GraphiQL

- **Hôtel Paris** : http://localhost:8082/graphiql
- **Hôtel Lyon** : http://localhost:8083/graphiql
- **Hôtel Montpellier** : http://localhost:8084/graphiql

### Endpoint GraphQL

- **Endpoint unique** : `http://localhost:808X/graphql` (où X = 2, 3 ou 4)

---

## 📊 Exemples de requêtes GraphQL

### Query : Informations de l'hôtel
```graphql
query {
  hotelInfo {
    nom
    adresse
    ville
  }
}
```

### Query : Rechercher des chambres
```graphql
query {
  rechercherChambres(criteres: {
    adresse: "Paris"
    dateArrive: "2026-02-01"
    dateDepart: "2026-02-05"
    prixMax: 200
    nbrLits: 2
  }) {
    id
    nom
    prix
    nbrDeLit
    nbrEtoile
    disponible
    imageUrl
  }
}
```

### Mutation : Créer une réservation
```graphql
mutation {
  creerReservation(reservation: {
    chambreId: "1"
    nomClient: "Dupont"
    prenomClient: "Jean"
    emailClient: "jean.dupont@example.com"
    dateArrive: "2026-02-01"
    dateDepart: "2026-02-05"
  }) {
    success
    message
    reservationId
  }
}
```

---

## ✅ Validation

- ✅ Compilation Maven : **SUCCESS**
- ✅ Build JAR : **SUCCESS**
- ✅ Schéma GraphQL créé
- ✅ Resolvers implémentés
- ✅ Configuration GraphQL activée
- ✅ Ancien code REST sauvegardé (.old)
- ✅ Base de données H2 conservée
- ✅ Images conservées
- ✅ Multi-profils fonctionnel

---

## 🎉 Conclusion

**Le module Hotellerie est maintenant 100% GraphQL** ! 

Les fondations sont en place pour migrer les modules Agence et Client. La migration a été effectuée en conservant toute la logique métier, la base de données H2, et l'architecture multi-profils.

**Date de complétion** : 4 janvier 2026  
**Temps estimé** : ~3 heures (module 1/3)

