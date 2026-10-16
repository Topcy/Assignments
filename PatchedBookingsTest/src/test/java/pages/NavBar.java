package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class NavBar {

    private final Page page;

    public NavBar(Page page) {
        this.page = page;
    }

    public Locator myBookingsLink() { return page.getByTestId("nav-bookings"); }
    public Locator userEmail()      { return page.getByTestId("user-email-display"); }
    public Locator logoutButton()   { return page.getByTestId("logout-btn"); }

    public void openMyBookings() {
        myBookingsLink().click();
    }
}
