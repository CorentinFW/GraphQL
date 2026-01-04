# 🔄 TODO LIST - Migration REST vers GraphQL

## 📋 Vue d'ensemble de la migration

Ce document détaille toutes les étapes nécessaires pour migrer le système de réservation d'hôtel de **REST** vers **GraphQL**, tout en conservant :
- ✅ Base de données H2
- ✅ Interface Swing (GUI)
- ✅ Architecture multi-agences et multi-hôtels
- ✅ Toutes les fonctionnalités actuelles

---

## 🏗️ Architecture actuelle vs cible

### Architecture REST actuelle
```
Client Swing
    ↓ (REST)
Agence 1 & 2 (REST Controllers)
    ↓ (REST)
Hôtels Paris/Lyon/Montpellier (REST Controllers)
    ↓
H2 Database
```

### Architecture GraphQL cible
```
Client Swing
    ↓ (GraphQL)
Agence 1 & 2 (GraphQL Resolvers)
    ↓ (GraphQL)
Hôtels Paris/Lyon/Montpellier (GraphQL Resolvers)
    ↓
H2 Database
```

---

## 📦 MODULE 1 : HOTELLERIE (Service Hôtel)

### 1.1 Configuration Maven (pom.xml)

- [ ] **Ajouter les dépendances GraphQL Spring Boot**
  ```xml
  <!-- GraphQL Spring Boot Starter -->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-graphql</artifactId>
  </dependency>
  
  <!-- GraphQL pour les tests -->
  <dependency>
      <groupId>org.springframework.graphql</groupId>
      <artifactId>spring-graphql-test</artifactId>
      <scope>test</scope>
  </dependency>
  ```

- [ ] **Retirer les dépendances REST obsolètes**
  - Supprimer `springdoc-openapi-ui` (Swagger/OpenAPI)
  - Conserver `spring-boot-starter-web` (nécessaire pour GraphQL)

### 1.2 Schéma GraphQL

- [ ] **Créer le dossier `src/main/resources/graphql/`**

- [ ] **Créer le fichier `hotel.graphqls`** avec les types suivants :
  
  ```graphql
  type Query {
    # Informations de l'hôtel
    hotelInfo: HotelInfo!
    
    # Rechercher des chambres disponibles
    rechercherChambres(criteres: RechercheInput!): [Chambre!]!
    
    # Obtenir une chambre par ID
    chambre(id: ID!): Chambre
    
    # Lister toutes les réservations
    reservations: [Reservation!]!
  }
  
  type Mutation {
    # Créer une réservation
    creerReservation(reservation: ReservationInput!): ReservationResponse!
    
    # Annuler une réservation
    annulerReservation(reservationId: ID!): Boolean!
  }
  
  type HotelInfo {
    nom: String!
    adresse: String!
    ville: String
    telephone: String
  }
  
  type Chambre {
    id: ID!
    nom: String!
    prix: Float!
    nbrDeLit: Int!
    nbrEtoile: Int!
    disponible: Boolean!
    imageUrl: String
    hotelNom: String
  }
  
  type Reservation {
    id: ID!
    chambreId: ID!
    nomClient: String!
    prenomClient: String!
    emailClient: String!
    telephoneClient: String
    dateArrive: String!
    dateDepart: String!
    prixTotal: Float!
  }
  
  input RechercheInput {
    adresse: String
    dateArrive: String!
    dateDepart: String!
    prixMin: Float
    prixMax: Float
    nbrEtoile: Int
    nbrLits: Int
  }
  
  input ReservationInput {
    chambreId: ID!
    nomClient: String!
    prenomClient: String!
    emailClient: String!
    telephoneClient: String
    dateArrive: String!
    dateDepart: String!
  }
  
  type ReservationResponse {
    success: Boolean!
    message: String!
    reservationId: ID
  }
  ```

### 1.3 Resolvers GraphQL

- [ ] **Créer le package `org.tp1.hotellerie.graphql`**

- [ ] **Créer `HotelQueryResolver.java`**
  - Implémenter `hotelInfo()` → remplace `GET /api/hotel/info`
  - Implémenter `rechercherChambres()` → remplace `POST /api/hotel/chambres/rechercher`
  - Implémenter `chambre(id)` → nouvelle fonctionnalité
  - Implémenter `reservations()` → remplace `GET /api/hotel/reservations`
  - Annoter avec `@QueryMapping`

- [ ] **Créer `HotelMutationResolver.java`**
  - Implémenter `creerReservation()` → remplace `POST /api/hotel/reservations`
  - Implémenter `annulerReservation()` → nouvelle fonctionnalité
  - Annoter avec `@MutationMapping`

### 1.4 Adaptation des DTOs

- [ ] **Modifier les DTOs existants pour GraphQL**
  - Vérifier que `ChambreDTO`, `ReservationRequest`, etc. sont compatibles
  - Ajouter les annotations `@NonNull` si nécessaire
  - Les DTOs peuvent rester identiques ou être simplifiés

### 1.5 Configuration GraphQL

- [ ] **Créer `src/main/resources/application.properties` avec configuration GraphQL**
  ```properties
  # GraphQL configuration
  spring.graphql.graphiql.enabled=true
  spring.graphql.graphiql.path=/graphiql
  spring.graphql.path=/graphql
  
  # Conserver la config H2 existante
  spring.datasource.url=jdbc:h2:file:./data/hotellerie-${spring.profiles.active}-db
  spring.jpa.hibernate.ddl-auto=update
  ```

### 1.6 Suppression du code REST

- [ ] **Supprimer `HotelController.java`** (remplacé par les Resolvers)
- [ ] **Supprimer le package `config/` si contient uniquement config REST**
- [ ] **Conserver** :
  - `HotelService.java` (logique métier)
  - Tous les `model/*` (entités JPA)
  - Tous les `repository/*` (JPA repositories)
  - Les DTOs (adapter si nécessaire)

### 1.7 Tests

- [ ] **Créer `HotelGraphQLTest.java`**
  - Tester les queries (rechercherChambres, hotelInfo)
  - Tester les mutations (creerReservation)
  - Utiliser `@GraphQlTest` et `GraphQlTester`

---

## 📦 MODULE 2 : AGENCE (Service Agence)

### 2.1 Configuration Maven (pom.xml)

- [ ] **Ajouter les dépendances GraphQL**
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-graphql</artifactId>
  </dependency>
  
  <!-- GraphQL Client pour appeler les hôtels -->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-webflux</artifactId>
  </dependency>
  ```

- [ ] **Retirer `springdoc-openapi-ui`**

### 2.2 Schéma GraphQL

- [ ] **Créer `src/main/resources/graphql/agence.graphqls`**
  
  ```graphql
  type Query {
    # Ping pour tester la disponibilité
    ping: PingResponse!
    
    # Rechercher dans tous les hôtels partenaires
    rechercherChambres(criteres: RechercheInput!): [Chambre!]!
    
    # Obtenir les réservations d'un hôtel
    reservationsHotel(hotelNom: String!): [Reservation!]!
    
    # Obtenir toutes les réservations
    toutesReservations: [Reservation!]!
  }
  
  type Mutation {
    # Effectuer une réservation via l'agence
    effectuerReservation(reservation: ReservationAgenceInput!): ReservationResponse!
  }
  
  type PingResponse {
    message: String!
    status: String!
    timestamp: String!
  }
  
  type Chambre {
    id: ID!
    nom: String!
    prix: Float!
    nbrDeLit: Int!
    nbrEtoile: Int!
    disponible: Boolean!
    imageUrl: String
    hotelNom: String!
    hotelAdresse: String!
    prixOriginal: Float
    coefficient: Float
  }
  
  type Reservation {
    id: ID!
    chambreId: ID!
    hotelNom: String!
    nomClient: String!
    prenomClient: String!
    emailClient: String!
    telephoneClient: String
    dateArrive: String!
    dateDepart: String!
    prixTotal: Float!
  }
  
  input RechercheInput {
    adresse: String
    dateArrive: String!
    dateDepart: String!
    prixMin: Float
    prixMax: Float
    nbrEtoile: Int
    nbrLits: Int
  }
  
  input ReservationAgenceInput {
    chambreId: ID!
    hotelAdresse: String!
    nomClient: String!
    prenomClient: String!
    emailClient: String!
    telephoneClient: String
    dateArrive: String!
    dateDepart: String!
  }
  
  type ReservationResponse {
    success: Boolean!
    message: String!
    reservationId: ID
    hotelNom: String
  }
  ```

### 2.3 Client GraphQL pour les Hôtels

- [ ] **Créer `HotelGraphQLClient.java`** (remplace `HotelRestClient.java`)
  - Utiliser `GraphQlClient` ou `WebClient` pour les appels GraphQL
  - Implémenter `rechercherChambres(hotelUrl, criteres)`
  - Implémenter `creerReservation(hotelUrl, reservation)`
  - Implémenter `getReservations(hotelUrl)`

- [ ] **Créer `MultiHotelGraphQLClient.java`** (remplace `MultiHotelRestClient.java`)
  - Agréger les résultats de plusieurs hôtels en parallèle
  - Appliquer les coefficients de prix de l'agence

### 2.4 Resolvers GraphQL

- [ ] **Créer `AgenceQueryResolver.java`**
  - Implémenter `ping()`
  - Implémenter `rechercherChambres()` → appelle tous les hôtels via GraphQL
  - Implémenter `reservationsHotel()`
  - Implémenter `toutesReservations()`

- [ ] **Créer `AgenceMutationResolver.java`**
  - Implémenter `effectuerReservation()` → route vers le bon hôtel

### 2.5 Service Agence

- [ ] **Modifier `AgenceService.java`**
  - Adapter pour utiliser `HotelGraphQLClient` au lieu de `HotelRestClient`
  - Conserver la logique métier (agrégation, coefficients)

### 2.6 Configuration

- [ ] **Ajouter dans `application.properties`**
  ```properties
  spring.graphql.graphiql.enabled=true
  spring.graphql.path=/graphql
  
  # URLs des hôtels (GraphQL endpoints)
  hotels.paris.url=http://localhost:8082/graphql
  hotels.lyon.url=http://localhost:8083/graphql
  hotels.montpellier.url=http://localhost:8084/graphql
  ```

### 2.7 Suppression du code REST

- [ ] **Supprimer `AgenceController.java`**
- [ ] **Supprimer `client/HotelRestClient.java`** → remplacé par `HotelGraphQLClient`
- [ ] **Supprimer `client/MultiHotelRestClient.java`**
- [ ] **Conserver `AgenceService.java`** (adapter)

### 2.8 Tests

- [ ] **Créer `AgenceGraphQLTest.java`**
  - Tester `rechercherChambres` avec plusieurs hôtels
  - Tester `effectuerReservation`
  - Mocker les appels aux hôtels

---

## 📦 MODULE 3 : CLIENT (Interface Swing)

### 3.1 Configuration Maven (pom.xml)

- [ ] **Ajouter le client GraphQL**
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-graphql</artifactId>
  </dependency>
  
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-webflux</artifactId>
  </dependency>
  
  <!-- Conserver Swing -->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter</artifactId>
  </dependency>
  ```

### 3.2 Client GraphQL pour les Agences

- [ ] **Créer `AgenceGraphQLClient.java`** (remplace `AgenceRestClient.java`)
  - Utiliser `GraphQlClient` de Spring
  - Implémenter `ping(agenceUrl)`
  - Implémenter `rechercherChambres(agenceUrl, criteres)` avec GraphQL query
  - Implémenter `effectuerReservation(agenceUrl, reservation)` avec GraphQL mutation

- [ ] **Créer `MultiAgenceGraphQLClient.java`** (remplace `MultiAgenceRestClient.java`)
  - Interroger les 2 agences en parallèle via GraphQL
  - Agréger les résultats

### 3.3 Adaptation de l'interface Swing

- [ ] **Modifier `ClientGUI.java`**
  - Injecter `MultiAgenceGraphQLClient` au lieu de `MultiAgenceRestClient`
  - Les appels deviennent des queries/mutations GraphQL
  - **Aucun changement visuel** : l'interface reste identique

- [ ] **Vérifier que les DTOs sont compatibles**
  - `ChambreDTO.java`
  - `RechercheRequest.java`
  - `ReservationRequest.java`
  - `ReservationResponse.java`

### 3.4 Configuration

- [ ] **Modifier `application.properties`**
  ```properties
  # URLs des agences (GraphQL endpoints)
  agence1.url=http://localhost:8081/graphql
  agence2.url=http://localhost:8085/graphql
  agence1.name=Paris Voyages
  agence2.name=Sud Réservations
  ```

### 3.5 Suppression du code REST

- [ ] **Supprimer `rest/AgenceRestClient.java`**
- [ ] **Supprimer `rest/MultiAgenceRestClient.java`**
- [ ] **Conserver l'interface Swing** complètement (aucun changement visuel)

### 3.6 Tests

- [ ] **Tester manuellement l'interface graphique**
  - Recherche de chambres
  - Affichage des images
  - Réservation
  - Comparaison de prix multi-agences

---

## 🔧 TÂCHES TRANSVERSALES

### 4.1 Scripts de démarrage

- [ ] **Vérifier que les scripts continuent de fonctionner**
  - `rest-restart.sh` → peut être renommé en `graphql-restart.sh`
  - `rest-all-restart.sh` → `graphql-all-restart.sh`
  - `rest-client.sh` → `graphql-client.sh`
  - `arreter-services.sh` → inchangé

- [ ] **Mettre à jour les ports dans les scripts**
  - Vérifier que les endpoints pointent vers `/graphql` au lieu de `/api/*`

### 4.2 Documentation

- [ ] **Créer `GRAPHQL-GUIDE.md`**
  - Expliquer l'architecture GraphQL
  - Donner des exemples de queries/mutations
  - Documenter les schémas GraphQL

- [ ] **Mettre à jour `README.md`**
  - Remplacer "REST" par "GraphQL"
  - Ajouter des exemples de queries GraphQL
  - Documenter GraphiQL (interface de test intégrée)

- [ ] **Créer des exemples de queries**
  ```graphql
  # Exemple : Rechercher des chambres à Lyon
  query {
    rechercherChambres(criteres: {
      adresse: "Lyon"
      dateArrive: "2025-12-01"
      dateDepart: "2025-12-05"
      prixMax: 200
    }) {
      id
      nom
      prix
      hotelNom
      imageUrl
    }
  }
  
  # Exemple : Réserver une chambre
  mutation {
    creerReservation(reservation: {
      chambreId: "1"
      nomClient: "Dupont"
      prenomClient: "Jean"
      emailClient: "jean.dupont@example.com"
      dateArrive: "2025-12-01"
      dateDepart: "2025-12-05"
    }) {
      success
      message
      reservationId
    }
  }
  ```

### 4.3 Tests d'intégration

- [ ] **Créer des tests end-to-end**
  - Test complet : Client → Agence → Hôtel
  - Vérifier que les données H2 sont bien persistées
  - Tester les scénarios multi-agences

### 4.4 GraphiQL (Interface de test)

- [ ] **Configurer GraphiQL pour chaque service**
  - Hôtel Paris : http://localhost:8082/graphiql
  - Hôtel Lyon : http://localhost:8083/graphiql
  - Hôtel Montpellier : http://localhost:8084/graphiql
  - Agence 1 : http://localhost:8081/graphiql
  - Agence 2 : http://localhost:8085/graphiql

- [ ] **Documenter l'utilisation de GraphiQL**
  - Interface web pour tester les queries
  - Auto-complétion et documentation intégrée

### 4.5 Gestion des erreurs

- [ ] **Implémenter un gestionnaire d'erreurs GraphQL global**
  - Créer `GraphQLExceptionHandler.java` dans chaque module
  - Gérer les erreurs de validation
  - Gérer les erreurs de disponibilité des chambres
  - Retourner des messages d'erreur clairs

### 4.6 Logging

- [ ] **Adapter les logs existants**
  - Les logs dans `logs/*.log` doivent continuer de fonctionner
  - Ajouter des logs spécifiques GraphQL si nécessaire

### 4.7 Performance

- [ ] **Optimiser les requêtes GraphQL**
  - Implémenter DataLoader si nécessaire (éviter N+1 queries)
  - Optimiser les appels parallèles aux hôtels
  - Conserver le cache si présent

---

## 📊 ORDRE D'EXÉCUTION RECOMMANDÉ

### Phase 1 : Hôtels (Fondation)
1. ✅ Configurer Maven (dépendances GraphQL)
2. ✅ Créer les schémas GraphQL (`hotel.graphqls`)
3. ✅ Implémenter les Resolvers (Query & Mutation)
4. ✅ Tester avec GraphiQL
5. ✅ Supprimer le code REST

**Durée estimée : 2-3 heures par hôtel (faire Paris en premier, puis dupliquer)**

### Phase 2 : Agences (Intégration)
1. ✅ Configurer Maven
2. ✅ Créer les schémas GraphQL (`agence.graphqls`)
3. ✅ Implémenter `HotelGraphQLClient` (appels aux hôtels)
4. ✅ Implémenter les Resolvers
5. ✅ Tester l'agrégation multi-hôtels
6. ✅ Supprimer le code REST

**Durée estimée : 3-4 heures par agence**

### Phase 3 : Client (Interface finale)
1. ✅ Configurer Maven
2. ✅ Implémenter `AgenceGraphQLClient`
3. ✅ Adapter l'interface Swing (injection du nouveau client)
4. ✅ Tester l'interface graphique complète
5. ✅ Supprimer le code REST

**Durée estimée : 2-3 heures**

### Phase 4 : Tests & Documentation
1. ✅ Tests d'intégration complets
2. ✅ Mettre à jour la documentation
3. ✅ Créer des exemples de queries
4. ✅ Vérifier les scripts de démarrage

**Durée estimée : 2-3 heures**

---

## 🎯 POINTS D'ATTENTION

### ⚠️ Ce qui RESTE IDENTIQUE
- ✅ Base de données H2 et JPA (aucun changement)
- ✅ Entités `@Entity` (Chambre, Reservation, Client, Hotel)
- ✅ Repositories JPA (aucun changement)
- ✅ Services métier (logique conservée, seulement l'interface change)
- ✅ Interface Swing (visuel identique, seul le client HTTP change)
- ✅ Architecture multi-agences et multi-hôtels

### 🔄 Ce qui CHANGE
- ❌ Controllers REST → ✅ Resolvers GraphQL
- ❌ Endpoints `/api/*` → ✅ Endpoint unique `/graphql`
- ❌ RestTemplate/RestClient → ✅ GraphQL Client
- ❌ Swagger/OpenAPI → ✅ GraphiQL
- ❌ Multiples endpoints → ✅ Un seul endpoint avec queries/mutations

### 🆕 AVANTAGES de GraphQL
- ✅ **Un seul endpoint** par service
- ✅ **Le client demande exactement ce dont il a besoin** (pas de sur-fetching)
- ✅ **Typage fort** avec le schéma GraphQL
- ✅ **GraphiQL** : interface de test intégrée et auto-documentée
- ✅ **Introspection** : le schéma est auto-documenté
- ✅ **Moins de requêtes réseau** : agréger plusieurs requêtes en une
- ✅ **Versionning simplifié** : pas besoin de `/v1`, `/v2`

---

## 📚 RESSOURCES

### Documentation Spring GraphQL
- https://spring.io/projects/spring-graphql
- https://docs.spring.io/spring-graphql/reference/

### GraphQL Java
- https://www.graphql-java.com/

### Tutoriels
- Spring Boot + GraphQL : https://www.baeldung.com/spring-graphql
- GraphQL Schema Design : https://graphql.org/learn/schema/

### Outils de test
- GraphiQL (inclus avec Spring GraphQL)
- Postman (supporte GraphQL)
- Altair GraphQL Client (extension Chrome)

---

## ✅ CHECKLIST FINALE

Avant de considérer la migration terminée :

- [ ] Tous les modules compilent sans erreur
- [ ] GraphiQL fonctionne sur tous les services
- [ ] L'interface Swing affiche les 20 chambres
- [ ] La recherche fonctionne (Lyon, Paris, Montpellier)
- [ ] Les réservations fonctionnent
- [ ] Les images s'affichent
- [ ] Les prix avec coefficients sont corrects
- [ ] Les bases H2 persistent les données
- [ ] Les logs sont corrects
- [ ] Les scripts de démarrage fonctionnent
- [ ] La documentation est à jour
- [ ] Tous les tests passent

---

## 📝 NOTES IMPORTANTES

1. **Compatibilité** : GraphQL cohabite avec REST pendant la migration si besoin
2. **Rollback** : Garder une branche Git avec le code REST avant de supprimer
3. **Tests** : Tester chaque module indépendamment avant l'intégration
4. **Documentation** : Documenter les schémas GraphQL au fur et à mesure

---

## 🚀 DURÉE TOTALE ESTIMÉE

- **Phase 1 (Hôtels)** : 6-8 heures (3 hôtels)
- **Phase 2 (Agences)** : 6-8 heures (2 agences)
- **Phase 3 (Client)** : 2-3 heures
- **Phase 4 (Tests/Doc)** : 2-3 heures

**TOTAL : 16-22 heures de travail**

---

*Document créé le 4 janvier 2026 - Migration REST → GraphQL*

