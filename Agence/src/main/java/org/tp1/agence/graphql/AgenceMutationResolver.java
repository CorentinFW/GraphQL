package org.tp1.agence.graphql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.tp1.agence.dto.ReservationRequest;
import org.tp1.agence.dto.ReservationResponse;
import org.tp1.agence.service.AgenceService;

/**
 * Resolver GraphQL pour les mutations de l'agence
 */
@Controller
public class AgenceMutationResolver {

    @Autowired
    private AgenceService agenceService;

    /**
     * Mutation: effectuerReservation
     * Créer une réservation dans l'hôtel sélectionné
     */
    @MutationMapping
    public ReservationResponse effectuerReservation(@Argument ReservationRequest reservation) {
        System.out.println("🏨 GraphQL Mutation: Tentative de réservation chambre " +
                          reservation.getChambreId() + " à " + reservation.getHotelAdresse());

        ReservationResponse response = agenceService.effectuerReservation(reservation);

        if (response.isSuccess()) {
            System.out.println("✅ Réservation GraphQL effectuée avec succès: " + response.getMessage());
        } else {
            System.out.println("❌ Échec de la réservation GraphQL: " + response.getMessage());
        }

        return response;
    }
}

