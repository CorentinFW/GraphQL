package org.tp1.agence.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tp1.agence.client.MultiHotelGraphQLClient;
import org.tp1.agence.dto.ChambreDTO;
import org.tp1.agence.dto.RechercheRequest;
import org.tp1.agence.dto.ReservationRequest;
import org.tp1.agence.dto.ReservationResponse;

import java.util.List;

/**
 * Service de l'agence qui orchestre les appels GraphQL aux hôtels
 * Modifié pour utiliser GraphQL au lieu de REST
 */
@Service
public class AgenceService {

    @Autowired
    private MultiHotelGraphQLClient multiHotelGraphQLClient;

    /**
     * Recherche des chambres disponibles dans tous les hôtels via GraphQL
     */
    public List<ChambreDTO> rechercherChambres(RechercheRequest request) {
        // Validation des paramètres
        if (request.getDateArrive() == null || request.getDateArrive().isEmpty()) {
            throw new IllegalArgumentException("La date d'arrivée est obligatoire");
        }
        if (request.getDateDepart() == null || request.getDateDepart().isEmpty()) {
            throw new IllegalArgumentException("La date de départ est obligatoire");
        }

        // Appel au client GraphQL pour interroger les hôtels
        return multiHotelGraphQLClient.rechercherChambres(request);
    }

    /**
     * Effectue une réservation dans un hôtel via GraphQL
     */
    public ReservationResponse effectuerReservation(ReservationRequest request) {
        // Validation des paramètres
        if (request.getNomClient() == null || request.getNomClient().isEmpty()) {
            return ReservationResponse.error("Le nom du client est obligatoire");
        }
        if (request.getPrenomClient() == null || request.getPrenomClient().isEmpty()) {
            return ReservationResponse.error("Le prénom du client est obligatoire");
        }
        if (request.getChambreId() == null || request.getChambreId() <= 0) {
            return ReservationResponse.error("L'ID de la chambre est invalide");
        }

        try {
            // Appel au client GraphQL pour effectuer la réservation
            return multiHotelGraphQLClient.effectuerReservation(request);
        } catch (Exception e) {
            return ReservationResponse.error("Erreur lors de la réservation: " + e.getMessage());
        }
    }
}

