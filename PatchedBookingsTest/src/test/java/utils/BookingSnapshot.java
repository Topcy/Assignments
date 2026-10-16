package utils;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * A small, immutable copy of the booking fields this test cares about.
 *
 * A Java "record" is a compact class: the compiler generates the constructor, getters
 * (id(), reference(), ...), equals/hashCode and toString for us.
 *
 * @param id        booking id. NEVER changed by the patch, the UI uses it for /bookings/{id}
 * @param reference booking reference code, e.g. "H-Y8F93Z" (API field: bookingRef)
 * @param title     event title (API field: event.title)
 * @param tickets   number of tickets (API field: quantity)
 * @param totalRaw  total price exactly as the API sends it - a STRING such as "7500"
 * @param email     customer e-mail (API field: customerEmail)
 */
public record BookingSnapshot(int id, String reference, String title, int tickets, String totalRaw, String email) {

    /** The total as the UI renders it, e.g. "7500" -> "$7,500". */
    public String displayTotal() {
        return "$" + NumberFormat.getIntegerInstance(Locale.US).format(Long.parseLong(totalRaw));
    }
}
