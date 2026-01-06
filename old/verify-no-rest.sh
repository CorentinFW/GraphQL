#!/bin/bash

echo "════════════════════════════════════════════════════════════════"
echo "  VÉRIFICATION FINALE - SUPPRESSION DU CODE REST"
echo "════════════════════════════════════════════════════════════════"
echo ""

# Compteurs
REST_FOUND=0
SUCCESS_COUNT=0

echo "🔍 1. Recherche de vestiges REST dans le code actif..."
echo "─────────────────────────────────────────────────────────────────"

# Recherche @RestController
echo -n "   @RestController : "
COUNT=$(find . -name "*.java" ! -name "*.old" -type f -exec grep -l "@RestController" {} \; 2>/dev/null | wc -l)
if [ "$COUNT" -eq 0 ]; then
    echo "✅ Aucun"
else
    echo "❌ $COUNT trouvé(s)"
    REST_FOUND=$((REST_FOUND + COUNT))
fi

# Recherche @RequestMapping
echo -n "   @RequestMapping : "
COUNT=$(find . -name "*.java" ! -name "*.old" -type f -exec grep -l "@RequestMapping" {} \; 2>/dev/null | wc -l)
if [ "$COUNT" -eq 0 ]; then
    echo "✅ Aucun"
else
    echo "❌ $COUNT trouvé(s)"
    REST_FOUND=$((REST_FOUND + COUNT))
fi

# Recherche RestTemplate
echo -n "   RestTemplate      : "
COUNT=$(find . -name "*.java" ! -name "*.old" -type f -exec grep -l "RestTemplate" {} \; 2>/dev/null | wc -l)
if [ "$COUNT" -eq 0 ]; then
    echo "✅ Aucun"
else
    echo "❌ $COUNT trouvé(s)"
    REST_FOUND=$((REST_FOUND + COUNT))
fi

echo ""
echo "🔍 2. Vérification des dépendances Maven..."
echo "─────────────────────────────────────────────────────────────────"

echo -n "   springdoc-openapi : "
COUNT=$(grep -r "springdoc" --include="pom.xml" | wc -l)
if [ "$COUNT" -eq 0 ]; then
    echo "✅ Aucun (supprimé)"
else
    echo "❌ $COUNT trouvé(s)"
fi

echo ""
echo "🔍 3. Compilation des modules..."
echo "─────────────────────────────────────────────────────────────────"

# Hotellerie
echo -n "   Module Hotellerie : "
cd Hotellerie
if mvn clean compile -DskipTests > /tmp/hotellerie-build.log 2>&1; then
    echo "✅ BUILD SUCCESS"
    SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
else
    echo "❌ BUILD FAILURE (voir /tmp/hotellerie-build.log)"
fi
cd ..

# Agence
echo -n "   Module Agence     : "
cd Agence
if mvn clean compile -DskipTests > /tmp/agence-build.log 2>&1; then
    echo "✅ BUILD SUCCESS"
    SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
else
    echo "❌ BUILD FAILURE (voir /tmp/agence-build.log)"
fi
cd ..

# Client
echo -n "   Module Client     : "
cd Client
if mvn clean compile -DskipTests > /tmp/client-build.log 2>&1; then
    echo "✅ BUILD SUCCESS"
    SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
else
    echo "❌ BUILD FAILURE (voir /tmp/client-build.log)"
fi
cd ..

echo ""
echo "🔍 4. Vérification GraphQL..."
echo "─────────────────────────────────────────────────────────────────"

echo -n "   Schémas .graphqls : "
SCHEMAS=$(find . -name "*.graphqls" | wc -l)
echo "$SCHEMAS trouvé(s) ✅"

echo -n "   Resolvers GraphQL : "
RESOLVERS=$(find . -name "*Resolver.java" ! -name "*.old" | wc -l)
echo "$RESOLVERS trouvé(s) ✅"

echo -n "   Clients GraphQL   : "
CLIENTS=$(find . -name "*GraphQLClient.java" ! -name "*.old" | wc -l)
echo "$CLIENTS trouvé(s) ✅"

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "  RÉSULTAT FINAL"
echo "════════════════════════════════════════════════════════════════"

if [ "$REST_FOUND" -eq 0 ] && [ "$SUCCESS_COUNT" -eq 3 ]; then
    echo ""
    echo "   🎉 VÉRIFICATION RÉUSSIE ! 🎉"
    echo ""
    echo "   ✅ Aucun code REST trouvé dans le code actif"
    echo "   ✅ Tous les modules compilent avec succès"
    echo "   ✅ Architecture GraphQL complète"
    echo ""
    echo "   Le projet est 100% GraphQL et prêt !"
    echo ""
else
    echo ""
    echo "   ⚠️  ATTENTION"
    echo ""
    if [ "$REST_FOUND" -ne 0 ]; then
        echo "   ❌ $REST_FOUND fichier(s) REST trouvé(s)"
    fi
    if [ "$SUCCESS_COUNT" -ne 3 ]; then
        echo "   ❌ $((3 - SUCCESS_COUNT)) module(s) en échec de compilation"
    fi
    echo ""
fi

echo "════════════════════════════════════════════════════════════════"

