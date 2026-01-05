#!/bin/bash

echo "🧪 TEST RAPIDE - Vérification Bug de Concurrence"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Ce test appelle l'agence 1 plusieurs fois pour vérifier"
echo "que les résultats sont STABLES (pas aléatoires)"
echo ""

for i in {1..5}; do
    echo "Test #$i:"
    curl -s -X POST http://localhost:8081/graphql \
      -H "Content-Type: application/json" \
      -d '{"query":"{ rechercherChambres(criteres: { adresse: \"\" dateArrive: \"2024-01-01\" dateDepart: \"2024-01-05\" }) { nom hotelNom hotelAdresse agenceNom } }"}' \
      | jq -r '.data.rechercherChambres[] | "\(.nom) | \(.hotelNom) | \(.hotelAdresse) | \(.agenceNom)"' \
      | head -3
    echo ""
    sleep 1
done

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Si les résultats sont IDENTIQUES à chaque fois → OK"
echo "❌ Si les résultats changent → Bug de concurrence persiste"

