package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;


public class EventDetailPage {

    private static final String MULTIPLY_SIGN = "×";

    private final Page page;

    public EventDetailPage(Page page) {
        this.page = page;
    }

    public Locator title() {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(1));
    }

    public Locator venue() {
        return valueOf("Venue");
    }

    public Locator city() {
        return valueOf("City");
    }

    public Locator availableSeats() {
        return valueOf("Available");
    }

    public Locator pricePerTicket() {
        return valueOf("Price per ticket");
    }

    private Locator valueOf(String label) {
        return page.locator("main")
                .getByText(label, new Locator.GetByTextOptions().setExact(true))
                .locator("xpath=following-sibling::p");
    }

    public Locator quantity() {
        return page.locator("#ticket-count");
    }

    private Locator increaseButton() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("+").setExact(true));
    }

    public Locator total() {
        return page.getByText("Total", new Page.GetByTextOptions().setExact(true))
                .locator("xpath=following-sibling::span");
    }

    public Locator priceBreakdown() {
        return page.getByText(Pattern.compile(MULTIPLY_SIGN + "\\s*\\d+ tickets?"));
    }

    public EventDetailPage increaseQuantity() {
        increaseButton().click();
        return this;
    }
}
