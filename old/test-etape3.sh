#!/bin/bash

echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║     🧪 TEST ÉTAPE 3 - Agence interroge ses hôtels              ║"
echo "╚══════════════════════════════════════════════════════════════════╝"
echo ""

echo "⚠️  Ce test nécessite que le système COMPLET soit déjà lancé"
echo "   avec des réservations existantes."
echo ""
echo "Si besoin, relancez: ./relancer-tout.sh et faites des réservations"
echo ""
echo "Appuyez sur Entrée pour tester..."
read

echo ""
echo "🧪 Test: Appeler toutesReservations sur l'Agence 1"
echo "───────────────────────────────────────────────────────────────────"
curl -s http://localhost:8081/graphql -X POST \
  -H "Content-Type: application/json" \
  -d '{"query":"{ toutesReservations { id chambreId nomClient prenomClient hotelNom dateArrive dateDepart prixTotal } }"}' | jq .

echo ""
echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║                    RÉSULTAT DU TEST                              ║"
echo "╚══════════════════════════════════════════════════════════════════╝"
echo ""
echo "RÉSULTAT ATTENDU:"
echo "  - Toutes les réservations des hôtels partenaires de l'Agence 1"
echo "  - Pour Agence 1: hôtels Paris + Lyon"
echo "  - Le champ 'hotelNom' est enrichi par l'agence"
echo ""
echo "SI OK → ÉTAPE 3 VALIDÉE, dites-moi 'validé'"
echo "SI KO → Envoyez-moi le résultat"
echo ""

