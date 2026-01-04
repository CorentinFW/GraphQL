package org.tp1.client.graphql;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.tp1.client.dto.ChambreDTO;
import org.tp1.client.dto.ReservationResponse;

import java.util.*;

/**
 * Client GraphQL pour communiquer avec une Agence via GraphQL
 * Remplace AgenceRestClient
 */
@Component
public class AgenceGraphQLClient {

    private final WebClient.Builder webClientBuilder;

    public AgenceGraphQLClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * Test de connexion à l'agence via GraphQL
     */
    public String ping(String agenceGraphQLUrl) {
        try {
            String query = """
                query {
                  ping {
                    message
                    status
                    timestamp
                  }
                }
                """;

            Map<String, Object> requestBody = Map.of("query", query);

            WebClient webClient = webClientBuilder.baseUrl(agenceGraphQLUrl).build();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response != null && response.containsKey("data")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");

                if (data.containsKey("ping")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> ping = (Map<String, Object>) data.get("ping");
                    return (String) ping.get("message");
                }
            }

            return "Agence non disponible";

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du ping GraphQL de l'agence: " + e.getMessage());
            return "Erreur: " + e.getMessage();
        }
    }

    /**
     * Rechercher des chambres disponibles via GraphQL
     */
    public List<ChambreDTO> rechercherChambres(String agenceGraphQLUrl, String adresse, String dateArrive,
                                               String dateDepart, Float prixMin, Float prixMax,
                                               Integer nbrEtoile, Integer nbrLits) {
        try {
            // Construction de la query GraphQL
            String query = """
                query {
                  rechercherChambres(criteres: {
                    adresse: "%s"
                    dateArrive: "%s"
                    dateDepart: "%s"
                    prixMin: %s
                    prixMax: %s
                    nbrEtoile: %s
                    nbrLits: %s
                  }) {
                    id
                    nom
                    prix
                    prixOriginal
                    coefficient
                    nbrDeLit
                    nbrEtoiles
                    disponible
                    imageUrl
                    hotelNom
                    hotelAdresse
                    agenceNom
                  }
                }
                """.formatted(
                adresse != null ? adresse : "",
                dateArrive,
                dateDepart,
                prixMin != null ? prixMin : "null",
                prixMax != null ? prixMax : "null",
                nbrEtoile != null ? nbrEtoile : "null",
                nbrLits != null ? nbrLits : "null"
            );

            Map<String, Object> requestBody = Map.of("query", query);

            WebClient webClient = webClientBuilder.baseUrl(agenceGraphQLUrl).build();

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
            System.err.println("❌ Erreur lors de la recherche GraphQL: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Effectuer une réservation via GraphQL
     */
    public ReservationResponse effectuerReservation(String agenceGraphQLUrl, String clientNom,
                                                    String clientPrenom, String clientEmail,
                                                    String clientTelephone, String numeroCarteBancaire,
                                                    Long chambreId, String hotelAdresse,
                                                    String dateArrive, String dateDepart) {
        try {
            // Construction de la mutation GraphQL
            String mutation = """
                mutation {
                  effectuerReservation(reservation: {
                    chambreId: "%s"
                    hotelAdresse: "%s"
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
                    hotelNom
                  }
                }
                """.formatted(
                chambreId,
                hotelAdresse,
                clientNom,
                clientPrenom,
                clientEmail != null ? clientEmail : "",
                clientTelephone != null ? clientTelephone : "",
                dateArrive,
                dateDepart
            );

            Map<String, Object> requestBody = Map.of("query", mutation);

            WebClient webClient = webClientBuilder.baseUrl(agenceGraphQLUrl).build();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response != null && response.containsKey("data")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");

                if (data.containsKey("effectuerReservation")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) data.get("effectuerReservation");

                    ReservationResponse reservationResponse = new ReservationResponse();
                    reservationResponse.setSuccess((Boolean) result.get("success"));
                    reservationResponse.setMessage((String) result.get("message"));

                    Object reservationId = result.get("reservationId");
                    if (reservationId != null) {
                        reservationResponse.setReservationId(Long.parseLong(reservationId.toString()));
                    }

                    return reservationResponse;
                }
            }

            ReservationResponse errorResponse = new ReservationResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Erreur lors de la réservation");
            return errorResponse;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la réservation GraphQL: " + e.getMessage());
            e.printStackTrace();
            ReservationResponse errorResponse = new ReservationResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Erreur: " + e.getMessage());
            return errorResponse;
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
        if (map.get("prixOriginal") != null) {
            dto.setPrixOriginal(((Number) map.get("prixOriginal")).floatValue());
        }
        if (map.get("coefficient") != null) {
            dto.setCoefficient(((Number) map.get("coefficient")).floatValue());
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

        dto.setImageUrl((String) map.get("imageUrl"));
        dto.setHotelNom((String) map.get("hotelNom"));
        dto.setHotelAdresse((String) map.get("hotelAdresse"));
        dto.setAgenceNom((String) map.get("agenceNom"));

        return dto;
    }
}

