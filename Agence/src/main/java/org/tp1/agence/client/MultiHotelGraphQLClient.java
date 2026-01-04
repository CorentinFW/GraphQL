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
        System.out.println("  Coefficient de prix: " + agenceCoefficient);
        System.out.println("  Nombre d'hôtels: " + hotelGraphQLUrls.size());
        if (hotelParisGraphQLUrl != null) System.out.println("  - Hôtel Paris: " + hotelParisGraphQLUrl);
        if (hotelLyonGraphQLUrl != null) System.out.println("  - Hôtel Lyon: " + hotelLyonGraphQLUrl);
        if (hotelMontpellierGraphQLUrl != null) System.out.println("  - Hôtel Montpellier: " + hotelMontpellierGraphQLUrl);
        System.out.println("═══════════════════════════════════════════");
    }

    /**
     * Recherche des chambres dans tous les hôtels en parallèle via GraphQL
     */
    public List<ChambreDTO> rechercherChambres(RechercheRequest request) {
        System.out.println("🔍 Recherche GraphQL dans " + hotelGraphQLUrls.size() + " hôtels...");

        // Créer des tâches asynchrones pour chaque hôtel
        List<CompletableFuture<List<ChambreDTO>>> futures = hotelGraphQLUrls.stream()
            .map(hotelGraphQLUrl -> CompletableFuture.supplyAsync(() -> {
                try {
                    List<ChambreDTO> chambres = hotelGraphQLClient.rechercherChambres(hotelGraphQLUrl, request);

                    if (!chambres.isEmpty()) {
                        // Récupérer les infos de l'hôtel pour enrichir les chambres
                        Map<String, Object> hotelInfo = hotelGraphQLClient.getHotelInfo(hotelGraphQLUrl);
                        String hotelNom = (String) hotelInfo.get("nom");
                        String hotelAdresse = (String) hotelInfo.get("adresse");

                        // Enrichir chaque chambre avec les infos de l'hôtel
                        for (ChambreDTO chambre : chambres) {
                            if (hotelNom != null) chambre.setHotelNom(hotelNom);
                            if (hotelAdresse != null) chambre.setHotelAdresse(hotelAdresse);

                            // Conserver le prix original
                            chambre.setPrixOriginal(chambre.getPrix());

                            // Appliquer le coefficient de prix de l'agence
                            chambre.setPrix(chambre.getPrix() * agenceCoefficient);
                            chambre.setCoefficient(agenceCoefficient);

                            // Ajouter le nom de l'agence
                            chambre.setAgenceNom(agenceNom);
                        }

                        System.out.println("✓ [" + hotelGraphQLUrl + "] Trouvé " + chambres.size() + " chambre(s)");
                    } else {
                        System.out.println("○ [" + hotelGraphQLUrl + "] Aucune chambre disponible");
                    }

                    return chambres;
                } catch (Exception e) {
                    System.err.println("✗ [" + hotelGraphQLUrl + "] Erreur: " + e.getMessage());
                    e.printStackTrace();
                    return new ArrayList<ChambreDTO>();
                }
            }))
            .collect(Collectors.toList());

        // Attendre que toutes les tâches se terminent et agréger les résultats
        List<ChambreDTO> toutesLesChambres = futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());

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
     * Obtenir les informations de tous les hôtels
     */
    public List<Map<String, Object>> getAllHotelsInfo() {
        return hotelGraphQLUrls.stream()
            .map(url -> hotelGraphQLClient.getHotelInfo(url))
            .filter(info -> !info.isEmpty())
            .collect(Collectors.toList());
    }
}

