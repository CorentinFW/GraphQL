#!/bin/bash

echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║        🧪 TEST ÉTAPE 1 - Query reservations sur l'hôtel        ║"
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
echo "3️⃣  Test 1: Vérifier que l'hôtel répond"
echo "───────────────────────────────────────────────────────────────────"
curl -s http://localhost:8083/graphql -X POST \
  -H "Content-Type: application/json" \
  -d '{"query":"{ hotelInfo { nom } }"}' | jq . 2>/dev/null || echo "❌ L'hôtel ne répond pas encore, attendez quelques secondes"

echo ""
echo "4️⃣  Test 2: Appeler la query reservations"
echo "───────────────────────────────────────────────────────────────────"
curl -s http://localhost:8083/graphql -X POST \
  -H "Content-Type: application/json" \
  -d '{"query":"{ reservations { id dateArrive dateDepart } }"}' | jq . 2>/dev/null || echo "❌ Erreur lors de l'appel"

echo ""
echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║                    RÉSULTAT DU TEST                              ║"
echo "╚══════════════════════════════════════════════════════════════════╝"
echo ""
echo "SI VOUS VOYEZ DES DONNÉES:"
echo "  ✅ ÉTAPE 1 VALIDÉE - On passe à l'ÉTAPE 2"
echo ""
echo "SI VOUS VOYEZ 'reservations: []' (liste vide):"
echo "  ℹ️  Normal si vous n'avez pas fait de réservation"
echo "  → Faites une réservation dans la GUI et relancez ce test"
echo ""
echo "SI VOUS VOYEZ DES ERREURS (nomClient null, etc):"
echo "  ⚠️  Normal pour l'ÉTAPE 1"
echo "  → On corrigera ça à l'ÉTAPE 2 avec ReservationDTO"
echo ""
echo "SI ERREUR GRAPHQL:"
echo "  ❌ Vérifier les logs: tail -50 logs/hotel-lyon.log"
echo ""

