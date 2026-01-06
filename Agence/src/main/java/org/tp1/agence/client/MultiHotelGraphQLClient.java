package org.tp1.agence.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tp1.agence.dto.ChambreDTO;
import org.tp1.agence.dto.RechercheRequest;
import org.tp1.agence.dto.ReservationRequest;
import org.tp1.agence.dto.ReservationResponse;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Client GraphQL qui interroge plusieurs hôtels en parallèle
 * Remplace MultiHotelRestClient
 */
@Component
public class MultiHotelGraphQLClient {

    @Autowired
    private HotelGraphQLClient hotelGraphQLClient;

    @Value("${agence.nom:Agence Inconnue}")
    private String agenceNom;

    @Value("${agence.coefficient:1.0}")
    private float agenceCoefficient;

    @Value("${agence.id:agence-default}")
    private String agenceId;

    @Value("${hotel.paris.graphql.url:#{null}}")
    private String hotelParisGraphQLUrl;

    @Value("${hotel.lyon.graphql.url:#{null}}")
    private String hotelLyonGraphQLUrl;

    @Value("${hotel.montpellier.graphql.url:#{null}}")
    private String hotelMontpellierGraphQLUrl;

    private List<String> hotelGraphQLUrls = new ArrayList<>();

    @PostConstruct
    public void init() {
        // Initialiser la liste des URLs GraphQL des hôtels (seulement ceux configurés)
        if (hotelParisGraphQLUrl != null && !hotelParisGraphQLUrl.isEmpty()) {
            hotelGraphQLUrls.add(hotelParisGraphQLUrl);
        }
        if (hotelLyonGraphQLUrl != null && !hotelLyonGraphQLUrl.isEmpty()) {
            hotelGraphQLUrls.add(hotelLyonGraphQLUrl);
        }
        if (hotelMontpellierGraphQLUrl != null && !hotelMontpellierGraphQLUrl.isEmpty()) {
            hotelGraphQLUrls.add(hotelMontpellierGraphQLUrl);
        }

        System.out.println("═══════════════════════════════════════════");
        System.out.println("  " + agenceNom + " - Configuration GraphQL");
        System.out.println("  ID Agence: " + agenceId);
        System.out.println("  Coefficient de prix: " + agenceCoefficient);
        System.out.println("  Nombre d'hôtels: " + hotelGraphQLUrls.size());
        if (hotelParisGraphQLUrl != null) System.out.println("  - Hôtel Paris: " + hotelParisGraphQLUrl);
        if (hotelLyonGraphQLUrl != null) System.out.println("  - Hôtel Lyon: " + hotelLyonGraphQLUrl);
        if (hotelMontpellierGraphQLUrl != null) System.out.println("  - Hôtel Montpellier: " + hotelMontpellierGraphQLUrl);
        System.out.println("═══════════════════════════════════════════");
    }

    /**
     * Recherche des chambres dans tous les hôtels via GraphQL
     * VERSION SÉQUENTIELLE (pas de parallélisme) pour debugging
     */
    public List<ChambreDTO> rechercherChambres(RechercheRequest request) {
        System.out.println("🔍 Recherche GraphQL SÉQUENTIELLE dans " + hotelGraphQLUrls.size() + " hôtels...");

        List<ChambreDTO> toutesLesChambres = new ArrayList<>();

        // Traiter chaque hôtel UN PAR UN (séquentiellement)
        for (String hotelGraphQLUrl : hotelGraphQLUrls) {
            try {
                System.out.println("  → Interrogation de " + hotelGraphQLUrl);

                // Récupérer les infos de l'hôtel EN PREMIER
                Map<String, Object> hotelInfo = hotelGraphQLClient.getHotelInfo(hotelGraphQLUrl);
                String hotelAdresse = (String) hotelInfo.get("adresse");
                String hotelNomFromInfo = (String) hotelInfo.get("nom");

                System.out.println("    Info récupérée: " + hotelNomFromInfo + " - " + hotelAdresse);

                // Récupérer les chambres de cet hôtel
                List<ChambreDTO> chambres = hotelGraphQLClient.rechercherChambres(hotelGraphQLUrl, request);

                if (!chambres.isEmpty()) {
                    System.out.println("    " + chambres.size() + " chambre(s) reçue(s)");

                    // Enrichir chaque chambre
                    for (ChambreDTO chambre : chambres) {
                        System.out.println("      Avant: " + chambre.getNom() + " | hotelNom=" + chambre.getHotelNom() + " | hotelAdresse=" + chambre.getHotelAdresse());

                        // Définir hotelAdresse (pas dans le schema GraphQL de l'hôtel)
                        chambre.setHotelAdresse(hotelAdresse);

                        // Conserver le prix original
                        chambre.setPrixOriginal(chambre.getPrix());

                        // Appliquer le coefficient de prix de l'agence
                        chambre.setPrix(chambre.getPrix() * agenceCoefficient);
                        chambre.setCoefficient(agenceCoefficient);

                        // Ajouter le nom de l'agence
                        chambre.setAgenceNom(agenceNom);

                        System.out.println("      Après: " + chambre.getNom() + " | hotelNom=" + chambre.getHotelNom() + " | hotelAdresse=" + chambre.getHotelAdresse() + " | agence=" + chambre.getAgenceNom());
                    }

                    // Ajouter à la liste totale
                    toutesLesChambres.addAll(chambres);
                    System.out.println("    ✓ [" + hotelGraphQLUrl + "] " + chambres.size() + " chambre(s) ajoutée(s)");
                } else {
                    System.out.println("    ○ [" + hotelGraphQLUrl + "] Aucune chambre disponible");
                }

            } catch (Exception e) {
                System.err.println("    ✗ [" + hotelGraphQLUrl + "] Erreur: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Filtrer par adresse si spécifiée (côté agence)
        if (request.getAdresse() != null && !request.getAdresse().trim().isEmpty()) {
            String adresseRecherchee = request.getAdresse().trim().toLowerCase();
            System.out.println("  Filtrage par adresse: \"" + adresseRecherchee + "\"");

            toutesLesChambres = toutesLesChambres.stream()
                .filter(chambre -> {
                    String hotelAdresse = chambre.getHotelAdresse();
                    String hotelNom = chambre.getHotelNom();

                    boolean matchAdresse = hotelAdresse != null && hotelAdresse.toLowerCase().contains(adresseRecherchee);
                    boolean matchNom = hotelNom != null && hotelNom.toLowerCase().contains(adresseRecherchee);

                    return matchAdresse || matchNom;
                })
                .collect(Collectors.toList());

            System.out.println("  Après filtrage: " + toutesLesChambres.size() + " chambre(s)");
        }

        System.out.println("✅ Total: " + toutesLesChambres.size() + " chambre(s) disponible(s) via GraphQL");

        return toutesLesChambres;
    }

    /**
     * Effectuer une réservation dans l'hôtel correspondant à l'adresse via GraphQL
     */
    public ReservationResponse effectuerReservation(ReservationRequest request) {
        String hotelAdresse = request.getHotelAdresse();

        System.out.println("🏨 Réservation GraphQL pour l'hôtel: " + hotelAdresse);

        // Trouver l'URL GraphQL de l'hôtel correspondant
        String targetHotelGraphQLUrl = null;

        for (String hotelGraphQLUrl : hotelGraphQLUrls) {
            try {
                Map<String, Object> hotelInfo = hotelGraphQLClient.getHotelInfo(hotelGraphQLUrl);
                String adresse = (String) hotelInfo.get("adresse");

                if (adresse != null && adresse.equalsIgnoreCase(hotelAdresse)) {
                    targetHotelGraphQLUrl = hotelGraphQLUrl;
                    break;
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la vérification GraphQL de l'hôtel " + hotelGraphQLUrl + ": " + e.getMessage());
            }
        }

        if (targetHotelGraphQLUrl == null) {
            System.err.println("❌ Hôtel non trouvé pour l'adresse: " + hotelAdresse);
            return ReservationResponse.error("Hôtel non trouvé");
        }

        System.out.println("📡 Envoi de la réservation GraphQL à: " + targetHotelGraphQLUrl);

        // Afficher le contenu de la requête
        System.out.println("📋 ReservationRequest détails:");
        System.out.println("   - chambreId: " + request.getChambreId());
        System.out.println("   - nomClient: " + request.getNomClient());
        System.out.println("   - prenomClient: " + request.getPrenomClient());
        System.out.println("   - emailClient: " + request.getEmailClient());
        System.out.println("   - telephoneClient: " + request.getTelephoneClient());
        System.out.println("   - dateArrive: " + request.getDateArrive());
        System.out.println("   - dateDepart: " + request.getDateDepart());
        System.out.println("   - hotelAdresse: " + request.getHotelAdresse());
        System.out.println("   - prixAvecCoefficient: " + request.getPrixAvecCoefficient());
        System.out.println("   - agenceId: " + agenceId);

        // Ajouter l'agenceId à la requête
        request.setAgenceId(agenceId);

        // Effectuer la réservation via GraphQL
        ReservationResponse response = hotelGraphQLClient.effectuerReservation(targetHotelGraphQLUrl, request);

        if (response.isSuccess()) {
            System.out.println("✅ Réservation GraphQL réussie: " + response.getMessage());
        } else {
            System.err.println("❌ Échec de la réservation GraphQL: " + response.getMessage());
        }

        return response;
    }

    /**
     * Obtenir toutes les réservations de tous les hôtels partenaires
     * Filtre uniquement les réservations de cette agence
     */
    public List<Map<String, Object>> getAllReservations() {
        System.out.println("📋 Récupération des réservations de " + hotelGraphQLUrls.size() + " hôtels pour l'agence: " + agenceId);

        List<Map<String, Object>> allReservations = new ArrayList<>();

        // Interroger chaque hôtel séquentiellement
        for (String hotelGraphQLUrl : hotelGraphQLUrls) {
            try {
                // Récupérer les infos de l'hôtel
                Map<String, Object> hotelInfo = hotelGraphQLClient.getHotelInfo(hotelGraphQLUrl);
                String hotelNom = (String) hotelInfo.get("nom");

                // Récupérer les réservations de cet hôtel
                List<Map<String, Object>> reservations = hotelGraphQLClient.getReservations(hotelGraphQLUrl);

                // Filtrer et enrichir uniquement les réservations de cette agence
                int countForAgence = 0;
                for (Map<String, Object> reservation : reservations) {
                    String reservationAgenceId = (String) reservation.get("agenceId");

                    // Ne garder que les réservations de cette agence
                    if (agenceId.equals(reservationAgenceId)) {
                        reservation.put("hotelNom", hotelNom);
                        allReservations.add(reservation);
                        countForAgence++;
                    }
                }

                System.out.println("  ✅ " + hotelNom + ": " + countForAgence + " réservation(s) pour " + agenceId);
            } catch (Exception e) {
                System.err.println("  ❌ Erreur avec " + hotelGraphQLUrl + ": " + e.getMessage());
            }
        }

        System.out.println("📊 Total: " + allReservations.size() + " réservation(s) pour l'agence " + agenceId);
        return allReservations;
    }

    /**
     * Obtenir les informations de tous les hôtels
     */
    public List<Map<String, Object>> getAllHotelsInfo() {
        return hotelGraphQLUrls.stream()
            .map(url -> hotelGraphQLClient.getHotelInfo(url))
            .filter(info -> !info.isEmpty())
            .collect(Collectors.toList());
    }
}

