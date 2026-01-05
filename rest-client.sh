#!/bin/bash

# Script de lancement du client GUI uniquement
# Prérequis : Les services backend (hôtels + agences) doivent être déjà lancés

echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║                                                               ║"
echo "║   LANCEMENT INTERFACE GRAPHIQUE CLIENT                        ║"
echo "║                                                               ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

cd "$(dirname "$0")"
PROJECT_ROOT=$(pwd)

# Créer le dossier logs
mkdir -p logs

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# VÉRIFICATION DES SERVICES BACKEND
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

echo "🔍 Vérification des services backend..."
echo ""

SERVICES_OK=0
SERVICES_TOTAL=5

# Fonction pour vérifier un service
check_backend_service() {
    local PORT=$1
    local NAME=$2

    # Vérifier GraphQL endpoint
    if curl -s --max-time 2 -X POST http://localhost:$PORT/graphql \
       -H "Content-Type: application/json" \
       -d '{"query":"{__typename}"}' 2>/dev/null | grep -q "data"; then
        echo "   ✅ $NAME (port $PORT) - Opérationnel"
        return 0
    else
        echo "   ❌ $NAME (port $PORT) - Inactif"
        return 1
    fi
}

# Vérifier les hôtels
check_backend_service 8082 "Hôtel Paris" && SERVICES_OK=$((SERVICES_OK + 1))
check_backend_service 8083 "Hôtel Lyon" && SERVICES_OK=$((SERVICES_OK + 1))
check_backend_service 8084 "Hôtel Montpellier" && SERVICES_OK=$((SERVICES_OK + 1))

# Vérifier les agences
check_backend_service 8081 "Agence 1 - Paris Voyages" && SERVICES_OK=$((SERVICES_OK + 1))
check_backend_service 8085 "Agence 2 - Sud Réservations" && SERVICES_OK=$((SERVICES_OK + 1))

echo ""
echo "📊 Services actifs: $SERVICES_OK / $SERVICES_TOTAL"
echo ""

if [ $SERVICES_OK -eq 0 ]; then
    echo "❌ ERREUR: Aucun service backend n'est actif !"
    echo ""
    echo "💡 Veuillez d'abord lancer les services backend avec:"
    echo "   ./rest-restart.sh          # Avec persistance des données"
    echo "   OU"
    echo "   ./rest-all-restart.sh      # Avec reset des bases de données"
    echo ""
    exit 1
elif [ $SERVICES_OK -lt $SERVICES_TOTAL ]; then
    echo "⚠️  ATTENTION: Seulement $SERVICES_OK service(s) sur $SERVICES_TOTAL sont actifs"
    echo ""
    echo "💡 Certaines fonctionnalités pourraient ne pas fonctionner"
    echo ""
    read -p "Voulez-vous continuer quand même ? (o/N) " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Oo]$ ]]; then
        echo "Annulé."
        exit 1
    fi
    echo ""
else
    echo "✅ Tous les services backend sont opérationnels !"
    echo ""
fi

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# COMPILATION DU CLIENT (si nécessaire)
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

echo "🔧 Vérification de la compilation du client..."

cd "$PROJECT_ROOT/Client"

if [ ! -f "target/Client-0.0.1-SNAPSHOT.jar" ]; then
    echo "   → Compilation nécessaire..."
    mvn clean package -DskipTests -q > "$PROJECT_ROOT/logs/compilation-client.log" 2>&1
    if [ $? -eq 0 ]; then
        echo "      ✓ Client compilé"
    else
        echo "      ✗ Erreur compilation Client (voir logs/compilation-client.log)"
        exit 1
    fi
else
    echo "   ✓ Client déjà compilé"
fi

echo ""

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# LANCEMENT DU CLIENT GUI
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

echo "═══════════════════════════════════════════════════════════════"
echo "   Lancement Interface Graphique"
echo "═══════════════════════════════════════════════════════════════"
echo ""

echo "🚀 Ouverture de l'interface graphique..."
echo ""
echo "💡 Conseil: Suivez les logs client avec:"
echo "   tail -f $PROJECT_ROOT/logs/client-gui.log"
echo ""
echo "💡 Pour voir les logs backend:"
echo "   ./voir-logs.sh all          # Tous les logs"
echo "   ./voir-logs.sh follow       # Temps réel"
echo ""

# Lancer le client GUI avec logs
MAVEN_OPTS="-Djava.awt.headless=false" mvn spring-boot:run \
  -Dspring-boot.run.arguments="--gui" \
  -Dspring-boot.run.jvmArguments="-Djava.awt.headless=false" \
  > "$PROJECT_ROOT/logs/client-gui.log" 2>&1

# Quand le client se ferme
echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "   Interface fermée"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "ℹ️  Les services backend continuent de tourner en arrière-plan"
echo ""
echo "Pour relancer le client:"
echo "   ./rest-client.sh"
echo ""
echo "Pour arrêter tous les services:"
echo "   ./arreter-services.sh"
echo ""

