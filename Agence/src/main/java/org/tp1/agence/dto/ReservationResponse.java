package org.tp1.agence.dto;

public class ReservationResponse {
    private Long reservationId;
    private String message;
    private boolean success;

    public ReservationResponse() {
    }

    public ReservationResponse(Long reservationId, String message, boolean success) {
        this.reservationId = reservationId;
        this.message = message;
        this.success = success;
    }

    // Méthode factory pour créer une réponse de succès
    public static ReservationResponse success(Long reservationId, String message) {
        return new ReservationResponse(reservationId, message, true);
    }

    // Méthode factory pour créer une réponse d'erreur
    public static ReservationResponse error(String message) {
        return new ReservationResponse(null, message, false);
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}

