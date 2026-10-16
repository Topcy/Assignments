package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class BookingConfirmationPanel {

    private final Locator heading;
    private final Locator panel;

    public BookingConfirmationPanel(Page page) {
        this.heading = page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(Pattern.compile("Booking Confirmed")));

        this.panel = heading.locator("xpath=..");
    }

    public void waitUntilVisible() {
        assertThat(heading).isVisible();
    }

    public String reference() {
        Locator ref = panel.locator(".booking-ref");
        assertThat(ref).hasText(Pattern.compile("\\S+"));
        return ref.innerText().trim();
    }

    public int tickets() {
        return Integer.parseInt(valueOf("Tickets").innerText().trim());
    }

    public String total() {
        return valueOf("Total").innerText().trim();
    }

    private Locator valueOf(String label) {
        return panel.getByText(label, new Locator.GetByTextOptions().setExact(true))
                .locator("xpath=following-sibling::span");
    }
}
