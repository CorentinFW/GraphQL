#!/bin/bash

# Script pour arrêter tous les services GraphQL (hôtels, agences, client)

echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║            🛑 ARRÊT DE TOUS LES SERVICES GRAPHQL                ║"
echo "╚══════════════════════════════════════════════════════════════════╝"

# Couleurs
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo ""
echo -e "${YELLOW}🔍 Recherche des processus Java en cours...${NC}"

# Compter les processus avant arrêt
HOTELLERIE_COUNT=$(pgrep -f "Hotellerie-0.0.1-SNAPSHOT.jar" | wc -l)
AGENCE_COUNT=$(pgrep -f "Agence-0.0.1-SNAPSHOT.jar" | wc -l)
CLIENT_COUNT=$(pgrep -f "Client-0.0.1-SNAPSHOT.jar" | wc -l)

TOTAL_BEFORE=$((HOTELLERIE_COUNT + AGENCE_COUNT + CLIENT_COUNT))

if [ $TOTAL_BEFORE -eq 0 ]; then
  echo ""
  echo -e "${BLUE}ℹ️  Aucun service GraphQL en cours d'exécution${NC}"
  echo ""
  exit 0
fi

echo ""
echo -e "${YELLOW}Services trouvés:${NC}"
[ $HOTELLERIE_COUNT -gt 0 ] && echo -e "${BLUE}   🏨 Hôtels: $HOTELLERIE_COUNT processus${NC}"
[ $AGENCE_COUNT -gt 0 ] && echo -e "${BLUE}   🏢 Agences: $AGENCE_COUNT processus${NC}"
[ $CLIENT_COUNT -gt 0 ] && echo -e "${BLUE}   🖥️  Client: $CLIENT_COUNT processus${NC}"
echo -e "${YELLOW}   📊 Total: $TOTAL_BEFORE processus${NC}"

echo ""
echo -e "${YELLOW}🛑 Arrêt des services en cours...${NC}"

# Arrêter le client
if [ $CLIENT_COUNT -gt 0 ]; then
  pkill -f "Client-0.0.1-SNAPSHOT.jar" 2>/dev/null
  echo -e "${GREEN}   ✅ Client arrêté ($CLIENT_COUNT processus)${NC}"
fi

# Arrêter les agences
if [ $AGENCE_COUNT -gt 0 ]; then
  pkill -f "Agence-0.0.1-SNAPSHOT.jar" 2>/dev/null
  echo -e "${GREEN}   ✅ Agences arrêtées ($AGENCE_COUNT processus)${NC}"
fi

# Arrêter les hôtels
if [ $HOTELLERIE_COUNT -gt 0 ]; then
  pkill -f "Hotellerie-0.0.1-SNAPSHOT.jar" 2>/dev/null
  echo -e "${GREEN}   ✅ Hôtels arrêtés ($HOTELLERIE_COUNT processus)${NC}"
fi

# Attendre que les processus se terminent
echo ""
echo -e "${YELLOW}⏳ Attente de l'arrêt complet des processus...${NC}"
sleep 3

# Vérification finale
HOTELLERIE_AFTER=$(pgrep -f "Hotellerie-0.0.1-SNAPSHOT.jar" | wc -l)
AGENCE_AFTER=$(pgrep -f "Agence-0.0.1-SNAPSHOT.jar" | wc -l)
CLIENT_AFTER=$(pgrep -f "Client-0.0.1-SNAPSHOT.jar" | wc -l)

TOTAL_AFTER=$((HOTELLERIE_AFTER + AGENCE_AFTER + CLIENT_AFTER))

echo ""
echo "╔══════════════════════════════════════════════════════════════════╗"

if [ $TOTAL_AFTER -eq 0 ]; then
  echo -e "║  ${GREEN}✅ TOUS LES SERVICES ONT ÉTÉ ARRÊTÉS AVEC SUCCÈS${NC}             ║"
  echo "╚══════════════════════════════════════════════════════════════════╝"
  echo ""
  echo -e "${GREEN}✨ $TOTAL_BEFORE processus arrêté(s)${NC}"
else
  echo -e "║  ${YELLOW}⚠️  CERTAINS PROCESSUS N'ONT PAS ÉTÉ ARRÊTÉS${NC}                  ║"
  echo "╚══════════════════════════════════════════════════════════════════╝"
  echo ""
  echo -e "${YELLOW}Processus restants:${NC}"
  [ $HOTELLERIE_AFTER -gt 0 ] && echo -e "${RED}   ❌ Hôtels: $HOTELLERIE_AFTER processus${NC}"
  [ $AGENCE_AFTER -gt 0 ] && echo -e "${RED}   ❌ Agences: $AGENCE_AFTER processus${NC}"
  [ $CLIENT_AFTER -gt 0 ] && echo -e "${RED}   ❌ Client: $CLIENT_AFTER processus${NC}"
  echo ""
  echo -e "${YELLOW}💡 Utilisez 'pkill -9 -f java' pour forcer l'arrêt si nécessaire${NC}"
fi

echo ""
echo -e "${BLUE}💡 Pour relancer les services:${NC}"
echo "   - ./graphQL-service.sh    (redémarrer sans réinitialiser la BDD)"
echo "   - ./graphQL-restart.sh    (redémarrer avec réinitialisation BDD H2)"
echo "   - ./graphQL-client.sh     (lancer uniquement le client)"
echo ""

