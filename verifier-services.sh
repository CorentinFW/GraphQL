#!/bin/bash

echo "🔍 Vérification que le nouveau code est chargé..."
echo ""

# Vérifier que les JAR ont été recompilés récemment
echo "📦 Date de compilation des JAR:"
echo "  Client: $(stat -c %y Client/target/Client-0.0.1-SNAPSHOT.jar 2>/dev/null | cut -d' ' -f1-2 || echo 'Non trouvé')"
echo "  Agence: $(stat -c %y Agence/target/Agence-0.0.1-SNAPSHOT.jar 2>/dev/null | cut -d' ' -f1-2 || echo 'Non trouvé')"
echo "  Hotellerie: $(stat -c %y Hotellerie/target/Hotellerie-0.0.1-SNAPSHOT.jar 2>/dev/null | cut -d' ' -f1-2 || echo 'Non trouvé')"
echo ""

# Vérifier que les services tournent
echo "🔄 Services en cours d'exécution:"
ps aux | grep -E "(spring-boot:run|java.*-SNAPSHOT.jar)" | grep -v grep | wc -l | xargs -I {} echo "  {} service(s) détecté(s)"
echo ""

# Vérifier les ports
echo "🌐 Ports ouverts:"
for port in 8081 8082 8083 8084 8085; do
    if lsof -i :$port >/dev/null 2>&1; then
        echo "  ✅ Port $port: OUVERT"
    else
        echo "  ❌ Port $port: FERMÉ"
    fi
done
echo ""

echo "✅ Si tous les ports sont ouverts, le système est prêt"
echo "❌ Si des ports sont fermés, relancez: ./rest-all-restart.sh"

