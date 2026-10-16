package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

public class BookingDetailPage {

    private final Page page;

    public BookingDetailPage(Page page) {
        this.page = page;
    }

    private Locator main() {
        return page.getByRole(AriaRole.MAIN);
    }

    public Locator breadcrumb() {
        return main().getByRole(AriaRole.NAVIGATION);
    }

    public Locator breadcrumbReference(String reference) {
        return breadcrumb().getByText(reference, new Locator.GetByTextOptions().setExact(true));
    }

    public Locator title() {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(1));
    }

    public Locator paymentSummaryHeading() {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Payment Summary"));
    }

    public Locator paymentTickets()   { return valueNextToLabel("Tickets"); }

    public Locator paymentTotalPaid() { return valueNextToLabel("Total Paid"); }

    public Locator customerEmail()    { return valueNextToLabel("Email"); }

        private Locator valueNextToLabel(String label) {
        return main()
                .getByText(label, new Locator.GetByTextOptions().setExact(true))
                .locator("xpath=following-sibling::*[1]");
    }

    public Locator backToMyBookingsButton() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Back to My Bookings")));
    }

    public void backToMyBookings() {
        backToMyBookingsButton().click();
    }
}
