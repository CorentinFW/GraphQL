package org.tp1.agence.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.tp1.agence.dto.ChambreDTO;
import org.tp1.agence.dto.RechercheRequest;
import org.tp1.agence.dto.ReservationRequest;
import org.tp1.agence.dto.ReservationResponse;

import java.util.*;
import java.util.stream.Collectors;

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
            // Ne pas inclure telephoneClient s'il est vide (GraphQL n'accepte pas "" pour un champ optionnel)
            String telephoneClientField = "";
            if (request.getTelephoneClient() != null && !request.getTelephoneClient().trim().isEmpty()) {
                telephoneClientField = "    telephoneClient: \"" + request.getTelephoneClient() + "\"\n";
            }

            // Ne pas inclure numeroCarteBancaire s'il est vide
            String numeroCarteBancaireField = "";
            if (request.getClientNumeroCarteBleue() != null && !request.getClientNumeroCarteBleue().trim().isEmpty()) {
                numeroCarteBancaireField = "    numeroCarteBancaire: \"" + request.getClientNumeroCarteBleue() + "\"\n";
            }

            String agenceIdField = "";
            if (request.getAgenceId() != null && !request.getAgenceId().trim().isEmpty()) {
                agenceIdField = "    agenceId: \"" + request.getAgenceId() + "\"\n";
            }

            String prixAvecCoefficientField = "";
            if (request.getPrixAvecCoefficient() != null) {
                prixAvecCoefficientField = "    prixAvecCoefficient: " + request.getPrixAvecCoefficient() + "\n";
            }

            String mutation = String.format("""
                mutation {
                  creerReservation(reservation: {
                    chambreId: "%s"
                    nomClient: "%s"
                    prenomClient: "%s"
                    emailClient: "%s"
                %s%s%s%s    dateArrive: "%s"
                    dateDepart: "%s"
                  }) {
                    success
                    message
                    reservationId
                  }
                }
                """,
                request.getChambreId(),
                request.getNomClient(),
                request.getPrenomClient(),
                request.getEmailClient() != null ? request.getEmailClient() : "",
                telephoneClientField,  // Sera soit "    telephoneClient: "..."\n" soit ""
                numeroCarteBancaireField,  // Sera soit "    numeroCarteBancaire: "..."\n" soit ""
                agenceIdField,  // Sera soit "    agenceId: "..."\n" soit ""
                prixAvecCoefficientField,  // Sera soit "    prixAvecCoefficient: X.X\n" soit ""
                request.getDateArrive(),
                request.getDateDepart()
            );

            System.out.println("🔍 MUTATION ENVOYÉE À L'HÔTEL:");
            System.out.println(mutation);

            Map<String, Object> requestBody = Map.of("query", mutation);

            WebClient webClient = webClientBuilder.baseUrl(hotelGraphQLUrl).build();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            // Vérifier d'abord s'il y a des erreurs GraphQL
            if (response != null && response.containsKey("errors")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> errors = (List<Map<String, Object>>) response.get("errors");
                String errorMessage = errors.stream()
                    .map(err -> (String) err.get("message"))
                    .collect(Collectors.joining(", "));

                System.err.println("❌ Erreur GraphQL de l'hôtel: " + errorMessage);
                return ReservationResponse.error("Erreur GraphQL: " + errorMessage);
            }

            if (response != null && response.containsKey("data")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");

                if (data != null && data.containsKey("creerReservation")) {
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

    /**
     * Récupérer toutes les réservations d'un hôtel via GraphQL
     */
    public List<Map<String, Object>> getReservations(String hotelGraphQLUrl) {
        try {
            String query = """
                query {
                  reservations {
                    id
                    chambreId
                    nomClient
                    prenomClient
                    emailClient
                    telephoneClient
                    dateArrive
                    dateDepart
                    prixTotal
                    agenceId
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

                if (data != null && data.containsKey("reservations")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> reservations = (List<Map<String, Object>>) data.get("reservations");
                    return reservations;
                }
            }

            return Collections.emptyList();

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des réservations de " + hotelGraphQLUrl + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }
}

