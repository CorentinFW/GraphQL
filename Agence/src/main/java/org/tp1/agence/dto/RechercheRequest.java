package org.tp1.agence.dto;

public class RechercheRequest {
    private String adresse;
    private String dateArrive;
    private String dateDepart;
    private Float prixMax;
    private Float prixMin;
    private Integer nbrEtoile;
    private Integer nbrLits;

    // Constructeur par défaut
    public RechercheRequest() {
    }

    // Getters et Setters
    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
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

    public Float getPrixMax() {
        return prixMax;
    }

    public void setPrixMax(Float prixMax) {
        this.prixMax = prixMax;
    }

    public Float getPrixMin() {
        return prixMin;
    }

    public void setPrixMin(Float prixMin) {
        this.prixMin = prixMin;
    }

    public Integer getNbrEtoile() {
        return nbrEtoile;
    }

    public void setNbrEtoile(Integer nbrEtoile) {
        this.nbrEtoile = nbrEtoile;
    }

    public Integer getNbrLits() {
        return nbrLits;
    }

    public void setNbrLits(Integer nbrLits) {
        this.nbrLits = nbrLits;
    }
}

