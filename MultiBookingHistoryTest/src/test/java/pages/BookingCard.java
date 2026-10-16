package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

public class BookingCard {

    private final Locator root;

    BookingCard(Locator root) {
        this.root = root;
    }

    /** The card itself (it may match 0 or 1 elements */
    public Locator root() {
        return root;
    }

    public Locator status() {
        return root.getByText(Pattern.compile("^confirmed$", Pattern.CASE_INSENSITIVE));
    }

    public Locator title() {
        return root.getByRole(AriaRole.HEADING);
    }

    public Locator ticketCount() {
        return root.getByText(Pattern.compile("\\d+ tickets?$"));
    }

    public Locator total() {
        return root.getByText(Pattern.compile("^\\$[\\d,]+(\\.\\d+)?$"));
    }

    public Locator bookingId() {
        return root.getByTestId("booking-id");
    }

    public Locator viewDetailsLink() {
        return root.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("View Details"));
    }

    public void openDetails() {
        viewDetailsLink().click();
    }
}
