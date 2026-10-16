package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;


public class BookingCard {

    private final Locator root;

    public BookingCard(Locator root) {
        this.root = root;
    }

    public Locator root() { return root; }

    public Locator reference(String reference) {
        return root.getByText(reference, new Locator.GetByTextOptions().setExact(true));
    }

    public Locator title() { return root.getByRole(AriaRole.HEADING); }

    public Locator bookingId() { return root.getByTestId("booking-id"); }

    public Locator tickets(int count) {
        return root.getByText(count + (count == 1 ? " ticket" : " tickets"));
    }

    public Locator total(String displayedAmount) {
        return root.getByText(displayedAmount, new Locator.GetByTextOptions().setExact(true));
    }

    public Locator viewDetailsButton() {
        return root.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("View Details"));
    }

    public void openDetails() {
        viewDetailsButton().click();
    }
}
