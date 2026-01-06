#!/bin/bash

# Script pour redémarrer les services GraphQL (hôtels + agences)
# Sans réinitialisation de la base de données H2

echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║          🔄 REDÉMARRAGE DES SERVICES GRAPHQL                    ║"
echo "╚══════════════════════════════════════════════════════════════════╝"

# Couleurs
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 1️⃣ Arrêt des services existants
echo ""
echo -e "${YELLOW}1️⃣  Arrêt des services Java en cours...${NC}"
pkill -f "Hotellerie-0.0.1-SNAPSHOT.jar" 2>/dev/null
pkill -f "Agence-0.0.1-SNAPSHOT.jar" 2>/dev/null
pkill -f "Client-0.0.1-SNAPSHOT.jar" 2>/dev/null
sleep 2
echo -e "${GREEN}   ✅ Services arrêtés${NC}"

# 2️⃣ Démarrage des hôtels
echo ""
echo -e "${YELLOW}2️⃣  Démarrage des hôtels GraphQL...${NC}"

# Hôtel Paris (port 8082)
nohup java -jar Hotellerie/target/Hotellerie-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=paris \
  --server.port=8082 > logs/hotel-paris.log 2>&1 &
echo -e "${GREEN}   ✅ Hôtel Paris démarré (port 8082)${NC}"
sleep 3

# Hôtel Lyon (port 8083)
nohup java -jar Hotellerie/target/Hotellerie-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=lyon \
  --server.port=8083 > logs/hotel-lyon.log 2>&1 &
echo -e "${GREEN}   ✅ Hôtel Lyon démarré (port 8083)${NC}"
sleep 3

# Hôtel Montpellier (port 8084)
nohup java -jar Hotellerie/target/Hotellerie-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=montpellier \
  --server.port=8084 > logs/hotel-montpellier.log 2>&1 &
echo -e "${GREEN}   ✅ Hôtel Montpellier démarré (port 8084)${NC}"
sleep 3

# 3️⃣ Démarrage des agences
echo ""
echo -e "${YELLOW}3️⃣  Démarrage des agences GraphQL...${NC}"

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

# 4️⃣ Vérification
echo ""
echo -e "${YELLOW}4️⃣  Vérification des services (timeout: 30s par service)...${NC}"

SERVICES_OK=0

# Fonction pour attendre qu'un service soit prêt
wait_for_service() {
  local PORT=$1
  local QUERY=$2
  local MAX_ATTEMPTS=30
  local ATTEMPT=0

  while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    if curl -s http://localhost:$PORT/graphql -H "Content-Type: application/json" \
      -d "{\"query\":\"$QUERY\"}" > /dev/null 2>&1; then
      return 0
    fi
    ATTEMPT=$((ATTEMPT + 1))
    sleep 1
  done
  return 1
}

# Vérifier les hôtels
for PORT in 8082 8083 8084; do
  if wait_for_service $PORT "{ hotelInfo { nom } }"; then
    echo -e "${GREEN}   ✅ Hôtel sur port $PORT opérationnel${NC}"
    ((SERVICES_OK++))
  else
    echo -e "${RED}   ❌ Hôtel sur port $PORT non accessible après 30s${NC}"
  fi
done

# Vérifier les agences
for PORT in 8081 8085; do
  if wait_for_service $PORT "{ ping { message } }"; then
    echo -e "${GREEN}   ✅ Agence sur port $PORT opérationnelle${NC}"
    ((SERVICES_OK++))
  else
    echo -e "${RED}   ❌ Agence sur port $PORT non accessible après 30s${NC}"
  fi
done

# Résultat final
echo ""
echo "╔══════════════════════════════════════════════════════════════════╗"
if [ $SERVICES_OK -eq 5 ]; then
  echo -e "║  ${GREEN}✅ TOUS LES SERVICES GRAPHQL SONT OPÉRATIONNELS (5/5)${NC}         ║"
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

