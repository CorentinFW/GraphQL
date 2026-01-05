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
     * VERSION SÉQUENTIELLE pour éviter les problèmes de concurrence
     */
    public List<ChambreDTO> rechercherChambres(String adresse, String dateArrive, String dateDepart,
                                               Float prixMin, Float prixMax, Integer nbrEtoile, Integer nbrLits) {
        System.out.println("🔍 Recherche GraphQL SÉQUENTIELLE dans " + agenceGraphQLUrls.size() + " agences...");

        List<ChambreDTO> toutesLesChambres = new ArrayList<>();

        // Traiter chaque agence UNE PAR UNE (séquentiellement)
        for (String agenceGraphQLUrl : agenceGraphQLUrls) {
            try {
                System.out.println("  → Interrogation agence: " + agenceGraphQLUrl);

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
                    System.out.println("    ✓ Trouvé " + chambres.size() + " chambre(s)");
                    toutesLesChambres.addAll(chambres);
                } else {
                    System.out.println("    ○ Aucune chambre disponible");
                }

            } catch (Exception e) {
                System.err.println("    ✗ Erreur: " + e.getMessage());
                e.printStackTrace();
            }
        }

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

    /**
     * Obtenir la liste des hôtels disponibles
     */
    public List<Map<String, Object>> getAllHotels() {
        System.out.println("🏨 Récupération de la liste des hôtels...");

        List<Map<String, Object>> allHotels = new ArrayList<>();
        Set<String> hotelNames = new HashSet<>();

        // Pour chaque agence
        for (String agenceGraphQLUrl : agenceGraphQLUrls) {
            try {
                List<Map<String, Object>> hotels = agenceGraphQLClient.getHotels(agenceGraphQLUrl);

                for (Map<String, Object> hotel : hotels) {
                    String hotelName = (String) hotel.get("nom");
                    // Éviter les doublons (Lyon est dans les 2 agences)
                    if (hotelName != null && !hotelNames.contains(hotelName)) {
                        hotelNames.add(hotelName);
                        allHotels.add(hotel);
                    }
                }

                System.out.println("✓ [" + agenceGraphQLUrl + "] " + hotels.size() + " hôtel(s)");
            } catch (Exception e) {
                System.err.println("✗ [" + agenceGraphQLUrl + "] Erreur: " + e.getMessage());
            }
        }

        System.out.println("✅ Total: " + allHotels.size() + " hôtel(s) unique(s)");

        return allHotels;
    }

    /**
     * Obtenir toutes les réservations de toutes les agences
     */
    public Map<String, List<Map<String, Object>>> getAllReservations() {
        System.out.println("📋 Récupération des réservations de toutes les agences...");

        Map<String, List<Map<String, Object>>> reservationsByAgence = new LinkedHashMap<>();

        // Interroger l'agence 1
        if (agence1GraphQLUrl != null) {
            try {
                List<Map<String, Object>> reservations = agenceGraphQLClient.getReservations(agence1GraphQLUrl);
                reservationsByAgence.put(agence1Name, reservations);
                System.out.println("  ✅ " + agence1Name + ": " + reservations.size() + " réservation(s)");
            } catch (Exception e) {
                System.err.println("  ❌ Erreur " + agence1Name + ": " + e.getMessage());
                reservationsByAgence.put(agence1Name, List.of());
            }
        }

        // Interroger l'agence 2
        if (agence2GraphQLUrl != null) {
            try {
                List<Map<String, Object>> reservations = agenceGraphQLClient.getReservations(agence2GraphQLUrl);
                reservationsByAgence.put(agence2Name, reservations);
                System.out.println("  ✅ " + agence2Name + ": " + reservations.size() + " réservation(s)");
            } catch (Exception e) {
                System.err.println("  ❌ Erreur " + agence2Name + ": " + e.getMessage());
                reservationsByAgence.put(agence2Name, List.of());
            }
        }

        int total = reservationsByAgence.values().stream().mapToInt(List::size).sum();
        System.out.println("📊 Total: " + total + " réservation(s)");

        return reservationsByAgence;
    }
}

