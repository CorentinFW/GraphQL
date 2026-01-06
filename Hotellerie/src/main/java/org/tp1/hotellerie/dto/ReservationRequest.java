package org.tp1.hotellerie.dto;

/**
 * DTO pour les informations de réservation d'une chambre (format REST/JSON)
 */
public class ReservationRequest {

    private Long chambreId;
    private String dateArrive;
    private String dateDepart;
    private String nomClient;
    private String prenomClient;
    private String numeroCarteBancaire;
    private String agenceId;  // Identifiant de l'agence qui effectue la réservation
    private Float prixAvecCoefficient;  // Prix par nuit avec coefficient de l'agence appliqué

    // Constructeurs
    public ReservationRequest() {
    }

    public ReservationRequest(Long chambreId, String dateArrive, String dateDepart, String nomClient, String prenomClient, String numeroCarteBancaire) {
        this.chambreId = chambreId;
        this.dateArrive = dateArrive;
        this.dateDepart = dateDepart;
        this.nomClient = nomClient;
        this.prenomClient = prenomClient;
        this.numeroCarteBancaire = numeroCarteBancaire;
    }

    // Getters et Setters
    public Long getChambreId() {
        return chambreId;
    }

    public void setChambreId(Long chambreId) {
        this.chambreId = chambreId;
    }

    public String getDateArrive() {
        return dateArrive;
    }

    public void setDateArrive(String dateArrive) {
        this.dateArrive = dateArrive;
    }

    public String getDateDepart() {
        return dateDepart;
    }

    public void setDateDepart(String dateDepart) {
        this.dateDepart = dateDepart;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getPrenomClient() {
        return prenomClient;
    }

    public void setPrenomClient(String prenomClient) {
        this.prenomClient = prenomClient;
    }

    public String getNumeroCarteBancaire() {
        return numeroCarteBancaire;
    }

    public void setNumeroCarteBancaire(String numeroCarteBancaire) {
        this.numeroCarteBancaire = numeroCarteBancaire;
    }

    public String getAgenceId() {
        return agenceId;
    }

    public void setAgenceId(String agenceId) {
        this.agenceId = agenceId;
    }

    public Float getPrixAvecCoefficient() {
        return prixAvecCoefficient;
    }

    public void setPrixAvecCoefficient(Float prixAvecCoefficient) {
        this.prixAvecCoefficient = prixAvecCoefficient;
    }

    @Override
    public String toString() {
        return "ReservationRequest{" +
                "chambreId=" + chambreId +
                ", dateArrive='" + dateArrive + '\'' +
                ", dateDepart='" + dateDepart + '\'' +
                ", nomClient='" + nomClient + '\'' +
                ", prenomClient='" + prenomClient + '\'' +
                ", numeroCarteBancaire='****'" + // Masquer le numéro de carte
                '}';
    }
}

