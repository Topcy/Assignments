package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

public class BookingDetailsPage extends BasePage {

    private final Locator main = page.locator("main");

    private final Locator breadcrumb = main.getByRole(AriaRole.NAVIGATION);

    private final Locator title =
            main.getByRole(AriaRole.HEADING, new Locator.GetByRoleOptions().setLevel(1));

    private final Locator headerReference =
            main.getByText(Pattern.compile("^confirmed$", Pattern.CASE_INSENSITIVE))
                    .locator("xpath=preceding-sibling::span");

    private final Locator backToMyBookings =
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Back to My Bookings"));

    public BookingDetailsPage(Page page) {
        super(page);
    }

    public Locator breadcrumb() {
        return breadcrumb;
    }

    public Locator headerReference() {
        return headerReference;
    }

    public Locator title() {
        return title;
    }

    public Locator customerEmail() {
        return valueOf("Email");
    }

    public Locator paymentTickets() {
        return valueOf("Tickets");
    }

    public Locator paymentTotal() {
        return valueOf("Total Paid");
    }

    public Locator bookingId() {
        return valueOf("Booking ID");
    }

    public Locator mentionsOf(String reference) {
        return main.getByText(reference);
    }

    public void backToMyBookings() {
        backToMyBookings.click();
    }

    private Locator valueOf(String label) {
        return main.getByText(label, new Locator.GetByTextOptions().setExact(true))
                .locator("xpath=following-sibling::span");
    }
}
