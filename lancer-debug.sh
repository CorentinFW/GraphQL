#!/bin/bash
set -e
echo "🔧 COMPILATION ET LANCEMENT - MODE DEBUG"
echo "════════════════════════════════════════════════════════════════"
# Nettoyer
rm -rf Hotellerie/target Agence/target Client/target
# Compiler
echo "📦 Compilation Hotellerie..."
cd Hotellerie && mvn clean package -DskipTests -q
cd ..
echo "📦 Compilation Agence..."
cd Agence && mvn clean package -DskipTests -q
cd ..
echo "📦 Compilation Client..."
cd Client && mvn clean package -DskipTests -q
cd ..
echo "✅ Compilation terminée"
echo ""
# Lancer les services
echo "🚀 Lancement des services..."
cd Hotellerie
nohup java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=paris > ../logs/hotel-paris.log 2>&1 &
sleep 3
nohup java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=lyon > ../logs/hotel-lyon.log 2>&1 &
sleep 3
nohup java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=montpellier > ../logs/hotel-montpellier.log 2>&1 &
sleep 3
cd ../Agence
nohup java -jar target/Agence-0.0.1-SNAPSHOT.jar --spring.profiles.active=agence1 > ../logs/agence1.log 2>&1 &
sleep 3
nohup java -jar target/Agence-0.0.1-SNAPSHOT.jar --spring.profiles.active=agence2 > ../logs/agence2.log 2>&1 &
sleep 3
cd ../Client
echo ""
echo "════════════════════════════════════════════════════════════════"
echo "🖥️  LANCEMENT DU CLIENT - REGARDEZ LA CONSOLE !"
echo "════════════════════════════════════════════════════════════════"
java -jar target/Client-0.0.1-SNAPSHOT.jar --gui
