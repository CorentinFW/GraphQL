# 🔍 MODE DEBUG - Instructions de Test

## ✅ Changement Appliqué

**Le code est maintenant SÉQUENTIEL (pas de parallélisme)**

Cela permet de voir EXACTEMENT ce qui se passe étape par étape sans les problèmes de concurrence.

---

## 🧪 Comment Tester

### 1. Relancer les services
```bash
./rest-all-restart.sh
```

### 2. Ouvrir les logs dans un terminal séparé
```bash
tail -f logs/agence1.log
```

### 3. Dans la GUI, faire une recherche
- Dates: `2024-01-01` → `2024-01-05`
- Adresse: (VIDE)
- Cliquer sur "Rechercher"

### 4. Observer les logs

Vous devriez voir quelque chose comme:
```
🔍 Recherche GraphQL SÉQUENTIELLE dans 2 hôtels...
  → Interrogation de http://localhost:8082/graphql
    Info récupérée: Grand Hotel Paris - 10 Rue de la Paix, Paris
    5 chambre(s) reçue(s)
      Avant: Chambre Simple | hotelNom=Grand Hotel Paris | hotelAdresse=null
      Après: Chambre Simple | hotelNom=Grand Hotel Paris | hotelAdresse=10 Rue de la Paix, Paris | agence=Agence Paris Voyages
    ✓ [http://localhost:8082/graphql] 5 chambre(s) ajoutée(s)
  
  → Interrogation de http://localhost:8083/graphql
    Info récupérée: Hotel Lyon Centre - 25 Place Bellecour, Lyon
    5 chambre(s) reçue(s)
      Avant: Chambre Standard | hotelNom=Hotel Lyon Centre | hotelAdresse=null
      Après: Chambre Standard | hotelNom=Hotel Lyon Centre | hotelAdresse=25 Place Bellecour, Lyon | agence=Agence Paris Voyages
    ✓ [http://localhost:8083/graphql] 5 chambre(s) ajoutée(s)

✅ Total: 10 chambre(s) disponible(s) via GraphQL
```

---

## ❓ Questions à Vérifier dans les Logs

### Question 1: getHotelInfo() retourne-t-il les bonnes infos?
```
Info récupérée: Grand Hotel Paris - 10 Rue de la Paix, Paris  ✅ Correct
Info récupérée: Hotel Lyon Centre - 25 Place Bellecour, Lyon  ✅ Correct
```

Si INCORRECT → Problème dans HotelQueryResolver.hotelInfo()

### Question 2: hotelNom est-il correct AVANT enrichissement?
```
Avant: Chambre Simple | hotelNom=Grand Hotel Paris  ✅ Correct
```

Si hotelNom est NULL ou incorrect → Problème dans HotelQueryResolver.rechercherChambres()

### Question 3: hotelAdresse est-elle définie APRÈS?
```
Après: ... | hotelAdresse=10 Rue de la Paix, Paris  ✅ Correct
```

Si hotelAdresse reste NULL → Problème dans l'enrichissement

### Question 4: Les valeurs restent-elles stables?
- Est-ce que la chambre #1 de Paris garde bien "Grand Hotel Paris"?
- Est-ce que la chambre #1 de Lyon garde bien "Hotel Lyon Centre"?

Si les valeurs CHANGENT entre hôtels → Bug de référence partagée

---

## 📋 Résultats Attendus dans la GUI

### Si le mode séquentiel FONCTIONNE:
✅ 10 chambres affichées (pour Agence 1)
✅ 5 de Paris avec "Grand Hotel Paris"
✅ 5 de Lyon avec "Hotel Lyon Centre"
✅ Adresses correctes
✅ Agence = "Agence Paris Voyages"
✅ **Résultats IDENTIQUES à chaque recherche**

→ **Le problème venait bien du parallélisme**

### Si le mode séquentiel a ENCORE des erreurs:
❌ Les noms/adresses sont encore mélangés

→ **Le problème est ailleurs (mapping, références, etc.)**

---

## 🐛 Problèmes Possibles

### Si hotelNom change pendant le traitement
→ Les objets ChambreDTO sont partagés/réutilisés

### Si getHotelInfo() retourne les mauvaises infos
→ Cache ou singleton problématique dans HotelGraphQLClient

### Si hotelNom est NULL depuis le début
→ HotelQueryResolver ne le définit pas correctement

---

## 📝 Que Noter

Copiez-collez les lignes des logs qui montrent le problème, par exemple:
```
Avant: Chambre Simple | hotelNom=Hotel Lyon Centre | hotelAdresse=null
```
Alors que ça devrait être "Grand Hotel Paris"

Cela permettra d'identifier EXACTEMENT la cause racine.

---

**Date:** 2026-01-05  
**Mode:** DEBUG SÉQUENTIEL  
**Objectif:** Identifier la vraie cause du bug

