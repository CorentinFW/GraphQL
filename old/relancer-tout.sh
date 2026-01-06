#!/bin/bash

echo "🚀 RELANCEMENT COMPLET DU SYSTÈME"
echo "════════════════════════════════════════════════════════════════"

# 1. Arrêter tout
echo "1️⃣  Arrêt des services..."
pkill -9 -f "java" 2>/dev/null
sleep 2

# 2. Compiler si nécessaire
echo "2️⃣  Vérification des JAR..."
if [ ! -f "Hotellerie/target/Hotellerie-0.0.1-SNAPSHOT.jar" ]; then
    echo "   Compilation Hotellerie..."
    cd Hotellerie && mvn clean package -DskipTests -q && cd ..
fi
if [ ! -f "Agence/target/Agence-0.0.1-SNAPSHOT.jar" ]; then
    echo "   Compilation Agence..."
    cd Agence && mvn clean package -DskipTests -q && cd ..
fi
if [ ! -f "Client/target/Client-0.0.1-SNAPSHOT.jar" ]; then
    echo "   Compilation Client..."
    cd Client && mvn clean package -DskipTests -q && cd ..
fi

echo "3️⃣  Démarrage des services..."

# Lancer les hôtels
cd Hotellerie
echo "   🏨 Hôtel Paris..."
java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=paris > ../logs/hotel-paris.log 2>&1 &
sleep 4

echo "   🏨 Hôtel Lyon..."
java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=lyon > ../logs/hotel-lyon.log 2>&1 &
sleep 4

echo "   🏨 Hôtel Montpellier..."
java -jar target/Hotellerie-0.0.1-SNAPSHOT.jar --spring.profiles.active=montpellier > ../logs/hotel-montpellier.log 2>&1 &
sleep 4

# Lancer les agences
cd ../Agence
echo "   🏢 Agence Paris Voyages..."
java -jar target/Agence-0.0.1-SNAPSHOT.jar --spring.profiles.active=agence1 > ../logs/agence1.log 2>&1 &
sleep 4

echo "   🏢 Agence Sud Réservations..."
java -jar target/Agence-0.0.1-SNAPSHOT.jar --spring.profiles.active=agence2 > ../logs/agence2.log 2>&1 &
sleep 4

# Lancer le client
cd ../Client
echo "   🖥️  Client GUI..."
echo ""
echo "════════════════════════════════════════════════════════════════"
echo "✅ Tous les services sont démarrés !"
echo "La GUI va s'ouvrir dans quelques secondes..."
echo "════════════════════════════════════════════════════════════════"
sleep 2

java -jar target/Client-0.0.1-SNAPSHOT.jar --gui

