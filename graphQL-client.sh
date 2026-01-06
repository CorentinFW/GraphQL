#!/bin/bash

# Script pour lancer l'interface client Swing GraphQL

echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║            🖥️  LANCEMENT DU CLIENT GRAPHQL (SWING)              ║"
echo "╚══════════════════════════════════════════════════════════════════╝"

# Couleurs
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 1️⃣ Vérification que les services sont lancés
echo ""
echo -e "${YELLOW}1️⃣  Vérification des services GraphQL...${NC}"

SERVICES_OK=0

# Vérifier les agences (le client en a besoin)
for PORT in 8081 8085; do
  if curl -s http://localhost:$PORT/graphql -H "Content-Type: application/json" \
    -d '{"query":"{ ping { message } }"}' > /dev/null 2>&1; then
    echo -e "${GREEN}   ✅ Agence sur port $PORT opérationnelle${NC}"
    ((SERVICES_OK++))
  else
    echo -e "${RED}   ❌ Agence sur port $PORT non accessible${NC}"
  fi
done

if [ $SERVICES_OK -lt 2 ]; then
  echo ""
  echo -e "${RED}⚠️  ATTENTION: Les agences ne sont pas toutes lancées!${NC}"
  echo -e "${YELLOW}   Lancez d'abord: ./graphQL-service.sh ou ./graphQL-restart.sh${NC}"
  echo ""
  read -p "Voulez-vous continuer quand même ? (o/N) " -n 1 -r
  echo
  if [[ ! $REPLY =~ ^[Oo]$ ]]; then
    echo -e "${BLUE}Lancement annulé.${NC}"
    exit 1
  fi
fi

# 2️⃣ Vérification du JAR client
echo ""
echo -e "${YELLOW}2️⃣  Vérification du JAR client...${NC}"

if [ ! -f "Client/target/Client-0.0.1-SNAPSHOT.jar" ]; then
  echo -e "${RED}   ❌ JAR client introuvable: Client/target/Client-0.0.1-SNAPSHOT.jar${NC}"
  echo ""
  echo -e "${YELLOW}   Compilation du client en cours...${NC}"

  cd Client
  mvn clean package -DskipTests > ../logs/compilation-client.log 2>&1

  if [ $? -eq 0 ]; then
    echo -e "${GREEN}   ✅ Client compilé avec succès${NC}"
    cd ..
  else
    echo -e "${RED}   ❌ Erreur lors de la compilation${NC}"
    echo -e "${YELLOW}   Voir logs/compilation-client.log pour plus de détails${NC}"
    cd ..
    exit 1
  fi
else
  echo -e "${GREEN}   ✅ JAR client trouvé${NC}"
fi

# 3️⃣ Arrêt de l'ancien client s'il tourne
echo ""
echo -e "${YELLOW}3️⃣  Arrêt de l'ancien client (si actif)...${NC}"
pkill -f "Client-0.0.1-SNAPSHOT.jar" 2>/dev/null
sleep 1
echo -e "${GREEN}   ✅ Ancien client arrêté${NC}"

# 4️⃣ Lancement du client
echo ""
echo -e "${YELLOW}4️⃣  Lancement de l'interface client Swing...${NC}"
echo ""

mkdir -p logs

# Lancer le client en arrière-plan avec logs
nohup java -jar Client/target/Client-0.0.1-SNAPSHOT.jar > logs/client-gui.log 2>&1 &

CLIENT_PID=$!

sleep 3

# Vérifier si le client est bien lancé
if ps -p $CLIENT_PID > /dev/null 2>&1; then
  echo ""
  echo "╔══════════════════════════════════════════════════════════════════╗"
  echo -e "║  ${GREEN}✅ CLIENT GRAPHQL LANCÉ AVEC SUCCÈS${NC}                          ║"
  echo "╚══════════════════════════════════════════════════════════════════╝"
  echo ""
  echo -e "${BLUE}📱 L'interface Swing devrait s'ouvrir automatiquement${NC}"
  echo -e "${BLUE}📋 PID du client: $CLIENT_PID${NC}"
  echo -e "${BLUE}📄 Logs: logs/client-gui.log${NC}"
  echo ""
  echo -e "${YELLOW}💡 Pour arrêter le client:${NC}"
  echo "   - Fermez la fenêtre Swing"
  echo "   - Ou utilisez: ./arret-graphQL.sh"
  echo "   - Ou utilisez: pkill -f Client-0.0.1-SNAPSHOT.jar"
  echo ""
else
  echo ""
  echo -e "${RED}❌ Erreur: Le client n'a pas pu démarrer${NC}"
  echo -e "${YELLOW}📋 Consultez les logs: tail -50 logs/client-gui.log${NC}"
  echo ""
  exit 1
fi

