#!/bin/bash

echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║  🧪 TEST COMPLET ÉTAPE 2 - Avec système complet relancé        ║"
echo "╚══════════════════════════════════════════════════════════════════╝"
echo ""
echo "⚠️  IMPORTANT:"
echo "Ce test va relancer TOUT le système car la base H2 est en mémoire."
echo "Les réservations sont perdues à chaque redémarrage d'hôtel."
echo ""
echo "PROCÉDURE:"
echo "1. Je relance tout le système"
echo "2. VOUS faites une réservation dans la GUI"
echo "3. VOUS testez avec: curl http://localhost:8083/graphql ..."
echo ""
echo "Appuyez sur Entrée pour continuer..."
read

echo ""
echo "1️⃣  Arrêt de tous les services..."
pkill -9 -f "java" 2>/dev/null
sleep 2

echo "2️⃣  Lancement du système complet..."
cd /home/corentinfay/Bureau/GraphQL
./relancer-tout.sh &

echo ""
echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║                    À FAIRE MAINTENANT                            ║"
echo "╚══════════════════════════════════════════════════════════════════╝"
echo ""
echo "1. Attendez que la GUI s'ouvre (~30 secondes)"
echo ""
echo "2. Dans la GUI:"
echo "   - Faites UNE réservation sur l'hôtel Lyon"
echo ""
echo "3. Puis testez dans un terminal:"
echo ""
echo "   curl -X POST http://localhost:8083/graphql \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"query\":\"{ reservations { id chambreId nomClient prenomClient emailClient dateArrive dateDepart prixTotal } }\"}' | jq ."
echo ""
echo "RÉSULTAT ATTENDU:"
echo "  Tous les champs remplis (nomClient, prenomClient, chambreId, prixTotal)"
echo ""
echo "✅ Si OK → ÉTAPE 2 VALIDÉE, dites-moi 'validé'"
echo "❌ Si KO → Envoyez-moi le résultat"
echo ""

