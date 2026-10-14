package pages;

import com.microsoft.playwright.Page;


public class HomePage {

    private final Page page;
    private final HeaderComponent header;

    public HomePage(Page page) {
        this.page = page;
        this.header = new HeaderComponent(page);
    }

    public HeaderComponent header() {
        return header;
    }

    public EventsPage openEvents() {
        header.eventsLink().click();
        return new EventsPage(page);
    }
}
