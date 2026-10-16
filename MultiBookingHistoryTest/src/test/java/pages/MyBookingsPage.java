package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;


public class MyBookingsPage extends BasePage {

    private final Locator heading = page.getByRole(AriaRole.HEADING,
            new Page.GetByRoleOptions().setName("My Bookings").setLevel(1));
    private final Locator bookingCards = page.getByTestId("booking-card");

    public MyBookingsPage(Page page) {
        super(page);
    }

    public MyBookingsPage open() {
        goToMyBookingsViaNav();
        assertThat(heading).isVisible();
        return this;
    }

    public BookingCard cardForReference(String reference) {
        return new BookingCard(bookingCards.filter(new Locator.FilterOptions().setHasText(reference)));
    }
}
