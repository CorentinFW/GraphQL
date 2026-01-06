#!/bin/bash

echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║     🧪 TEST ÉTAPE 2 - ReservationDTO avec tous les champs      ║"
echo "╚══════════════════════════════════════════════════════════════════╝"
echo ""

# Arrêter les hôtels
echo "1️⃣  Arrêt des hôtels..."
pkill -f "Hotellerie" 2>/dev/null
sleep 2

# Relancer hôtel Lyon
echo "2️⃣  Démarrage de l'hôtel Lyon..."
cd /home/corentinfay/Bureau/GraphQL/Hotellerie
java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=lyon > ../logs/hotel-lyon.log 2>&1 &
LYON_PID=$!

echo "   Hôtel Lyon démarré (PID: $LYON_PID)"
echo "   Attente 15 secondes pour le démarrage complet..."
sleep 15

echo ""
echo "3️⃣  Test: Appeler la query reservations avec TOUS les champs"
echo "───────────────────────────────────────────────────────────────────"
curl -s http://localhost:8083/graphql -X POST \
  -H "Content-Type: application/json" \
  -d '{"query":"{ reservations { id chambreId nomClient prenomClient emailClient dateArrive dateDepart prixTotal } }"}' | jq .

echo ""
echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║                    RÉSULTAT DU TEST                              ║"
echo "╚══════════════════════════════════════════════════════════════════╝"
echo ""
echo "SI TOUS LES CHAMPS SONT REMPLIS:"
echo "  ✅ ÉTAPE 2 VALIDÉE"
echo "  → nomClient, prenomClient, chambreId, prixTotal sont là"
echo "  → On passe à l'ÉTAPE 3 (Couche Agence)"
echo ""
echo "SI DES CHAMPS SONT ENCORE NULL:"
echo "  ⚠️  Vérifier les logs: tail -50 logs/hotel-lyon.log"
echo "  → Il y a un problème dans le mapping DTO"
echo ""

