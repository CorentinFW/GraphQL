package org.tp1.hotellerie.dto;

/**
 * DTO pour exposer les réservations via GraphQL
 * Mappe les champs complexes (Client, Chambre) vers des champs simples
 */
public class ReservationDTO {

    private Long id;
    private Long chambreId;
    private String nomClient;
    private String prenomClient;
    private String emailClient;
    private String telephoneClient;
    private String dateArrive;
    private String dateDepart;
    private Float prixTotal;
    private String agenceId;  // Identifiant de l'agence qui a effectué la réservation

    // Constructeur vide
    public ReservationDTO() {
    }

    // Constructeur complet
    public ReservationDTO(Long id, Long chambreId, String nomClient, String prenomClient,
                         String emailClient, String telephoneClient, String dateArrive,
                         String dateDepart, Float prixTotal) {
        this.id = id;
        this.chambreId = chambreId;
        this.nomClient = nomClient;
        this.prenomClient = prenomClient;
        this.emailClient = emailClient;
        this.telephoneClient = telephoneClient;
        this.dateArrive = dateArrive;
        this.dateDepart = dateDepart;
        this.prixTotal = prixTotal;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChambreId() {
        return chambreId;
    }

    public void setChambreId(Long chambreId) {
        this.chambreId = chambreId;
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

    public String getEmailClient() {
        return emailClient;
    }

    public void setEmailClient(String emailClient) {
        this.emailClient = emailClient;
    }

    public String getTelephoneClient() {
        return telephoneClient;
    }

    public void setTelephoneClient(String telephoneClient) {
        this.telephoneClient = telephoneClient;
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

    public Float getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(Float prixTotal) {
        this.prixTotal = prixTotal;
    }

    public String getAgenceId() {
        return agenceId;
    }

    public void setAgenceId(String agenceId) {
        this.agenceId = agenceId;
    }
}

