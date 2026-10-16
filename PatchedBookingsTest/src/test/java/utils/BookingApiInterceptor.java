package utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Network interception helper ("API mocking by patching").
 *
 * What it does:
 *   1. LIST   GET  /api/bookings?page=1&limit=10   -> lets the REAL request go to the server, takes the
 *      real JSON, changes exactly ONE booking, and hands the modified JSON to the browser.
 *   2. DETAIL GET  /api/bookings/{id}              -> same idea for the patched booking's detail call,
 *      so the detail page agrees with the list card, while untouched fields (e.g. customerEmail)
 *      stay LIVE.
 *
 * How Playwright interception works (read this once, it is the core idea):
 *   page.route(urlPattern, handler)  registers a handler. When the page makes a matching request,
 *   the request is PAUSED and the handler gets a Route. The handler must do exactly one of:
 *     route.fulfill(...)  - answer the request itself
 *     route.resume()      - let it through unchanged
 *     route.fallback()    - pass to the next matching handler
 *   route.fetch() performs the real request on the handler's behalf, so we can read the live
 *   response, modify it, and then fulfill() the browser with our modified version.
 *
 * Routes stay registered for the whole page lifetime. When the user comes "back" to My Bookings the
 * app sometimes RE-FETCHES the list and sometimes serves it from a client-side cache (both were
 * observed live), so the list handler may or may not run a second time. The handler is therefore
 * IDEMPOTENT: after the first run it finds the booking by its preserved id instead of choosing
 * an index again, so the result is the same either way.
 */
public class BookingApiInterceptor {

    // ---- Deterministic patched values (easy to assert in the UI) -------------------------------
    public static final String PATCHED_REFERENCE = "QA-PATCH-0001";
    public static final String PATCHED_TITLE     = "QA Patched Event Title";
    public static final int    PATCHED_TICKETS   = 5;
    public static final String PATCHED_TOTAL_RAW = "7500";          // UI shows "$7,500"

    // List endpoint, with or without a query string. The "$" anchor means it does NOT match
    // /api/bookings/123 (the detail endpoint).
    private static final Pattern LIST_URL = Pattern.compile(".*/api/bookings(\\?.*)?$");

    private final Page page;

    // ---- State recorded for the test's assertions ----------------------------------------------
    private boolean listIntercepted;
    private int listHitCount;
    private int detailHitCount;
    private int liveBookingCount;
    private int changedRecordCount;
    private BookingSnapshot original;     // the booking BEFORE patching (live values)
    private BookingSnapshot patched;      // the booking AFTER patching
    private List<String> liveReferences = List.of();   // every live reference, before patching
    private String liveDetailEmail;       // e-mail returned by the REAL detail API

    public BookingApiInterceptor(Page page) {
        this.page = page;
    }

    // =============================================================================================
    // LIST
    // =============================================================================================

    /** Register the list interception. Call this BEFORE opening My Bookings. */
    public void patchBookingsList() {
        page.route(LIST_URL, route -> {
            // POST /api/bookings (creating a booking) would also match; we only care about GET.
            if (!"GET".equals(route.request().method())) {
                route.fallback();
                return;
            }

            APIResponse live = route.fetch();                       // 1) real server response
            JsonObject root = JsonParser.parseString(live.text()).getAsJsonObject();
            JsonArray data = root.getAsJsonArray("data");
            listHitCount++;

            if (data.isEmpty()) {                                    // nothing to patch; the test's
                listIntercepted = true;                              // precondition assertion reports it
                fulfillJson(route, live, root);
                return;
            }

            // Snapshot every record as text BEFORE we touch anything (used to count changes later).
            List<String> before = new ArrayList<>();
            data.forEach(e -> before.add(e.toString()));

            // 2) choose the record to patch
            int index;
            if (original == null) {
                // First call: pick a record from the MIDDLE of the list. Choosing neither first nor
                // last means a test that accidentally relied on position would fail loudly.
                index = data.size() / 2;
                liveBookingCount = data.size();
                liveReferences = refsOf(data);
                original = snapshotOf(data.get(index).getAsJsonObject());
            } else {
                // Later calls (e.g. user came "back"): find the SAME booking by its preserved id.
                index = indexOfId(data, original.id());
            }

            if (index >= 0) {
                applyPatch(data.get(index).getAsJsonObject());
                patched = snapshotOf(data.get(index).getAsJsonObject());
            }

            // 3) count how many records actually differ from the live ones (must be exactly 1)
            int changed = 0;
            for (int i = 0; i < data.size(); i++) {
                if (!data.get(i).toString().equals(before.get(i))) {
                    changed++;
                }
            }
            changedRecordCount = changed;
            listIntercepted = true;

            fulfillJson(route, live, root);                          // 4) answer the browser
        });
    }

    // =============================================================================================
    // DETAIL
    // =============================================================================================

    /**
     * Register the detail interception for ONE booking id. The id comes from the patched list
     * record, which proves the app navigated with the preserved id.
     */
    public void patchBookingDetail(int bookingId) {
        // e.g. .*/api/bookings/26534(\?.*)?$
        Pattern detailUrl = Pattern.compile(".*/api/bookings/" + bookingId + "(\\?.*)?$");

        page.route(detailUrl, route -> {
            if (!"GET".equals(route.request().method())) {
                route.fallback();
                return;
            }

            APIResponse live = route.fetch();
            JsonObject root = JsonParser.parseString(live.text()).getAsJsonObject();
            JsonObject data = root.getAsJsonObject("data");
            detailHitCount++;

            // Remember the REAL e-mail so the test can prove it was not touched.
            liveDetailEmail = data.get("customerEmail").getAsString();

            // Align the detail fields with the patched LIST values (not hard-coded a second time).
            applyPatch(data);

            fulfillJson(route, live, root);
        });
    }

    // =============================================================================================
    // Patch + helpers
    // =============================================================================================

    /** Replaces exactly the four fields we want to change. id and every other field stay as-is. */
    private static void applyPatch(JsonObject booking) {
        booking.addProperty("bookingRef", PATCHED_REFERENCE);
        booking.getAsJsonObject("event").addProperty("title", PATCHED_TITLE);
        booking.addProperty("quantity", PATCHED_TICKETS);
        booking.addProperty("totalPrice", PATCHED_TOTAL_RAW);   // the API uses a string, keep that type
    }

    private static BookingSnapshot snapshotOf(JsonObject b) {
        return new BookingSnapshot(
                b.get("id").getAsInt(),
                b.get("bookingRef").getAsString(),
                b.getAsJsonObject("event").get("title").getAsString(),
                b.get("quantity").getAsInt(),
                b.get("totalPrice").getAsString(),
                b.get("customerEmail").getAsString());
    }

    private static List<String> refsOf(JsonArray data) {
        List<String> refs = new ArrayList<>();
        for (JsonElement e : data) {
            refs.add(e.getAsJsonObject().get("bookingRef").getAsString());
        }
        return List.copyOf(refs);
    }

    private static int indexOfId(JsonArray data, int id) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getAsJsonObject().get("id").getAsInt() == id) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sends the modified JSON to the browser, keeping the live status code and headers
     * (important: the CORS headers, otherwise the browser would reject the cross-origin response).
     * We drop headers that describe the OLD body (length / compression) because the body changed.
     */
    private static void fulfillJson(Route route, APIResponse live, JsonObject body) {
        Map<String, String> headers = new HashMap<>(live.headers());
        headers.keySet().removeIf(name ->
                name.equalsIgnoreCase("content-length")
                        || name.equalsIgnoreCase("content-encoding")
                        || name.equalsIgnoreCase("transfer-encoding"));
        headers.put("content-type", "application/json; charset=utf-8");

        route.fulfill(new Route.FulfillOptions()
                .setStatus(live.status())
                .setHeaders(headers)
                .setBody(body.toString()));
    }

    // =============================================================================================
    // Read-only accessors used by the test's assertions
    // =============================================================================================

    public boolean listWasIntercepted()        { return listIntercepted; }
    public int listHitCount()                  { return listHitCount; }
    public int detailHitCount()                { return detailHitCount; }
    public int liveBookingCount()              { return liveBookingCount; }
    public int changedRecordCount()            { return changedRecordCount; }
    public BookingSnapshot originalBooking()   { return original; }
    public BookingSnapshot patchedBooking()    { return patched; }
    public List<String> liveReferences()       { return liveReferences; }
    public String liveDetailEmail()            { return liveDetailEmail; }
}
