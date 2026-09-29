package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class DashboardPage {

    private final Page page;
    private final Locator browseEventsButton;

    public DashboardPage(Page page) {
        this.page = page;
        this.browseEventsButton = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Browse Events →"));
    }

    public void waitForEventsToLoad(){
        browseEventsButton.waitFor();
    }

    public EventsPage openEventsPage(){
        waitForEventsToLoad();
        browseEventsButton.click();
        EventsPage eventsPage = new EventsPage(page);
        eventsPage.waitForEventsToLoad();
        return eventsPage;
    }
}
