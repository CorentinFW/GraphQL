package org.tp1.hotellerie.graphql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.tp1.hotellerie.dto.ReservationRequest;
import org.tp1.hotellerie.dto.ReservationResponse;
import org.tp1.hotellerie.model.Client;
import org.tp1.hotellerie.repository.ReservationRepository;
import org.tp1.hotellerie.service.HotelService;

/**
 * Resolver GraphQL pour les mutations (modification de données)
 * Remplace les endpoints POST/PUT/DELETE du HotelController REST
 */
@Controller
public class HotelMutationResolver {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private ReservationRepository reservationRepository;

    /**
     * Mutation: creerReservation
     * Crée une réservation pour une chambre
     * Remplace: POST /api/hotel/reservations
     */
    @MutationMapping
    public ReservationResponse creerReservation(@Argument ReservationRequest reservation) {
        // Créer le client
        Client client = new Client(
            reservation.getNomClient(),
            reservation.getPrenomClient(),
            reservation.getNumeroCarteBancaire()
        );

        // Effectuer la réservation (utilise l'ID de la chambre + agenceId)
        HotelService.ReservationResult result = hotelService.effectuerReservation(
            client,
            reservation.getChambreId(),
            reservation.getDateArrive(),
            reservation.getDateDepart(),
            reservation.getAgenceId()  // Passer l'agenceId
        );

        if (result.isSuccess()) {
            return ReservationResponse.success(
                (long) result.getReservationId(),
                result.getMessage()
            );
        } else {
            return ReservationResponse.error(result.getMessage());
        }
    }

    /**
     * Mutation: annulerReservation
     * Annule une réservation existante
     * Nouvelle fonctionnalité GraphQL
     */
    @MutationMapping
    public Boolean annulerReservation(@Argument String reservationId) {
        try {
            Long id = Long.parseLong(reservationId);

            // Vérifier si la réservation existe
            if (reservationRepository.existsById(id)) {
                reservationRepository.deleteById(id);
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

