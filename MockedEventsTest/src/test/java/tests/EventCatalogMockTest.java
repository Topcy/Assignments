package tests;

import config.TestConfig;
import resources.EventApiMock;
import resources.MockEvent;
import resources.MockEventData;
import pages.EventDetailPage;
import pages.EventsPage;
import pages.HomePage;
import pages.LoginPage;
import com.microsoft.playwright.Locator;
import org.testng.annotations.Test;

import static utils.Match.urlOf;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class EventCatalogMockTest extends BaseTest {

    @Test
    public void mockedCatalogFlow() {

        //Log in
        HomePage homePage = new LoginPage(page)
                .open()
                .loginAs(TestConfig.email(), TestConfig.password());

        assertThat(page).hasURL(urlOf("/"));
        assertThat(homePage.header().userEmail()).hasText(TestConfig.email());

        //Install the mock BEFORE opening Events
        MockEventData dataset = MockEventData.defaults();
        EventApiMock api = new EventApiMock(dataset);
        api.install(context);

        //Open Events
        EventsPage eventsPage = homePage.openEvents();
        assertThat(eventsPage.heading()).isVisible();

        //Only mocked data is shown
        assertThat(eventsPage.eventCards()).hasCount(4);
        assertThat(eventsPage.cardTitles()).hasText(dataset.titles().toArray(new String[0]));
        assertThat(eventsPage.anywhereWithText(MockEventData.TITLE)).hasCount(0);

        for (MockEvent event : dataset.all()) {
            Locator card = eventsPage.cardFor(event);
            assertThat(card).isVisible();
            assertThat(eventsPage.priceOn(card, event)).isVisible();
            assertThat(eventsPage.seatsOn(card, event)).isVisible();
            assertThat(eventsPage.titleLink(card)).hasAttribute("href", event.detailPath());
            assertThat(eventsPage.bookNowLink(card)).hasAttribute("href", event.detailPath());
        }

        //Filter down to one card
        eventsPage.filterBy(MockEventData.KEYWORD, "Conference", "Hyderabad");
        assertThat(eventsPage.eventCards()).hasCount(1);

        // Store the mock record the UI is showing.
        MockEvent selected = dataset.byTitle(eventsPage.cardTitles().innerText());

        assertEquals(selected.id(), 9001, "Filter should leave only the Hyderabad conference");
        assertThat(eventsPage.cardFor(selected)).isVisible();

        // The catalog request that produced this list carried all three filters.
        assertTrue(api.catalogRequestUrls().stream().anyMatch(url ->
                        url.contains("search=") && url.contains("category=Conference") && url.contains("city=Hyderabad")),
                "Expected a mocked catalog request with search, category and city. Got: " + api.catalogRequestUrls());

        //Book Now -> detail page matches the stored record
        EventDetailPage detailPage = eventsPage.openBookNow(eventsPage.cardFor(selected));

        assertThat(page).hasURL(urlOf(selected.detailPath()));
        assertThat(detailPage.title()).hasText(selected.title());
        assertThat(detailPage.pricePerTicket()).hasText(selected.formattedPrice());
        assertThat(detailPage.venue()).hasText(selected.venue());
        assertThat(detailPage.city()).hasText(selected.city());
        assertThat(detailPage.availableSeats()).hasText(selected.detailSeatsText());

        // The detail data also came from the mock, not from the live server.
        assertTrue(api.detailRequestUrls().stream().anyMatch(url -> url.endsWith("/api/events/" + selected.id())),
                "Expected a mocked detail request for id " + selected.id() + ". Got: " + api.detailRequestUrls());

        //Ticket quantity and total
        assertThat(detailPage.quantity()).hasText("1");
        assertThat(detailPage.total()).hasText(selected.formattedTotalFor(1));

        detailPage.increaseQuantity();

        assertThat(detailPage.quantity()).hasText("2");
        assertThat(detailPage.total()).hasText(selected.formattedTotalFor(2));
        assertThat(detailPage.priceBreakdown()).hasText(selected.formattedPrice() + " × 2 tickets");
    }
}
