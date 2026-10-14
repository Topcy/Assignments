package resources;

import java.util.List;

//The single source of truth for mocked data.

public final class MockEventData {

    public static final String KEYWORD = "Hyderabad conference";

    public static final String TITLE = "World Tech Summit";

    private final List<MockEvent> events;

    private MockEventData(List<MockEvent> events) {
        this.events = events;
    }

    public static MockEventData defaults() {
        return new MockEventData(List.of(
                new MockEvent(9001, "Hyderabad Developer Conference 2026",
                        "Two-day developer conference covering cloud-native, AI and test automation.",
                        "Conference", "HITEX Exhibition Centre", "Hyderabad",
                        "2026-11-20T09:00:00.000Z", "1800", 500, 120, null, false, null),

                new MockEvent(9002, "Delhi Winter Lights Festival",
                        "Open-air winter festival with food stalls and live folk performances.",
                        "Festival", "Jawaharlal Nehru Stadium", "Delhi",
                        "2026-12-05T13:00:00.000Z", "750", 5000, 3400, null, false, null),

                new MockEvent(9003, "Mumbai Sunset Live Concert",
                        "An evening of live music by the Arabian Sea.",
                        "Concert", "Mahalaxmi Racecourse", "Mumbai",
                        "2026-11-28T13:30:00.000Z", "2750", 2000, 860, null, false, null),

                new MockEvent(9004, "Bangalore Test Automation Workshop",
                        "Hands-on Playwright and API testing workshop.",
                        "Workshop", "Koramangala Tech Hub", "Bangalore",
                        "2026-12-12T04:30:00.000Z", "499", 60, 35, null, false, null)));
    }

    public List<MockEvent> all() {
        return events;
    }

    public List<String> titles() {
        return events.stream().map(MockEvent::title).toList();
    }

    public MockEvent byTitle(String title) {
        return events.stream()
                .filter(event -> event.title().equals(title))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No mock event with title: " + title));
    }
}
