package resources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Intercepts the two EventHub API calls we care about.

public final class EventApiMock {

    private static final Pattern EVENTS_API = Pattern.compile(".*/api/events(/\\d+)?(\\?.*)?$");
    private static final Pattern DETAIL_PATH = Pattern.compile(".*/api/events/(\\d+)$");

    private static final Map<String, String> CORS_HEADERS = Map.of(
            "Access-Control-Allow-Origin", "*",
            "Access-Control-Allow-Headers", "authorization,content-type",
            "Access-Control-Allow-Methods", "GET,OPTIONS");

    private static final int DEFAULT_LIMIT = 12;

    private final MockEventData data;
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    private final List<String> interceptedUrls = new CopyOnWriteArrayList<>();

    public EventApiMock(MockEventData data) {
        this.data = data;
    }


    public void install(BrowserContext context) {
        context.route(EVENTS_API, this::handle);
    }

    // Every API URL this mock answered (after install). Used to prove traffic really was mocked.
    public List<String> interceptedUrls() {
        return List.copyOf(interceptedUrls);
    }

    public List<String> catalogRequestUrls() {
        return interceptedUrls().stream().filter(url -> !DETAIL_PATH.matcher(pathOf(url)).matches()).toList();
    }

    public List<String> detailRequestUrls() {
        return interceptedUrls().stream().filter(url -> DETAIL_PATH.matcher(pathOf(url)).matches()).toList();
    }

    private void handle(Route route) {
        Request request = route.request();
        switch (request.method()) {
            case "OPTIONS" -> route.fulfill(new Route.FulfillOptions().setStatus(204).setHeaders(CORS_HEADERS));
            case "GET" -> serveGet(route, request.url());

            default -> route.resume();
        }
    }

    private void serveGet(Route route, String url) {
        interceptedUrls.add(url);

        Matcher detail = DETAIL_PATH.matcher(pathOf(url));
        if (detail.matches()) {
            int id = Integer.parseInt(detail.group(1));
            data.all().stream().filter(event -> event.id() == id).findFirst().ifPresentOrElse(
                    event -> respond(route, 200, Map.of("success", true, "data", event)),
                    () -> respond(route, 404, Map.of("success", false, "error", "Event not found")));
            return;
        }

        Map<String, String> params = queryParams(url);
        String search = params.getOrDefault("search", "");
        String category = params.getOrDefault("category", "");
        String city = params.getOrDefault("city", "");

        List<MockEvent> matches = data.all().stream()
                .filter(event -> matchesSearch(event, search))
                .filter(event -> category.isBlank() || event.category().equalsIgnoreCase(category))
                .filter(event -> city.isBlank() || event.city().equalsIgnoreCase(city))
                .toList();

        respond(route, 200, Map.of(
                "success", true,
                "data", matches,
                "pagination", Map.of("total", matches.size(), "page", 1, "limit", DEFAULT_LIMIT, "totalPages", 1)));
    }

    private static boolean matchesSearch(MockEvent event, String search) {
        if (search.isBlank()) {
            return true;
        }
        String haystack = String.join(" ", event.title(), event.description(), event.category(),
                event.venue(), event.city()).toLowerCase();
        return Arrays.stream(search.toLowerCase().trim().split("\\s+")).allMatch(haystack::contains);
    }

    private void respond(Route route, int status, Object body) {
        route.fulfill(new Route.FulfillOptions()
                .setStatus(status)
                .setContentType("application/json")
                .setHeaders(CORS_HEADERS)
                .setBody(gson.toJson(body)));
    }

    private static String pathOf(String url) {
        return URI.create(url).getPath();
    }

    private static Map<String, String> queryParams(String url) {
        Map<String, String> params = new HashMap<>();
        String query = URI.create(url).getRawQuery();
        if (query == null) {
            return params;
        }
        for (String pair : query.split("&")) {
            String[] keyValue = pair.split("=", 2);
            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
            String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }
        return params;
    }
}
