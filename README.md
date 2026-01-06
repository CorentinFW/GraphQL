# 🏨 Système de Réservation Multi-Agences GraphQL

## 📖 Description

Système distribué de réservation de chambres d'hôtel utilisant **GraphQL** pour la communication entre les services. Le projet permet la recherche et la réservation de chambres via une interface graphique Swing, en interrogeant plusieurs agences qui communiquent avec leurs hôtels partenaires via GraphQL.

**Architecture distribuée :**
- 3 hôtels (Paris, Lyon, Montpellier) exposant des APIs GraphQL
- 2 agences (Paris Voyages, Sud Réservations) agrégeant les données via GraphQL
- 1 client GUI (Swing) consommant les APIs GraphQL des agences
- Bases de données H2 embarquées pour la persistance

---

## 🚀 Quick Start

### Lancer tout le système

```bash
# Démarrage complet avec réinitialisation des bases de données H2
./graphQL-restart.sh

# OU : Démarrage sans réinitialisation (conserve les réservations)
./graphQL-service.sh
```

**⏱️ Temps de démarrage :** ~30 secondes

**✅ Résultat :** Une fenêtre graphique s'ouvre avec les 20 chambres disponibles !

### Lancer uniquement le client

```bash
# Les services backend doivent être déjà lancés
./graphQL-client.sh
```

### Arrêter le système

```bash
./arret-graphQL.sh
```

---

## 🖥️ Interface Swing - Guide d'utilisation

### 1. Rechercher des chambres

1. **Remplir les critères de recherche :**
   - Ville (optionnel) : Lyon, Paris, Montpellier
   - Date d'arrivée (obligatoire)
   - Date de départ (obligatoire)
   - Nombre de personnes (optionnel)
   - Prix maximum (optionnel)

2. **Cliquer sur "🔍 Rechercher"**

3. **Les résultats s'affichent dans le tableau** avec :
   - Nom de la chambre
   - Hôtel et adresse
   - Prix total
   - Agence proposant l'offre

**Exemple :** Recherche "Lyon" du 2025-11-11 au 2025-11-15 → 10 chambres trouvées

### 2. Réserver une chambre

1. **Sélectionner une chambre** dans le tableau (clic simple)
2. **Cliquer sur "📝 Réserver"** (ou double-clic sur la ligne)
3. **Remplir le formulaire de réservation :**
   - Nom (obligatoire)
   - Prénom (obligatoire)
   - Email (obligatoire)
   - Téléphone (optionnel)
   - Moyen de paiement (obligatoire)
4. **Valider**

→ Confirmation instantanée avec le numéro de réservation !

### 3. Voir les réservations

1. **Cliquer sur "👁️ Voir Réservations"** (ou menu Actions → Voir réservations)
2. **Toutes les réservations s'affichent** avec :
   - ID de réservation
   - Client (nom, prénom)
   - Hôtel
   - Dates
   - Prix total

### 4. Raccourcis clavier

- **Ctrl+R** : Rechercher
- **Ctrl+B** : Réserver
- **Ctrl+V** : Voir les réservations
- **Ctrl+Q** : Quitter

---

## 📁 Structure du Projet

```
GraphQL/
├── Hotellerie/              # Module des hôtels (GraphQL Server)
│   ├── src/main/
│   │   ├── java/
│   │   │   └── org/tp1/hotellerie/
│   │   │       ├── controller/      # Resolvers GraphQL
│   │   │       ├── service/         # Logique métier
│   │   │       ├── model/           # Entités JPA
│   │   │       ├── repository/      # DAO
│   │   │       └── dto/             # DTOs GraphQL
│   │   └── resources/
│   │       ├── graphql/             # Schémas GraphQL (.graphqls)
│   │       └── application*.properties
│   └── data/                        # Bases H2 (lyon, paris, montpellier)
│
├── Agence/                  # Module des agences (GraphQL Server + Client)
│   ├── src/main/
│   │   ├── java/
│   │   │   └── org/tp1/agence/
│   │   │       ├── controller/      # Resolvers GraphQL
│   │   │       ├── service/         # Agrégation GraphQL
│   │   │       ├── graphql/         # Client GraphQL (vers hôtels)
│   │   │       └── dto/             # DTOs
│   │   └── resources/
│   │       ├── graphql/             # Schémas GraphQL
│   │       └── application*.properties
│
├── Client/                  # Module client GUI (GraphQL Client)
│   ├── src/main/
│   │   └── java/
│   │       └── org/tp1/client/
│   │           ├── gui/             # Interface Swing
│   │           ├── graphql/         # Client GraphQL (vers agences)
│   │           └── model/           # Modèles locaux
│
├── logs/                    # Logs des services
├── old/                     # Anciens fichiers de migration
│
├── graphQL-restart.sh       # Démarrage complet (avec reset BDD)
├── graphQL-service.sh       # Démarrage services (sans reset BDD)
├── graphQL-client.sh        # Lancement client GUI uniquement
└── arret-graphQL.sh         # Arrêt de tous les services
```

### Architecture distribuée

```
┌─────────────────────────────────────────────────────────┐
│                   CLIENT GUI (Swing)                    │
│              GraphQL Client (HTTP POST)                 │
└──────────────────┬──────────────────┬───────────────────┘
                   │                  │
         ┌─────────▼────────┐  ┌──────▼──────────┐
         │  AGENCE 1 :8081  │  │  AGENCE 2 :8085 │
         │  Paris Voyages   │  │ Sud Réservations│
         │ GraphQL Server   │  │ GraphQL Server  │
         └─────┬─────┬──────┘  └──────┬────┬─────┘
               │     │                │    │
      ┌────────▼─┐ ┌─▼───────┐  ┌────▼──┐ │
      │ HOTEL    │ │ HOTEL   │  │ HOTEL │ │
      │ Paris    │ │ Lyon    │◄─┤ Lyon  │ │
      │ :8082    │ │ :8083   │  │ :8083 │ │
      │ GraphQL  │ │ GraphQL │  │       │ │
      └──────────┘ └─────────┘  └───────┘ │
                                           │
                               ┌───────────▼────┐
                               │ HOTEL          │
                               │ Montpellier    │
                               │ :8084          │
                               │ GraphQL        │
                               └────────────────┘
```

**Points clés :**
- Hotel Lyon (:8083) est partagé entre les 2 agences
- Communication 100% GraphQL (plus de REST)
- Chaque service expose son propre schéma GraphQL

---

## ✨ Fonctionnalités

### ✅ Recherche de chambres
- Critères multiples (ville, dates, prix, nombre de personnes)
- Agrégation temps réel des résultats de plusieurs agences
- Affichage comparatif des prix

### ✅ Réservation
- Formulaire complet avec validation
- Confirmation instantanée
- Attribution d'un ID de réservation unique

### ✅ Consultation des réservations
- Liste complète des réservations effectuées
- Détails complets (client, hôtel, dates, prix)

### ✅ Interface utilisateur
- Interface graphique Swing moderne
- Console de logs intégrée
- Menus et raccourcis clavier
- Formulaires validés

### ✅ Persistance
- Bases de données H2 embarquées
- 1 base par hôtel (lyon, paris, montpellier)
- Données conservées entre les redémarrages

---

## 🛠️ Langages et Technologies

### Backend
- **Java 17** (compatible jusqu'à Java 21)
- **Spring Boot 2.7.5**
- **Spring for GraphQL 1.1.0** - Serveur GraphQL
- **GraphQL Java** - Implémentation GraphQL
- **H2 Database** - Base de données embarquée
- **Spring Data JPA** - Persistance
- **Lombok** - Réduction du code boilerplate
- **Maven** - Gestion des dépendances

### Frontend
- **Java Swing** - Interface graphique
- **HTTP Client (java.net.http)** - Client GraphQL

### GraphQL
- **Queries** : Recherche de chambres, consultation de réservations
- **Mutations** : Création de réservations
- **Schema-First Design** : Fichiers `.graphqls`

### DevOps
- **Bash Scripts** - Automatisation du démarrage/arrêt
- **Logs** - Fichiers de logs dédiés par service

---

## 📊 Ports et Services

| Service | Port | Type | GraphQL Endpoint |
|---------|------|------|------------------|
| Hotel Paris | 8082 | Server | http://localhost:8082/graphql |
| Hotel Lyon | 8083 | Server | http://localhost:8083/graphql |
| Hotel Montpellier | 8084 | Server | http://localhost:8084/graphql |
| Agence Paris Voyages | 8081 | Server | http://localhost:8081/graphql |
| Agence Sud Réservations | 8085 | Server | http://localhost:8085/graphql |
| Client GUI | - | Client | Consomme les agences |

---

## 📝 Logs

Les logs de chaque service sont disponibles dans le dossier `logs/` :

```bash
# Voir les logs en temps réel
tail -f logs/hotel-paris.log
tail -f logs/hotel-lyon.log
tail -f logs/hotel-montpellier.log
tail -f logs/agence1.log
tail -f logs/agence2.log
tail -f logs/client-gui.log
```

---

## 🐛 Corrections Récentes

### Version 3.1 - 6 janvier 2026

✅ **Correction du bug de duplication des réservations Lyon**
- **Problème :** Les réservations de l'hôtel Lyon (partagé entre 2 agences) apparaissaient dans les deux agences
- **Solution :** Ajout d'un champ `agenceId` dans les réservations pour isoler les données par agence
- **Impact :** Chaque agence voit maintenant uniquement ses propres réservations

Détails complets : Voir `old/CORRECTIONS-BUGS.md`

---

## 🔧 Maintenance

### Recompiler les modules

```bash
# Recompilation complète
cd Hotellerie && mvn clean package -DskipTests && cd ..
cd Agence && mvn clean package -DskipTests && cd ..
cd Client && mvn clean package -DskipTests && cd ..
```

### Réinitialiser les bases de données

```bash
# Suppression des fichiers H2
rm -f Hotellerie/data/*.db

# Relancer avec réinitialisation
./graphQL-restart.sh
```

---

## 📚 Documentation

Consultez le dossier `old/` pour les fichiers de migration et documentation technique :
- Guides de migration REST → GraphQL
- Notes techniques sur l'implémentation
- Scripts de test

---

## ✅ Version

- **Version :** 3.0 - Architecture GraphQL
- **Date :** Janvier 2026
- **Architecture :** GraphQL avec Spring Boot
- **Interface :** Java Swing
- **Statut :** ✅ Production Ready

