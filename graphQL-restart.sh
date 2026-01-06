#!/bin/bash

# Script pour redémarrer complètement les services GraphQL avec réinitialisation de la base H2

echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║     🔄 REDÉMARRAGE COMPLET (Services + Base de données H2)     ║"
echo "╚══════════════════════════════════════════════════════════════════╝"

# Couleurs
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 1️⃣ Arrêt des services existants
echo ""
echo -e "${YELLOW}1️⃣  Arrêt des services Java en cours...${NC}"
pkill -f "Hotellerie-0.0.1-SNAPSHOT.jar" 2>/dev/null
pkill -f "Agence-0.0.1-SNAPSHOT.jar" 2>/dev/null
pkill -f "Client-0.0.1-SNAPSHOT.jar" 2>/dev/null
sleep 3
echo -e "${GREEN}   ✅ Services arrêtés${NC}"

# 2️⃣ Suppression des bases de données H2
echo ""
echo -e "${YELLOW}2️⃣  Suppression des bases de données H2...${NC}"

if [ -d "Hotellerie/data" ]; then
  rm -f Hotellerie/data/*.db 2>/dev/null
  echo -e "${GREEN}   ✅ Bases de données H2 supprimées${NC}"
else
  echo -e "${BLUE}   ℹ️  Dossier data/ inexistant (sera créé au démarrage)${NC}"
fi

# 3️⃣ Démarrage des hôtels
echo ""
echo -e "${YELLOW}3️⃣  Démarrage des hôtels GraphQL avec bases H2 vierges...${NC}"

# Créer le dossier logs s'il n'existe pas
mkdir -p logs

# Hôtel Paris (port 8082)
nohup java -jar Hotellerie/target/Hotellerie-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=paris \
  --server.port=8082 > logs/hotel-paris.log 2>&1 &
echo -e "${GREEN}   ✅ Hôtel Paris démarré (port 8082) - BDD réinitialisée${NC}"
sleep 4

# Hôtel Lyon (port 8083)
nohup java -jar Hotellerie/target/Hotellerie-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=lyon \
  --server.port=8083 > logs/hotel-lyon.log 2>&1 &
echo -e "${GREEN}   ✅ Hôtel Lyon démarré (port 8083) - BDD réinitialisée${NC}"
sleep 4

# Hôtel Montpellier (port 8084)
nohup java -jar Hotellerie/target/Hotellerie-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=montpellier \
  --server.port=8084 > logs/hotel-montpellier.log 2>&1 &
echo -e "${GREEN}   ✅ Hôtel Montpellier démarré (port 8084) - BDD réinitialisée${NC}"
sleep 4

# 4️⃣ Démarrage des agences
echo ""
echo -e "${YELLOW}4️⃣  Démarrage des agences GraphQL...${NC}"

# Agence Paris Voyage (port 8081)
nohup java -jar Agence/target/Agence-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=agence1 \
  --server.port=8081 > logs/agence1.log 2>&1 &
echo -e "${GREEN}   ✅ Agence Paris Voyage démarrée (port 8081)${NC}"
sleep 3

# Agence Sud Réservation (port 8085)
nohup java -jar Agence/target/Agence-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=agence2 \
  --server.port=8085 > logs/agence2.log 2>&1 &
echo -e "${GREEN}   ✅ Agence Sud Réservation démarrée (port 8085)${NC}"
sleep 3

# 5️⃣ Vérification
echo ""
echo -e "${YELLOW}5️⃣  Vérification des services...${NC}"
sleep 2

SERVICES_OK=0

# Vérifier les hôtels
for PORT in 8082 8083 8084; do
  if curl -s http://localhost:$PORT/graphql -H "Content-Type: application/json" \
    -d '{"query":"{ hotelInfo { nom } }"}' > /dev/null 2>&1; then
    echo -e "${GREEN}   ✅ Hôtel sur port $PORT opérationnel${NC}"
    ((SERVICES_OK++))
  else
    echo -e "${RED}   ❌ Hôtel sur port $PORT non accessible${NC}"
  fi
done

# Vérifier les agences
for PORT in 8081 8085; do
  if curl -s http://localhost:$PORT/graphql -H "Content-Type: application/json" \
    -d '{"query":"{ ping { message } }"}' > /dev/null 2>&1; then
    echo -e "${GREEN}   ✅ Agence sur port $PORT opérationnelle${NC}"
    ((SERVICES_OK++))
  else
    echo -e "${RED}   ❌ Agence sur port $PORT non accessible${NC}"
  fi
done

# 6️⃣ Vérification des données initiales
echo ""
echo -e "${YELLOW}6️⃣  Vérification des données initiales...${NC}"

# Vérifier les chambres de chaque hôtel
for PORT in 8082 8083 8084; do
  CHAMBRES_COUNT=$(curl -s http://localhost:$PORT/graphql -H "Content-Type: application/json" \
    -d '{"query":"{ chambresDisponibles(dateDebut: \"2025-12-01\", dateFin: \"2025-12-05\") { id nom } }"}' \
    | grep -o '"id"' | wc -l)

  case $PORT in
    8082) HOTEL="Paris" ;;
    8083) HOTEL="Lyon" ;;
    8084) HOTEL="Montpellier" ;;
  esac

  if [ "$CHAMBRES_COUNT" -gt 0 ]; then
    echo -e "${GREEN}   ✅ $HOTEL: $CHAMBRES_COUNT chambres initialisées${NC}"
  else
    echo -e "${RED}   ❌ $HOTEL: Aucune chambre trouvée${NC}"
  fi
done

# Résultat final
echo ""
echo "╔══════════════════════════════════════════════════════════════════╗"
if [ $SERVICES_OK -eq 5 ]; then
  echo -e "║  ${GREEN}✅ TOUS LES SERVICES SONT OPÉRATIONNELS (5/5)${NC}                 ║"
  echo -e "║  ${GREEN}✅ BASES DE DONNÉES H2 RÉINITIALISÉES${NC}                        ║"
else
  echo -e "║  ${YELLOW}⚠️  Services opérationnels: $SERVICES_OK/5${NC}                           ║"
fi
echo "╚══════════════════════════════════════════════════════════════════╝"

echo ""
echo "📋 Logs disponibles dans le dossier logs/"
echo "   - logs/hotel-paris.log"
echo "   - logs/hotel-lyon.log"
echo "   - logs/hotel-montpellier.log"
echo "   - logs/agence1.log"
echo "   - logs/agence2.log"
echo ""
echo "💡 Utilisez './graphQL-client.sh' pour lancer l'interface client"
echo ""

