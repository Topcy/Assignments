package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;


public abstract class BasePage {

    protected final Page page;

    private final Locator navEvents;
    private final Locator navBookings;
    private final Locator loggedInUserEmail;

    protected BasePage(Page page) {
        this.page = page;
        this.navEvents = page.getByTestId("nav-events");
        this.navBookings = page.getByTestId("nav-bookings");
        this.loggedInUserEmail = page.getByTestId("user-email-display");
    }

    public void goToEventsViaNav() {
        navEvents.click();
        assertThat(page).hasURL(Pattern.compile(".*/events(\\?.*)?$"));
    }

    public void goToMyBookingsViaNav() {
        navBookings.click();
        assertThat(page).hasURL(Pattern.compile(".*/bookings$"));
    }

    public Locator loggedInUserEmail() {
        return loggedInUserEmail;
    }
}
