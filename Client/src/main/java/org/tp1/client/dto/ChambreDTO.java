package org.tp1.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO pour les informations d'une chambre (côté client)
 */
public class ChambreDTO {
    private Long id;
    private String nom;
    private float prix;
    private float prixOriginal;
    private float coefficient;
    private int nbrLits;
    private int nbrEtoiles;
    private boolean disponible;
    private String hotelNom;
    private String hotelAdresse;

    @JsonProperty("image")
    private String imageUrl;

    private String agenceNom;

    public ChambreDTO() {
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public float getPrix() {
        return prix;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }

    public float getPrixOriginal() {
        return prixOriginal;
    }

    public void setPrixOriginal(float prixOriginal) {
        this.prixOriginal = prixOriginal;
    }

    public float getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(float coefficient) {
        this.coefficient = coefficient;
    }

    public int getNbrLits() {
        return nbrLits;
    }

    public void setNbrLits(int nbrLits) {
        this.nbrLits = nbrLits;
    }

    public int getNbrEtoiles() {
        return nbrEtoiles;
    }

    public void setNbrEtoiles(int nbrEtoiles) {
        this.nbrEtoiles = nbrEtoiles;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public String getHotelNom() {
        return hotelNom;
    }

    public void setHotelNom(String hotelNom) {
        this.hotelNom = hotelNom;
    }

    public String getHotelAdresse() {
        return hotelAdresse;
    }

    public void setHotelAdresse(String hotelAdresse) {
        this.hotelAdresse = hotelAdresse;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAgenceNom() {
        return agenceNom;
    }

    public void setAgenceNom(String agenceNom) {
        this.agenceNom = agenceNom;
    }
}

