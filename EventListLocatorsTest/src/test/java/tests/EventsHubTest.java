package tests;

import com.microsoft.playwright.Locator;
import config.PlaywrightConfig;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.DashboardPage;
import pages.DetailsPage;
import pages.EventsPage;
import pages.LoginPage;

import static org.testng.Assert.*;

public class EventsHubTest extends BaseTest {
    LoginPage loginPage;

    @DataProvider(name = "browserProjects")
    public Object[][] browserProjects() {
        return PlaywrightConfig.BROWSER_PROJECTS
                .stream()
                .map(browser -> new Object[]{browser})
                .toArray(Object[][]::new);

    }

    @Test(description = "verify that the login page loads correctly with the configured base URL",
            dataProvider = "browserProjects"
    )
    public void openLoginTest(String browserName) {
        setUp(browserName);
        loginPage = new LoginPage(page);
        loginPage.openLoginPage();
        loginPage.waitForEventsToLoad();

        // Sign in and open the Events page
        DashboardPage dashboardPage = loginPage.loginToDashboardPage("test123@gmail.com", "Testing1#");
        assertEquals(page.title(), "EventHub — Discover & Book Events");
        EventsPage eventsPage = dashboardPage.openEventsPage();

        //Confirm Upcoming Events heading is visible
        assertTrue(eventsPage.isEventsPageHeadingVisible());

        //filtering and obtaining at least one card match
        Locator eventCard = eventsPage.findEventCard("World", "Conference", "Hyderabad", "World Tech Summit");
        int count = eventCard.count();
        assertEquals(count, 1, "There is exactly one match after filtering");

        // capturing event title, price text and seats text
        String title = eventCard.getByText("World").innerText();
        String price = eventCard.getByText("$").innerText();
        String seat = eventCard.getByText("seats").innerText();
        int seatCount = Integer.parseInt(seat.split(" ")[0]);

        assertTrue(title.contains("World Tech Summit"));
        assertTrue(price.contains("$"));
        assertTrue(seatCount > 0);

        //open Book Now and confirm details page url, title and price
        DetailsPage detailsPage = eventsPage.openBookNow(eventCard);
        assertTrue(detailsPage.returnUrl().contains("/events"));
        assertEquals(title, detailsPage.compareTitle());
        assertEquals(price, detailsPage.comparePrice());

        //return to events list, clear filter and confirm returned cards
        EventsPage returnToEventsPage = detailsPage.returnToEventsList();
        Locator events = returnToEventsPage.clearFilter();

        assertTrue(events.count() >= 3);
        String firstEvent = events.first().locator("h3").innerText();
        String secondEvent = events.nth(1).locator("h3").innerText();
        String lastEvent = events.last().locator("h3").innerText();

        assertNotNull(firstEvent);
        assertNotNull(secondEvent);
        assertNotNull(lastEvent);
        assertNotEquals(firstEvent,lastEvent);
    }
}