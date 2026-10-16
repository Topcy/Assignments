package models;

public record BookingPayload(String reference,
                             String eventTitle,
                             int tickets,
                             String total,
                             String customerEmail) {


    public String ticketText() {
        return tickets + (tickets == 1 ? " ticket" : " tickets");
    }
}
