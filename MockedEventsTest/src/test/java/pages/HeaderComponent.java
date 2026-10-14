package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;


public class HeaderComponent {

    private final Page page;

    public HeaderComponent(Page page) {
        this.page = page;
    }

    public Locator eventsLink() {
        return page.getByTestId("nav-events");
    }

    public Locator userEmail() {
        return page.getByTestId("user-email-display");
    }

    public Locator logoutButton() {
        return page.getByTestId("logout-btn");
    }
}
