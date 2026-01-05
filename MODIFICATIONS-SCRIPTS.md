# Modifications des Scripts de Démarrage - Migration GraphQL

## Date de modification
5 janvier 2026

## Scripts modifiés

### 1. `rest-all-restart.sh`
**Modifications effectuées :**
- ✅ Titre modifié : "REDÉMARRAGE COMPLET GraphQL - RESET BASES DE DONNÉES"
- ✅ Fonction `check_service()` : Vérification via endpoint GraphQL (`/graphql`) au lieu de REST (`/api/*`)
- ✅ Utilisation de requête GraphQL introspection : `{"query":"{__typename}"}`

### 2. `rest-restart.sh`
**Modifications effectuées :**
- ✅ Titre modifié : "REDÉMARRAGE GraphQL - CONSERVATION DES DONNÉES"
- ✅ Fonction `check_service()` : Vérification via endpoint GraphQL (`/graphql`)
- ✅ Utilisation de requête GraphQL introspection : `{"query":"{__typename}"}`

### 3. `rest-client.sh`
**Modifications effectuées :**
- ✅ Fonction `check_backend_service()` : Vérification via endpoint GraphQL
- ✅ Suppression des vérifications REST (`/api/hotel/info`, `/api/agence/ping`)
- ✅ Utilisation de requête GraphQL introspection : `{"query":"{__typename}"}`

### 4. `arreter-services.sh`
**Modifications effectuées :**
- ✅ Commentaire modifié : "Arrêt de tous les services GraphQL"
- ✅ Message de sortie modifié : "Arrêt de tous les services GraphQL"

### 5. `.gitignore` (NOUVEAU)
**Fichier créé avec les règles suivantes :**
- ✅ Ignore tous les dossiers `target/` (build Maven)
- ✅ Ignore les fichiers de logs (`logs/`, `*.log`)
- ✅ Ignore les bases de données H2 (`**/*.db`, `**/*.mv.db`, `**/*.trace.db`)
- ✅ Ignore les fichiers IDE (IntelliJ IDEA, Eclipse, VSCode)
- ✅ Ignore les fichiers système (`.DS_Store`, `Thumbs.db`)
- ✅ Ignore les fichiers Maven wrapper

## Détails techniques

### Vérification de santé GraphQL
Tous les scripts utilisent maintenant une requête GraphQL introspection pour vérifier que les services sont opérationnels :

```bash
curl -s -X POST http://localhost:$PORT/graphql \
     -H "Content-Type: application/json" \
     -d '{"query":"{__typename}"}' | grep -q "data"
```

Cette requête :
- Envoie une requête POST au endpoint `/graphql`
- Utilise une query d'introspection GraphQL basique
- Vérifie que la réponse contient le champ "data"
- Retourne 0 (succès) si le service répond correctement

### Ancien comportement (REST)
```bash
curl -s http://localhost:$PORT/api/hotel/info >/dev/null 2>&1 || \
curl -s http://localhost:$PORT/api/agence/ping >/dev/null 2>&1
```

### Nouveau comportement (GraphQL)
```bash
curl -s -X POST http://localhost:$PORT/graphql \
     -H "Content-Type: application/json" \
     -d '{"query":"{__typename}"}' | grep -q "data"
```

## Validation

✅ Tous les scripts ont été vérifiés syntaxiquement avec `bash -n`
✅ Les vérifications GraphQL sont en place dans tous les scripts
✅ Le `.gitignore` a été créé et configuré correctement

## Utilisation

Les scripts fonctionnent exactement de la même manière qu'avant, mais vérifient maintenant les services GraphQL :

```bash
# Démarrage avec reset des bases de données
./rest-all-restart.sh

# Démarrage avec conservation des données
./rest-restart.sh

# Lancement du client uniquement
./rest-client.sh

# Arrêt de tous les services
./arreter-services.sh
```

## Notes importantes

- Les noms des scripts contiennent toujours "rest-" mais lancent maintenant des services GraphQL
- Les ports restent inchangés (8081, 8082, 8083, 8084, 8085)
- La structure de démarrage des services reste identique
- Seule la vérification de santé a été modifiée pour utiliser GraphQL

