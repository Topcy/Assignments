package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class DetailsPage {

    private final Page page;
    private Locator detailsPageHeading;
    private String detailsPageUrl;

    public DetailsPage(Page page) {
        this.page = page;
        this.detailsPageUrl = page.url();
        this.detailsPageHeading = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("World Tech Summit"));;
    }

    public void waitForEventsToLoad(){
        detailsPageHeading.waitFor();

    }

    public String returnUrl(){
        return detailsPageUrl;
    }

    public String compareTitle(){
        return detailsPageHeading.innerText();
    }

    public String comparePrice(){
       Locator priceText = page.getByText("$");
       return priceText.first().innerText();
    }

    public EventsPage returnToEventsList(){
        page.goBack();
        return new EventsPage(page);
    }
}
