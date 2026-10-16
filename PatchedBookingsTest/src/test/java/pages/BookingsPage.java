package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;


public class BookingsPage {

    private final Page page;

    public BookingsPage(Page page) {
        this.page = page;
    }

    public Locator heading() {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("My Bookings"));
    }

    public Locator cards() {
        return page.getByTestId("booking-card");
    }

    public BookingCard cardByReference(String reference) {
        Locator matching = cards().filter(new Locator.FilterOptions()
                .setHas(page.getByText(reference, new Page.GetByTextOptions().setExact(true))));
        return new BookingCard(matching);
    }

    public Locator cardsWithout(String text) {
        return cards().filter(new Locator.FilterOptions().setHasNotText(text));
    }

    public Locator exactTextOnPage(String text) {
        return page.getByText(text, new Page.GetByTextOptions().setExact(true));
    }
}
