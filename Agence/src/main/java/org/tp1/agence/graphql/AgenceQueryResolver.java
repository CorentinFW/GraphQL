package org.tp1.agence.graphql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.tp1.agence.dto.ChambreDTO;
import org.tp1.agence.dto.RechercheRequest;
import org.tp1.agence.service.AgenceService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolver GraphQL pour les queries de l'agence
 * Remplace les endpoints GET du AgenceController REST
 */
@Controller
public class AgenceQueryResolver {

    @Autowired
    private AgenceService agenceService;

    /**
     * Query: ping
     * Vérifier que l'agence est opérationnelle
     */
    @QueryMapping
    public Map<String, String> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Agence GraphQL opérationnelle");
        response.put("status", "OK");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return response;
    }

    /**
     * Query: rechercherChambres
     * Rechercher des chambres dans tous les hôtels partenaires
     */
    @QueryMapping
    public List<ChambreDTO> rechercherChambres(@Argument RechercheRequest criteres) {
        System.out.println("🔍 GraphQL Query: Recherche de chambres avec critères: " + criteres);

        List<ChambreDTO> chambres = agenceService.rechercherChambres(criteres);

        System.out.println("✅ " + chambres.size() + " chambres trouvées via GraphQL");

        return chambres;
    }

    /**
     * Query: reservationsHotel
     * Obtenir les réservations d'un hôtel spécifique
     */
    @QueryMapping
    public List<Map<String, Object>> reservationsHotel(@Argument String hotelNom) {
        System.out.println("📋 GraphQL Query: Réservations pour l'hôtel: " + hotelNom);

        // Cette fonctionnalité nécessiterait d'interroger l'hôtel
        // Pour l'instant, retourner une liste vide
        return List.of();
    }

    /**
     * Query: toutesReservations
     * Obtenir toutes les réservations de tous les hôtels
     */
    @QueryMapping
    public List<Map<String, Object>> toutesReservations() {
        System.out.println("📋 GraphQL Query: Toutes les réservations");

        // Cette fonctionnalité nécessiterait d'interroger tous les hôtels
        // Pour l'instant, retourner une liste vide
        return List.of();
    }
}

