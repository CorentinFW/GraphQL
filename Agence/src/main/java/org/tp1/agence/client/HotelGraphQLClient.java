package org.tp1.agence.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.tp1.agence.dto.ChambreDTO;
import org.tp1.agence.dto.RechercheRequest;
import org.tp1.agence.dto.ReservationRequest;
import org.tp1.agence.dto.ReservationResponse;

import java.util.*;

/**
 * Client GraphQL pour communiquer avec un hôtel via GraphQL
 * Remplace HotelRestClient
 */
@Component
public class HotelGraphQLClient {

    private final WebClient.Builder webClientBuilder;

    public HotelGraphQLClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * Rechercher des chambres disponibles dans un hôtel via GraphQL
     */
    public List<ChambreDTO> rechercherChambres(String hotelGraphQLUrl, RechercheRequest request) {
        try {
            // Construction de la query GraphQL
            // NE PAS envoyer l'adresse - on filtre côté agence après
            String query = "query {" +
                "  rechercherChambres(criteres: {" +
                "    adresse: \"\"" +  // Toujours vide pour récupérer toutes les chambres
                "    dateArrive: \"" + request.getDateArrive() + "\"" +
                "    dateDepart: \"" + request.getDateDepart() + "\"" +
                "    prixMin: " + (request.getPrixMin() != null ? request.getPrixMin() : "null") +
                "    prixMax: " + (request.getPrixMax() != null ? request.getPrixMax() : "null") +
                "    nbrEtoile: " + (request.getNbrEtoile() != null ? request.getNbrEtoile() : "null") +
                "    nbrLits: " + (request.getNbrLits() != null ? request.getNbrLits() : "null") +
                "  }) {" +
                "    id" +
                "    nom" +
                "    prix" +
                "    nbrDeLit" +
                "    nbrEtoiles" +
                "    disponible" +
                "    imageUrl" +
                "    hotelNom" +
                "  }" +
                "}";

            Map<String, Object> requestBody = Map.of("query", query);

            WebClient webClient = webClientBuilder.baseUrl(hotelGraphQLUrl).build();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response != null && response.containsKey("data")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");

                if (data.containsKey("rechercherChambres")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> chambres = (List<Map<String, Object>>) data.get("rechercherChambres");

                    return chambres.stream()
                        .map(this::mapToChambreDTO)
                        .toList();
                }
            }

            return Collections.emptyList();

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la recherche GraphQL à " + hotelGraphQLUrl + ": " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Effectuer une réservation dans un hôtel via GraphQL
     */
    public ReservationResponse effectuerReservation(String hotelGraphQLUrl, ReservationRequest request) {
        try {
            // Construction de la mutation GraphQL
            String mutation = """
                mutation {
                  creerReservation(reservation: {
                    chambreId: "%s"
                    nomClient: "%s"
                    prenomClient: "%s"
                    emailClient: "%s"
                    telephoneClient: "%s"
                    dateArrive: "%s"
                    dateDepart: "%s"
                  }) {
                    success
                    message
                    reservationId
                  }
                }
                """.formatted(
                request.getChambreId(),
                request.getNomClient(),
                request.getPrenomClient(),
                request.getEmailClient() != null ? request.getEmailClient() : "",
                request.getTelephoneClient() != null ? request.getTelephoneClient() : "",
                request.getDateArrive(),
                request.getDateDepart()
            );

            Map<String, Object> requestBody = Map.of("query", mutation);

            WebClient webClient = webClientBuilder.baseUrl(hotelGraphQLUrl).build();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response != null && response.containsKey("data")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");

                if (data.containsKey("creerReservation")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) data.get("creerReservation");

                    boolean success = (Boolean) result.get("success");
                    String message = (String) result.get("message");
                    Object reservationId = result.get("reservationId");

                    if (success && reservationId != null) {
                        return ReservationResponse.success(
                            Long.parseLong(reservationId.toString()),
                            message
                        );
                    } else {
                        return ReservationResponse.error(message);
                    }
                }
            }

            return ReservationResponse.error("Erreur lors de la réservation");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la réservation GraphQL à " + hotelGraphQLUrl + ": " + e.getMessage());
            e.printStackTrace();
            return ReservationResponse.error("Erreur: " + e.getMessage());
        }
    }

    /**
     * Obtenir les informations d'un hôtel via GraphQL
     */
    public Map<String, Object> getHotelInfo(String hotelGraphQLUrl) {
        try {
            String query = """
                query {
                  hotelInfo {
                    nom
                    adresse
                    ville
                    telephone
                  }
                }
                """;

            Map<String, Object> requestBody = Map.of("query", query);

            WebClient webClient = webClientBuilder.baseUrl(hotelGraphQLUrl).build();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response != null && response.containsKey("data")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");

                if (data.containsKey("hotelInfo")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> hotelInfo = (Map<String, Object>) data.get("hotelInfo");
                    return hotelInfo;
                }
            }

            return Collections.emptyMap();

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des infos GraphQL à " + hotelGraphQLUrl + ": " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Mapper un objet Map vers ChambreDTO
     */
    private ChambreDTO mapToChambreDTO(Map<String, Object> map) {
        ChambreDTO dto = new ChambreDTO();

        if (map.get("id") != null) {
            dto.setId(Long.parseLong(map.get("id").toString()));
        }
        dto.setNom((String) map.get("nom"));

        if (map.get("prix") != null) {
            dto.setPrix(((Number) map.get("prix")).floatValue());
        }
        if (map.get("nbrDeLit") != null) {
            dto.setNbrLits(((Number) map.get("nbrDeLit")).intValue());
        }
        if (map.get("nbrEtoiles") != null) {
            dto.setNbrEtoiles(((Number) map.get("nbrEtoiles")).intValue());
        }
        if (map.get("disponible") != null) {
            dto.setDisponible((Boolean) map.get("disponible"));
        }
        dto.setImage((String) map.get("imageUrl"));
        dto.setHotelNom((String) map.get("hotelNom"));

        return dto;
    }
}

