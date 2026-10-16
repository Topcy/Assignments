package pages;

import models.BookingPayload;
import models.CustomerDetails;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class EventDetailsPage extends BasePage {

    private final Locator eventTitle =
            page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(1));

    private final Locator increaseQuantityButton =
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("+").setExact(true));

    private final Locator ticketCount = page.locator("#ticket-count");

    private final Locator fullNameInput = page.getByLabel("Full Name");
    private final Locator emailInput = page.getByLabel("Email");
    private final Locator phoneInput = page.getByLabel("Phone Number");
    private final Locator confirmBookingButton =
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Confirm Booking"));

    private final BookingConfirmationPanel confirmation;

    public EventDetailsPage(Page page) {
        super(page);
        this.confirmation = new BookingConfirmationPanel(page);
    }

    public BookingPayload bookTickets(int quantity, CustomerDetails customer) {
        waitUntilLoaded();
        String title = eventTitle.innerText().trim();

        selectQuantity(quantity);

        fullNameInput.fill(customer.fullName());
        emailInput.fill(customer.email());
        phoneInput.fill(customer.phone());
        String enteredEmail = emailInput.inputValue();

        confirmBookingButton.click();
        confirmation.waitUntilVisible();

        return new BookingPayload(
                confirmation.reference(),
                title,
                confirmation.tickets(),
                confirmation.total(),
                enteredEmail);
    }

    private void waitUntilLoaded() {
        assertThat(eventTitle).hasText(Pattern.compile("\\S+"));
        assertThat(confirmBookingButton).isVisible();
    }

    private void selectQuantity(int quantity) {
        assertThat(ticketCount).hasText("1");
        for (int expected = 2; expected <= quantity; expected++) {
            increaseQuantityButton.click();
            assertThat(ticketCount).hasText(String.valueOf(expected));
        }
    }
}
