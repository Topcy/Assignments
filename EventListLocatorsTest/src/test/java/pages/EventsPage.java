package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class EventsPage {

    private final Page page;
    private final Locator eventsPageHeading;
    private final Locator searchBox;
    private final Locator categoriesOptions;
    private final Locator cityOptions;
    private final Locator eventCards;
    public Locator filteredEventCard;

    public EventsPage(Page page) {
        this.page = page;
        this.eventsPageHeading = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Upcoming Events"));
        this.searchBox = page.getByPlaceholder("Search events, venues…");
        this.categoriesOptions = page.locator("select").filter(new Locator.FilterOptions().setHasText("All Categories"));
        this.cityOptions = page.locator("select").filter(new Locator.FilterOptions().setHasText("Cities"));
        this.eventCards = page.locator("#event-card");
    }

    public void waitForEventsToLoad(){
        System.out.println(page.url());
        eventsPageHeading.waitFor();
    }

    public boolean isEventsPageHeadingVisible(){
        return eventsPageHeading.isVisible();
    }

    public void isEventCardsVisible(){
        assertThat(eventCards.first()).isVisible();
    }

    public Locator findEventCard(String searchText, String category, String city, String titleCard){
        searchBox.fill(searchText);
        categoriesOptions.selectOption(category);
        cityOptions.selectOption(city);
        isEventCardsVisible();
        filteredEventCard = eventCards.filter(new Locator.FilterOptions().setHasText(titleCard));
        return filteredEventCard;
    }


    public DetailsPage openBookNow(Locator locator){
        Locator bookNow = locator.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Book Now"));
        assertThat(bookNow).isVisible();
        assertThat(bookNow).isEnabled();
        bookNow.click();
        DetailsPage detailsPage = new DetailsPage(page);
        detailsPage.waitForEventsToLoad();
        return detailsPage;
    }

    public Locator clearFilter(){
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Clear filters")).click();
        Locator eventCards = page.locator("#event-card");
        System.out.println(eventCards.count());
        return eventCards;
    }
}
