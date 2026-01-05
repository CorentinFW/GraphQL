# 🧪 TESTS DE VALIDATION ÉTAPE PAR ÉTAPE

## ⚠️ AVANT DE COMMENCER

**IMPORTANT:** Relancez les services proprement :
```bash
cd /home/corentinfay/Bureau/GraphQL
./arreter-services.sh
./rest-all-restart.sh
```

---

## Test 1: Vérifier que les hôtels fonctionnent individuellement

### Test 1.1: Hôtel Paris (port 8082)
```bash
curl -X POST http://localhost:8082/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ rechercherChambres(criteres: { adresse: \"\" dateArrive: \"2024-01-01\" dateDepart: \"2024-01-05\" }) { id nom hotelNom } }"}' \
  | jq '.data.rechercherChambres | length'
```
**Résultat attendu:** `5`

### Test 1.2: Hôtel Lyon (port 8083)
```bash
curl -X POST http://localhost:8083/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ rechercherChambres(criteres: { adresse: \"\" dateArrive: \"2024-01-01\" dateDepart: \"2024-01-05\" }) { id nom hotelNom } }"}' \
  | jq '.data.rechercherChambres | length'
```
**Résultat attendu:** `5`

### Test 1.3: Hôtel Montpellier (port 8084)
```bash
curl -X POST http://localhost:8084/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ rechercherChambres(criteres: { adresse: \"\" dateArrive: \"2024-01-01\" dateDepart: \"2024-01-05\" }) { id nom hotelNom } }"}' \
  | jq '.data.rechercherChambres | length'
```
**Résultat attendu:** `5`

### Test 1.4: Vérifier les noms d'hôtels retournés
```bash
curl -X POST http://localhost:8082/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ rechercherChambres(criteres: { adresse: \"\" dateArrive: \"2024-01-01\" dateDepart: \"2024-01-05\" }) { id nom hotelNom } }"}' \
  | jq '.data.rechercherChambres[0].hotelNom'
```
**Résultat attendu:** `"Grand Hotel Paris"`

---

## Test 2: Vérifier que les agences fonctionnent

### Test 2.1: Agence 1 - Sans filtre
```bash
curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ rechercherChambres(criteres: { adresse: \"\" dateArrive: \"2024-01-01\" dateDepart: \"2024-01-05\" }) { id nom hotelNom hotelAdresse } }"}' \
  | jq '.data.rechercherChambres | length'
```
**Résultat attendu:** `10` (5 Paris + 5 Lyon)

### Test 2.2: Agence 2 - Sans filtre
```bash
curl -X POST http://localhost:8085/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ rechercherChambres(criteres: { adresse: \"\" dateArrive: \"2024-01-01\" dateDepart: \"2024-01-05\" }) { id nom hotelNom hotelAdresse } }"}' \
  | jq '.data.rechercherChambres | length'
```
**Résultat attendu:** `10` (5 Lyon + 5 Montpellier)

### Test 2.3: Vérifier les hotelNom et hotelAdresse
```bash
curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ rechercherChambres(criteres: { adresse: \"\" dateArrive: \"2024-01-01\" dateDepart: \"2024-01-05\" }) { nom hotelNom hotelAdresse } }"}' \
  | jq '.data.rechercherChambres[] | {nom, hotelNom, hotelAdresse}' | head -20
```
**Résultat attendu:** 
- hotelNom: "Grand Hotel Paris" ET hotelAdresse: "10 Rue de la Paix, Paris"
- hotelNom: "Hotel Lyon Centre" ET hotelAdresse: "25 Place Bellecour, Lyon"

---

## Test 3: Client GUI - Sans filtre

1. Ouvrir l'interface
2. Dates: `2024-01-01` → `2024-01-05`
3. Laisser "Adresse" VIDE
4. Rechercher

**Résultat attendu:** 20 chambres
- 5 de Paris (via Agence 1)
- 5 de Lyon (via Agence 1)
- 5 de Lyon (via Agence 2)
- 5 de Montpellier (via Agence 2)

---

## Test 4: Client GUI - Avec filtres

### Test 4.1: Recherche "Paris"
1. Adresse: `Paris`
2. Dates: `2024-01-01` → `2024-01-05`
3. Rechercher

**Résultat attendu:** 5 chambres de Paris

### Test 4.2: Recherche "Lyon"
1. Adresse: `Lyon`
2. Dates: `2024-01-01` → `2024-01-05`
3. Rechercher

**Résultat attendu:** 10 chambres de Lyon (5 via Ag1 + 5 via Ag2)

### Test 4.3: Recherche "Montpellier"
1. Adresse: `Montpellier`
2. Dates: `2024-01-01` → `2024-01-05`
3. Rechercher

**Résultat attendu:** 5 chambres de Montpellier

---

## 🐛 Si ça ne fonctionne pas

### Vérifier les logs
```bash
tail -f logs/hotel-paris.log
tail -f logs/hotel-lyon.log
tail -f logs/agence1.log
```

### Vérifier que tous les services tournent
```bash
lsof -i :8081,8082,8083,8084,8085
```
Doit montrer 5 services (3 hôtels + 2 agences)

### Arrêter et relancer proprement
```bash
./arreter-services.sh
sleep 2
./rest-all-restart.sh
```

---

## 📝 Noter les résultats

Pour chaque test, notez:
- ✅ OK
- ❌ ÉCHEC - Nombre obtenu: XXX
- ⚠️ ERREUR - Message: XXX

Cela permettra d'identifier EXACTEMENT où est le problème.

