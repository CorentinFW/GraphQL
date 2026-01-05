#!/bin/bash

echo "🔥 RELANCEMENT FORCÉ AVEC NOUVEAU CODE"
echo "═══════════════════════════════════════════════════════════════"
echo ""

# Tuer TOUS les processus Java
echo "1️⃣  Arrêt de tous les services Java..."
pkill -9 -f "java" 2>/dev/null
sleep 2

# Nettoyer les target
echo "2️⃣  Nettoyage des dossiers target..."
rm -rf /home/corentinfay/Bureau/GraphQL/Hotellerie/target
rm -rf /home/corentinfay/Bureau/GraphQL/Agence/target
rm -rf /home/corentinfay/Bureau/GraphQL/Client/target

# Recompiler TOUT
echo "3️⃣  Recompilation complète..."
cd /home/corentinfay/Bureau/GraphQL

echo "   📦 Hotellerie..."
cd Hotellerie && mvn clean package -DskipTests -q
echo "   ✅ Hotellerie compilé"

echo "   📦 Agence..."
cd ../Agence && mvn clean package -DskipTests -q
echo "   ✅ Agence compilé"

echo "   📦 Client..."
cd ../Client && mvn clean package -DskipTests -q
echo "   ✅ Client compilé"

cd ..

echo ""
echo "4️⃣  Vérification des JAR..."
ls -lh Hotellerie/target/*.jar 2>/dev/null | tail -1
ls -lh Agence/target/*.jar 2>/dev/null | tail -1
ls -lh Client/target/*.jar 2>/dev/null | tail -1

echo ""
echo "5️⃣  Lancement des services..."

# Lancer les hôtels DIRECTEMENT avec les JAR
cd Hotellerie
nohup java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=paris > ../logs/hotel-paris.log 2>&1 &
echo "   ✅ Hôtel Paris démarré (JAR)"
sleep 5

nohup java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=lyon > ../logs/hotel-lyon.log 2>&1 &
echo "   ✅ Hôtel Lyon démarré (JAR)"
sleep 5

nohup java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=montpellier > ../logs/hotel-montpellier.log 2>&1 &
echo "   ✅ Hôtel Montpellier démarré (JAR)"
sleep 5

# Lancer les agences DIRECTEMENT avec les JAR
cd ../Agence
nohup java -jar target/Agence-0.0.1-SNAPSHOT.jar --spring.profiles.active=agence1 > ../logs/agence1.log 2>&1 &
echo "   ✅ Agence 1 démarrée (JAR)"
sleep 5

nohup java -jar target/Agence-0.0.1-SNAPSHOT.jar --spring.profiles.active=agence2 > ../logs/agence2.log 2>&1 &
echo "   ✅ Agence 2 démarrée (JAR)"
sleep 5

# Lancer le client DIRECTEMENT avec le JAR
cd ../Client
echo "   🖥️  Lancement du client GUI (JAR COMPILÉ)..."
echo ""
echo "════════════════════════════════════════════════════════════════"
echo "⚠️  REGARDEZ LA CONSOLE DU CLIENT !"
echo "Si vous voyez le message:"
echo "🔥🔥🔥 NOUVEAU CODE CHARGÉ 🔥🔥🔥"
echo "C'est que le nouveau code est bien utilisé !"
echo "════════════════════════════════════════════════════════════════"
echo ""

java -jar target/Client-0.0.1-SNAPSHOT.jar --gui

