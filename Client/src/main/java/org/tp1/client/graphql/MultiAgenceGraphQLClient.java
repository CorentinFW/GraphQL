package org.tp1.client.graphql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tp1.client.dto.ChambreDTO;
import org.tp1.client.dto.ReservationResponse;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Client GraphQL qui agrège les résultats de plusieurs agences
 * Permet de voir toutes les chambres, même celles en commun entre agences
 * Remplace MultiAgenceRestClient
 */
@Component
public class MultiAgenceGraphQLClient {

    private final AgenceGraphQLClient agenceGraphQLClient;

    @Value("${agence1.graphql.url:http://localhost:8081/graphql}")
    private String agence1GraphQLUrl;

    @Value("${agence2.graphql.url:http://localhost:8085/graphql}")
    private String agence2GraphQLUrl;

    @Value("${agence1.name:Agence Paris Voyages}")
    private String agence1Name;

    @Value("${agence2.name:Agence Sud Reservations}")
    private String agence2Name;

    private List<String> agenceGraphQLUrls;

    public MultiAgenceGraphQLClient(AgenceGraphQLClient agenceGraphQLClient) {
        this.agenceGraphQLClient = agenceGraphQLClient;
    }

    @PostConstruct
    private void initAgenceUrls() {
        agenceGraphQLUrls = new ArrayList<>();
        agenceGraphQLUrls.add(agence1GraphQLUrl);
        agenceGraphQLUrls.add(agence2GraphQLUrl);

        System.out.println("═══════════════════════════════════════════");
        System.out.println("  Client GraphQL - Configuration");
        System.out.println("  Agence 1: " + agence1GraphQLUrl);
        System.out.println("  Agence 2: " + agence2GraphQLUrl);
        System.out.println("═══════════════════════════════════════════");
    }

    /**
     * Test de connexion aux agences via GraphQL
     */
    public String ping() {
        StringBuilder result = new StringBuilder();

        for (String agenceGraphQLUrl : agenceGraphQLUrls) {
            try {
                String message = agenceGraphQLClient.ping(agenceGraphQLUrl);
                result.append(message).append(" | ");
            } catch (Exception e) {
                result.append("[").append(agenceGraphQLUrl).append(": ERREUR] | ");
            }
        }

        return result.length() > 0 ? result.toString() : "Aucune agence disponible";
    }

    /**
     * Rechercher des chambres disponibles dans TOUTES les agences via GraphQL
     * Retourne TOUTES les chambres, y compris les doublons (même chambre proposée par plusieurs agences)
     */
    public List<ChambreDTO> rechercherChambres(String adresse, String dateArrive, String dateDepart,
                                               Float prixMin, Float prixMax, Integer nbrEtoile, Integer nbrLits) {
        System.out.println("🔍 Recherche GraphQL dans " + agenceGraphQLUrls.size() + " agences en parallèle...");

        // Créer des tâches asynchrones pour chaque agence
        List<CompletableFuture<List<ChambreDTO>>> futures = agenceGraphQLUrls.stream()
            .map(agenceGraphQLUrl -> CompletableFuture.supplyAsync(() -> {
                try {
                    List<ChambreDTO> chambres = agenceGraphQLClient.rechercherChambres(
                        agenceGraphQLUrl,
                        adresse,
                        dateArrive,
                        dateDepart,
                        prixMin,
                        prixMax,
                        nbrEtoile,
                        nbrLits
                    );

                    if (!chambres.isEmpty()) {
                        System.out.println("✓ [" + agenceGraphQLUrl + "] Trouvé " + chambres.size() + " chambre(s)");
                    } else {
                        System.out.println("○ [" + agenceGraphQLUrl + "] Aucune chambre disponible");
                    }

                    return chambres;

                } catch (Exception e) {
                    System.err.println("✗ [" + agenceGraphQLUrl + "] Erreur: " + e.getMessage());
                    e.printStackTrace();
                    return Collections.<ChambreDTO>emptyList();
                }
            }))
            .collect(Collectors.toList());

        // Attendre que toutes les tâches se terminent et agréger les résultats
        // On garde TOUS les résultats, même les doublons
        List<ChambreDTO> toutesLesChambres = futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        System.out.println("✅ Total: " + toutesLesChambres.size() + " chambre(s) disponible(s) via GraphQL");

        return toutesLesChambres;
    }

    /**
     * Effectuer une réservation via GraphQL
     * On choisit l'agence en fonction de l'agenceNom dans la chambre
     */
    public ReservationResponse effectuerReservation(String clientNom, String clientPrenom,
                                                    String clientEmail, String clientTelephone,
                                                    String numeroCarteBancaire, Long chambreId,
                                                    String hotelAdresse, String dateArrive,
                                                    String dateDepart, String agenceNom) {
        // Trouver l'URL GraphQL de l'agence correspondante
        String targetAgenceGraphQLUrl = null;

        if (agenceNom != null) {
            if (agenceNom.contains(agence1Name) || agenceNom.equals(agence1Name)) {
                targetAgenceGraphQLUrl = agence1GraphQLUrl;
            } else if (agenceNom.contains(agence2Name) || agenceNom.equals(agence2Name)) {
                targetAgenceGraphQLUrl = agence2GraphQLUrl;
            }
        }

        // Si on n'a pas trouvé l'agence, utiliser la première par défaut
        if (targetAgenceGraphQLUrl == null) {
            targetAgenceGraphQLUrl = agence1GraphQLUrl;
            System.out.println("⚠️ Agence non trouvée, utilisation de l'agence par défaut");
        }

        System.out.println("📡 Envoi de la réservation GraphQL à: " + targetAgenceGraphQLUrl);

        // Effectuer la réservation via GraphQL
        ReservationResponse response = agenceGraphQLClient.effectuerReservation(
            targetAgenceGraphQLUrl,
            clientNom,
            clientPrenom,
            clientEmail,
            clientTelephone,
            numeroCarteBancaire,
            chambreId,
            hotelAdresse,
            dateArrive,
            dateDepart
        );

        if (response.isSuccess()) {
            System.out.println("✅ Réservation GraphQL réussie: " + response.getMessage());
        } else {
            System.err.println("❌ Échec de la réservation GraphQL: " + response.getMessage());
        }

        return response;
    }

    /**
     * Obtenir le nom de l'agence 1
     */
    public String getAgence1Name() {
        return agence1Name;
    }

    /**
     * Obtenir le nom de l'agence 2
     */
    public String getAgence2Name() {
        return agence2Name;
    }
}

