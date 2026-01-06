#!/bin/bash

# Arrêt de tous les services GraphQL (Agences, Hôtels, Client, BDD H2)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOGS_DIR="$SCRIPT_DIR/logs"

echo "═══════════════════════════════════════════════════════════════"
echo "   Arrêt de tous les services GraphQL"
echo "═══════════════════════════════════════════════════════════════"
echo ""

# Fonction pour arrêter un service Spring Boot
stop_service() {
    local service_name=$1
    local port=$2

    echo "🛑 Arrêt de $service_name (port $port)..."

    # Trouver le PID du processus utilisant le port
    PID=$(lsof -ti:$port 2>/dev/null)

    if [ -n "$PID" ]; then
        kill -15 $PID 2>/dev/null
        sleep 2

        # Vérifier si le processus est toujours actif
        if ps -p $PID > /dev/null 2>&1; then
            echo "   ⚠ Arrêt forcé nécessaire..."
            kill -9 $PID 2>/dev/null
            sleep 1
        fi

        echo "   ✓ $service_name arrêté"
    else
        echo "   ℹ $service_name n'était pas actif"
    fi
}

# Arrêt du Client
echo "───────────────────────────────────────────────────────────────"
echo " Arrêt du Client"
echo "───────────────────────────────────────────────────────────────"
CLIENT_PID=$(pgrep -f "Client.*jar" 2>/dev/null)
if [ -n "$CLIENT_PID" ]; then
    echo "🛑 Arrêt du Client (PID: $CLIENT_PID)..."
    kill -15 $CLIENT_PID 2>/dev/null
    sleep 2
    if ps -p $CLIENT_PID > /dev/null 2>&1; then
        kill -9 $CLIENT_PID 2>/dev/null
    fi
    echo "   ✓ Client arrêté"
else
    echo "   ℹ Client n'était pas actif"
fi
echo ""

# Arrêt des Agences
echo "───────────────────────────────────────────────────────────────"
echo " Arrêt des Agences"
echo "───────────────────────────────────────────────────────────────"
stop_service "Agence 1 (Paris Voyage)" "8081"
stop_service "Agence 2 (Sud Réservation)" "8085"
echo ""

# Arrêt des Hôtels
echo "───────────────────────────────────────────────────────────────"
echo " Arrêt des Hôtels"
echo "───────────────────────────────────────────────────────────────"
stop_service "Hôtel Paris" "8082"
stop_service "Hôtel Lyon" "8083"
stop_service "Hôtel Montpellier" "8084"
echo ""

# Arrêt de la base de données H2
echo "───────────────────────────────────────────────────────────────"
echo " Arrêt de la base de données H2"
echo "───────────────────────────────────────────────────────────────"
H2_PID=$(pgrep -f "h2.*Server" 2>/dev/null)
if [ -n "$H2_PID" ]; then
    echo "🛑 Arrêt du serveur H2 (PID: $H2_PID)..."
    kill -15 $H2_PID 2>/dev/null
    sleep 2
    if ps -p $H2_PID > /dev/null 2>&1; then
        kill -9 $H2_PID 2>/dev/null
    fi
    echo "   ✓ Base de données H2 arrêtée"
else
    echo "   ℹ Base de données H2 n'était pas active"
fi
echo ""

# Vérification finale
echo "═══════════════════════════════════════════════════════════════"
echo " Vérification finale"
echo "═══════════════════════════════════════════════════════════════"

REMAINING=$(lsof -ti:8081,8082,8083,8084,8085,9092 2>/dev/null | wc -l)

if [ "$REMAINING" -eq 0 ]; then
    echo "✓ Tous les services ont été arrêtés avec succès"
else
    echo "⚠ Attention: $REMAINING processus restant(s) détecté(s)"
    echo ""
    echo "Processus restants:"
    lsof -i:8081,8082,8083,8084,8085,9092 2>/dev/null | grep LISTEN
fi

echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "   Arrêt terminé"
echo "═══════════════════════════════════════════════════════════════"

