# 🔧 IMPLÉMENTATION RÉSERVATIONS - ÉTAPE PAR ÉTAPE

**Date:** 2026-01-05  
**Objectif:** Ajouter la fonctionnalité "Voir les réservations" en testant à chaque étape

---

## 📋 PLAN D'ACTION

### Étape 1 : Couche Hôtel (Backend)
**Objectif:** L'hôtel peut retourner ses réservations via GraphQL

**Fichiers à modifier:**
1. `Hotellerie/src/main/resources/graphql/hotel.graphqls` - Vérifier le type Reservation
2. `Hotellerie/src/main/java/.../graphql/HotelQueryResolver.java` - Ajouter query reservations()
3. `Hotellerie/src/main/java/.../service/HotelService.java` - Ajouter getToutesReservations()

**Test:** Curl direct sur l'hôtel
```bash
curl -X POST http://localhost:8083/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ reservations { id dateArrive dateDepart } }"}'
```

**Critère de succès:** Retourne les réservations (même si certains champs sont null)

---

### Étape 2 : Créer un ReservationDTO
**Objectif:** Mapper Reservation JPA → ReservationDTO pour GraphQL

**Nouveau fichier:**
- `Hotellerie/src/main/java/.../dto/ReservationDTO.java`

**Fichiers à modifier:**
- `HotelQueryResolver.java` - Retourner List<ReservationDTO> au lieu de List<Reservation>
- `HotelService.java` - Mapper vers DTO

**Test:** Même curl qu'étape 1

**Critère de succès:** Tous les champs sont remplis (pas de null)

---

### Étape 3 : Couche Agence (Intermédiaire)
**Objectif:** L'agence interroge ses hôtels pour les réservations

**Fichiers à modifier:**
1. `Agence/src/main/java/.../client/HotelGraphQLClient.java` - Ajouter getReservations()
2. `Agence/src/main/java/.../client/MultiHotelGraphQLClient.java` - Ajouter getAllReservations()
3. `Agence/src/main/java/.../graphql/AgenceQueryResolver.java` - Implémenter toutesReservations()

**Test:** Curl sur l'agence
```bash
curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ toutesReservations { id nomClient hotelNom } }"}'
```

**Critère de succès:** Retourne les réservations de tous les hôtels partenaires

---

### Étape 4 : Couche Client (Frontend)
**Objectif:** Le client interroge les 2 agences et affiche dans la GUI

**Fichiers à modifier:**
1. `Client/src/main/java/.../graphql/AgenceGraphQLClient.java` - Ajouter getReservations()
2. `Client/src/main/java/.../graphql/MultiAgenceGraphQLClient.java` - Implémenter getAllReservations()

**Test:** Cliquer sur "Voir réservations" dans la GUI

**Critère de succès:** Affiche les réservations groupées par agence

---

## 🚦 PROCÉDURE DE TEST À CHAQUE ÉTAPE

### 1. Modifier les fichiers
### 2. Recompiler UNIQUEMENT le module concerné
```bash
cd Hotellerie && mvn clean package -DskipTests -q
# OU
cd Agence && mvn clean package -DskipTests -q
# OU
cd Client && mvn clean package -DskipTests -q
```

### 3. Relancer UNIQUEMENT les services concernés
```bash
# Pour hôtels
pkill -f "Hotellerie"
cd Hotellerie
java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=lyon > ../logs/hotel-lyon.log 2>&1 &

# Pour agences
pkill -f "Agence"
cd Agence
java -jar target/Agence-0.0.1-SNAPSHOT.jar --spring.profiles.active=agence1 > ../logs/agence1.log 2>&1 &
```

### 4. Tester avec curl OU la GUI

### 5. SI ÇA MARCHE → Passer à l'étape suivante
### 6. SI ÇA CASSE → STOP, analyser les logs et corriger

---

## 📝 JOURNAL DES ÉTAPES

### ⏳ Étape 1 : Couche Hôtel
- [ ] Fichiers modifiés
- [ ] Compilé
- [ ] Relancé
- [ ] Testé
- [ ] ✅ Validé / ❌ Échec

### ⏳ Étape 2 : ReservationDTO
- [ ] Fichiers modifiés
- [ ] Compilé
- [ ] Relancé
- [ ] Testé
- [ ] ✅ Validé / ❌ Échec

### ⏳ Étape 3 : Couche Agence
- [ ] Fichiers modifiés
- [ ] Compilé
- [ ] Relancé
- [ ] Testé
- [ ] ✅ Validé / ❌ Échec

### ⏳ Étape 4 : Couche Client
- [ ] Fichiers modifiés
- [ ] Compilé
- [ ] Relancé
- [ ] Testé
- [ ] ✅ Validé / ❌ Échec

---

**PRÊT À COMMENCER L'ÉTAPE 1 !**

