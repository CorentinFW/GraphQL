package org.tp1.hotellerie.graphql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.tp1.hotellerie.dto.ChambreDTO;
import org.tp1.hotellerie.dto.HotelInfoDTO;
import org.tp1.hotellerie.dto.RechercheRequest;
import org.tp1.hotellerie.model.Chambre;
import org.tp1.hotellerie.model.Hotel;
import org.tp1.hotellerie.model.Reservation;
import org.tp1.hotellerie.service.HotelService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Resolver GraphQL pour les queries (lecture de données)
 * Remplace les endpoints GET du HotelController REST
 */
@Controller
public class HotelQueryResolver {

    @Autowired
    private HotelService hotelService;

    /**
     * Query: hotelInfo
     * Retourne les informations de l'hôtel
     * Remplace: GET /api/hotel/info
     */
    @QueryMapping
    public HotelInfoDTO hotelInfo() {
        Hotel hotel = hotelService.getHotel();

        return new HotelInfoDTO(
            hotel.getNom(),
            hotel.getAdresse(),
            null, // Ville non disponible dans le modèle actuel
            null  // Téléphone non disponible
        );
    }

    /**
     * Query: rechercherChambres
     * Recherche des chambres disponibles selon des critères
     * Remplace: POST /api/hotel/chambres/rechercher
     */
    @QueryMapping
    public List<ChambreDTO> rechercherChambres(@Argument RechercheRequest criteres) {
        List<Chambre> chambres = hotelService.rechercherChambres(
            criteres.getAdresse(),
            criteres.getDateArrive(),
            criteres.getDateDepart(),
            criteres.getPrixMin(),
            criteres.getPrixMax(),
            criteres.getNbrEtoile(),
            criteres.getNbrLits()
        );

        Hotel hotel = hotelService.getHotel();

        // Convertir les Chambres en ChambreDTO
        return chambres.stream()
            .map(chambre -> {
                ChambreDTO dto = new ChambreDTO(
                    chambre.getId(),
                    chambre.getNom(),
                    chambre.getPrix(),
                    chambre.getNbrDeLit(),
                    hotel.getType().ordinal() + 1, // Nombre d'étoiles
                    true, // Disponible si dans les résultats
                    chambre.getImageUrl()
                );
                dto.setHotelNom(hotel.getNom());
                return dto;
            })
            .collect(Collectors.toList());
    }

    /**
     * Query: chambre
     * Obtenir une chambre par son ID
     * Nouvelle fonctionnalité GraphQL
     */
    @QueryMapping
    public ChambreDTO chambre(@Argument String id) {
        try {
            Long chambreId = Long.parseLong(id);
            Optional<Chambre> chambreOpt = hotelService.getChambreById(chambreId);

            if (chambreOpt.isEmpty()) {
                return null;
            }

            Chambre chambre = chambreOpt.get();
            Hotel hotel = hotelService.getHotel();

            ChambreDTO dto = new ChambreDTO(
                chambre.getId(),
                chambre.getNom(),
                chambre.getPrix(),
                chambre.getNbrDeLit(),
                hotel.getType().ordinal() + 1,
                true,
                chambre.getImageUrl()
            );
            dto.setHotelNom(hotel.getNom());
            return dto;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Query: reservations
     * Lister toutes les réservations de l'hôtel
     * Remplace: GET /api/hotel/reservations
     */
    @QueryMapping
    public List<Reservation> reservations() {
        return hotelService.getReservations();
    }
}

