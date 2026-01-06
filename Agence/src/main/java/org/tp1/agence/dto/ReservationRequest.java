package org.tp1.agence.dto;

public class ReservationRequest {
    private String clientNom;
    private String clientPrenom;
    private String clientEmail;
    private String clientTelephone;
    private String clientNumeroCarteBleue;
    private Long chambreId;
    private String hotelAdresse;
    private String dateArrive;
    private String dateDepart;
    private String agenceId;  // Identifiant de l'agence qui effectue la réservation
    private Float prixAvecCoefficient;  // Prix par nuit avec coefficient de l'agence

    public ReservationRequest() {
    }

    // Getters pour compatibilité avec l'ancien code
    public String getClientNom() {
        return clientNom;
    }

    // Alias pour GraphQL
    public String getNomClient() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    // Setter pour GraphQL (nomClient -> clientNom)
    public void setNomClient(String nomClient) {
        this.clientNom = nomClient;
    }

    public String getClientPrenom() {
        return clientPrenom;
    }

    // Alias pour GraphQL
    public String getPrenomClient() {
        return clientPrenom;
    }

    public void setClientPrenom(String clientPrenom) {
        this.clientPrenom = clientPrenom;
    }

    // Setter pour GraphQL (prenomClient -> clientPrenom)
    public void setPrenomClient(String prenomClient) {
        this.clientPrenom = prenomClient;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    // Alias pour GraphQL
    public String getEmailClient() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    // Setter pour GraphQL (emailClient -> clientEmail)
    public void setEmailClient(String emailClient) {
        this.clientEmail = emailClient;
    }

    public String getClientTelephone() {
        return clientTelephone;
    }

    // Alias pour GraphQL
    public String getTelephoneClient() {
        return clientTelephone;
    }

    public void setClientTelephone(String clientTelephone) {
        this.clientTelephone = clientTelephone;
    }

    // Setter pour GraphQL (telephoneClient -> clientTelephone)
    public void setTelephoneClient(String telephoneClient) {
        this.clientTelephone = telephoneClient;
    }

    public String getClientNumeroCarteBleue() {
        return clientNumeroCarteBleue;
    }

    public void setClientNumeroCarteBleue(String clientNumeroCarteBleue) {
        this.clientNumeroCarteBleue = clientNumeroCarteBleue;
    }

    // Setter pour GraphQL (numeroCarteBancaire -> clientNumeroCarteBleue)
    public void setNumeroCarteBancaire(String numeroCarteBancaire) {
        this.clientNumeroCarteBleue = numeroCarteBancaire;
    }

    public Long getChambreId() {
        return chambreId;
    }

    public void setChambreId(Long chambreId) {
        this.chambreId = chambreId;
    }

    public String getHotelAdresse() {
        return hotelAdresse;
    }

    public void setHotelAdresse(String hotelAdresse) {
        this.hotelAdresse = hotelAdresse;
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
}

