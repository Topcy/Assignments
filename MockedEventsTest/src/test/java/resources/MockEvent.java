package resources;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 *
 * The record components mirror the JSON the real API returns (observed on the live site):
 * id, title, description, category, venue, city, eventDate, price (a STRING, e.g. "1800"),
 * totalSeats, availableSeats, imageUrl, isStatic, userId.
 * Gson turns this record straight into that JSON shape.
 */
public record MockEvent(
        int id,
        String title,
        String description,
        String category,
        String venue,
        String city,
        String eventDate,
        String price,
        int totalSeats,
        int availableSeats,
        String imageUrl,
        boolean isStatic,
        Integer userId) {

    /** Observed on the live site: the card says "N seats left!" for 10 or fewer, else "N seats available". */
    private static final int LOW_SEATS_THRESHOLD = 10;

    /** Path used by both the title link and the Book Now link, e.g. "/events/9001". */
    public String detailPath() {
        return "/events/" + id;
    }

    /** Price for one ticket as the UI formats it, e.g. "$1,800". */
    public String formattedPrice() {
        return formatUsd(new BigDecimal(price));
    }

    /** Total for N tickets as the UI formats it, e.g. "$3,600" for N = 2. */
    public String formattedTotalFor(int quantity) {
        return formatUsd(new BigDecimal(price).multiply(BigDecimal.valueOf(quantity)));
    }

    /** Seat text on the catalog card, e.g. "120 seats available". */
    public String cardSeatsText() {
        return availableSeats <= LOW_SEATS_THRESHOLD
                ? availableSeats + " seats left!"
                : availableSeats + " seats available";
    }

    /** Seat text on the detail page, e.g. "120 / 500 seats". */
    public String detailSeatsText() {
        return availableSeats + " / " + totalSeats + " seats";
    }

    private static String formatUsd(BigDecimal amount) {
        // NumberFormat is not thread-safe, so we create a fresh one each call.
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        return format.format(amount);
    }
}
