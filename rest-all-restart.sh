#!/bin/bash

# Script de redémarrage complet avec reset des bases de données
# Relance : Hôtels + Agences + Client GUI
# Base de données : RESET (suppression et recréation)

echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║                                                               ║"
echo "║   REDÉMARRAGE COMPLET - RESET BASES DE DONNÉES                ║"
echo "║                                                               ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""
echo "⚠️  ATTENTION: Les bases de données H2 sont SUPPRIMÉES"
echo "    Toutes les réservations existantes sont PERDUES"
echo ""

cd "$(dirname "$0")"
PROJECT_ROOT=$(pwd)

# Créer le dossier logs
mkdir -p logs

# Fonction pour vérifier qu'un service est démarré
check_service() {
    local PORT=$1
    local SERVICE_NAME=$2
    local LOG_FILE=$3

    echo "   Vérification du démarrage..."

    for i in {1..30}; do
        if curl -s http://localhost:$PORT/actuator/health >/dev/null 2>&1 || \
           curl -s http://localhost:$PORT/api/hotel/info >/dev/null 2>&1 || \
           curl -s http://localhost:$PORT/api/agence/ping >/dev/null 2>&1; then
            echo "   ✅ $SERVICE_NAME démarré avec succès (port $PORT)"
            return 0
        fi
        sleep 1
    done

    echo "   ❌ ERREUR: $SERVICE_NAME n'a pas démarré après 30 secondes"
    echo ""
    echo "═══════════════════════════════════════════════════════════════"
    echo "   LOGS DE $SERVICE_NAME :"
    echo "═══════════════════════════════════════════════════════════════"
    tail -50 "$LOG_FILE"
    echo "═══════════════════════════════════════════════════════════════"
    return 1
}

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# ÉTAPE 1 : ARRÊT DES SERVICES
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

echo "1. Arrêt des services existants..."
pkill -f 'maven.*Hotellerie' 2>/dev/null
pkill -f 'maven.*Agence' 2>/dev/null
pkill -f 'java.*Hotellerie' 2>/dev/null
pkill -f 'java.*Agence' 2>/dev/null
sleep 3
echo "   ✓ Services arrêtés"
echo ""

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# ÉTAPE 2 : SUPPRESSION DES BASES DE DONNÉES
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

echo "2. Suppression des bases de données H2..."
rm -f Hotellerie/data/*.db 2>/dev/null
rm -f Hotellerie/data/*.mv.db 2>/dev/null
rm -f Hotellerie/data/*.trace.db 2>/dev/null
echo "   ✓ Bases de données supprimées"
echo ""

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# ÉTAPE 3 : RECOMPILATION
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

echo "3. Recompilation des modules..."
echo "   → Hotellerie..."
cd "$PROJECT_ROOT/Hotellerie"
mvn clean package -DskipTests -q > "$PROJECT_ROOT/logs/compilation-hotellerie.log" 2>&1
if [ $? -eq 0 ]; then
    echo "      ✓ Hotellerie compilé"
else
    echo "      ✗ Erreur compilation Hotellerie (voir logs/compilation-hotellerie.log)"
    exit 1
fi

echo "   → Agence..."
cd "$PROJECT_ROOT/Agence"
mvn clean package -DskipTests -q > "$PROJECT_ROOT/logs/compilation-agence.log" 2>&1
if [ $? -eq 0 ]; then
    echo "      ✓ Agence compilé"
else
    echo "      ✗ Erreur compilation Agence (voir logs/compilation-agence.log)"
    exit 1
fi

echo "   → Client..."
cd "$PROJECT_ROOT/Client"
mvn clean package -DskipTests -q > "$PROJECT_ROOT/logs/compilation-client.log" 2>&1
if [ $? -eq 0 ]; then
    echo "      ✓ Client compilé"
else
    echo "      ✗ Erreur compilation Client (voir logs/compilation-client.log)"
    exit 1
fi

cd "$PROJECT_ROOT"
echo ""

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# ÉTAPE 4 : DÉMARRAGE DES HÔTELS
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

echo "═══════════════════════════════════════════════════════════════"
echo "   ÉTAPE 4/6 : Démarrage des 3 Hôtels"
echo "═══════════════════════════════════════════════════════════════"
echo ""

# Hôtel Paris
echo "🏨 Démarrage Hôtel Paris (Port 8082)..."
cd "$PROJECT_ROOT/Hotellerie"
mvn spring-boot:run -Dspring-boot.run.profiles=paris > "$PROJECT_ROOT/logs/hotel-paris.log" 2>&1 &
PARIS_PID=$!
echo "   PID: $PARIS_PID"
cd "$PROJECT_ROOT"

if ! check_service 8082 "Hôtel Paris" "$PROJECT_ROOT/logs/hotel-paris.log"; then
    echo ""
    echo "❌ Échec du démarrage. Arrêt du script."
    exit 1
fi
echo ""

# Hôtel Lyon
echo "🏨 Démarrage Hôtel Lyon (Port 8083)..."
cd "$PROJECT_ROOT/Hotellerie"
mvn spring-boot:run -Dspring-boot.run.profiles=lyon > "$PROJECT_ROOT/logs/hotel-lyon.log" 2>&1 &
LYON_PID=$!
echo "   PID: $LYON_PID"
cd "$PROJECT_ROOT"

if ! check_service 8083 "Hôtel Lyon" "$PROJECT_ROOT/logs/hotel-lyon.log"; then
    echo ""
    echo "❌ Échec du démarrage. Arrêt du script."
    exit 1
fi
echo ""

# Hôtel Montpellier
echo "🏨 Démarrage Hôtel Montpellier (Port 8084)..."
cd "$PROJECT_ROOT/Hotellerie"
mvn spring-boot:run -Dspring-boot.run.profiles=montpellier > "$PROJECT_ROOT/logs/hotel-montpellier.log" 2>&1 &
MONTPELLIER_PID=$!
echo "   PID: $MONTPELLIER_PID"
cd "$PROJECT_ROOT"

if ! check_service 8084 "Hôtel Montpellier" "$PROJECT_ROOT/logs/hotel-montpellier.log"; then
    echo ""
    echo "❌ Échec du démarrage. Arrêt du script."
    exit 1
fi
echo ""

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# ÉTAPE 5 : DÉMARRAGE DES AGENCES
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

echo "═══════════════════════════════════════════════════════════════"
echo "   ÉTAPE 5/6 : Démarrage des 2 Agences"
echo "═══════════════════════════════════════════════════════════════"
echo ""

# Agence 1
echo "🏢 Démarrage Agence 1 - Paris Voyages (Port 8081)..."
echo "   Configuration: Paris + Lyon | Coefficient: 1.15"
cd "$PROJECT_ROOT/Agence"
mvn spring-boot:run -Dspring-boot.run.profiles=agence1 > "$PROJECT_ROOT/logs/agence1.log" 2>&1 &
AGENCE1_PID=$!
echo "   PID: $AGENCE1_PID"
cd "$PROJECT_ROOT"

if ! check_service 8081 "Agence 1" "$PROJECT_ROOT/logs/agence1.log"; then
    echo ""
    echo "❌ Échec du démarrage. Arrêt du script."
    exit 1
fi
echo ""

# Agence 2
echo "🏢 Démarrage Agence 2 - Sud Réservations (Port 8085)..."
echo "   Configuration: Lyon + Montpellier | Coefficient: 1.20"
cd "$PROJECT_ROOT/Agence"
mvn spring-boot:run -Dspring-boot.run.profiles=agence2 > "$PROJECT_ROOT/logs/agence2.log" 2>&1 &
AGENCE2_PID=$!
echo "   PID: $AGENCE2_PID"
cd "$PROJECT_ROOT"

if ! check_service 8085 "Agence 2" "$PROJECT_ROOT/logs/agence2.log"; then
    echo ""
    echo "❌ Échec du démarrage. Arrêt du script."
    exit 1
fi
echo ""

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# RÉSUMÉ
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

echo "✅ Tous les services backend sont démarrés !"
echo ""
echo "   🏨 Hôtels : Paris ($PARIS_PID), Lyon ($LYON_PID), Montpellier ($MONTPELLIER_PID)"
echo "   🏢 Agences : Agence 1 ($AGENCE1_PID), Agence 2 ($AGENCE2_PID)"
echo ""
echo "📝 Logs disponibles dans: logs/"
echo "   - hotel-paris.log"
echo "   - hotel-lyon.log"
echo "   - hotel-montpellier.log"
echo "   - agence1.log"
echo "   - agence2.log"
echo ""

# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# ÉTAPE 6 : LANCEMENT DU CLIENT GUI
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

echo "═══════════════════════════════════════════════════════════════"
echo "   ÉTAPE 6/6 : Lancement Interface Graphique"
echo "═══════════════════════════════════════════════════════════════"
echo ""

cd "$PROJECT_ROOT/Client"

echo "🚀 Ouverture de l'interface graphique..."
echo ""
echo "💡 Conseil: Regardez les logs en temps réel avec:"
echo "   tail -f $PROJECT_ROOT/logs/hotel-paris.log"
echo "   tail -f $PROJECT_ROOT/logs/agence1.log"
echo ""

# Lancer le client GUI
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
echo "🛑 Pour arrêter tous les services:"
echo "   $PROJECT_ROOT/arreter-services.sh"
echo ""

