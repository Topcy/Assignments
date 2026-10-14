package pages;

import resources.MockEvent;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;


public class EventsPage {

    private static final String SEARCH_PLACEHOLDER = "Search events, venues…";
    private final Page page;

    public EventsPage(Page page) {
        this.page = page;
    }

    public Locator heading() {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Upcoming Events"));
    }

    private Locator searchInput() {
        return page.getByPlaceholder(SEARCH_PLACEHOLDER);
    }

    private Locator categorySelect() {
        return page.getByRole(AriaRole.COMBOBOX).filter(new Locator.FilterOptions()
                .setHas(page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("All Categories"))));
    }

    private Locator citySelect() {
        return page.getByRole(AriaRole.COMBOBOX).filter(new Locator.FilterOptions()
                .setHas(page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("All Cities"))));
    }

    public Locator eventCards() {
        return page.getByTestId("event-card");
    }

    public Locator cardTitles() {
        return eventCards().getByRole(AriaRole.HEADING);
    }

    public Locator cardFor(MockEvent event) {
        return eventCards().filter(new Locator.FilterOptions().setHas(
                page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(event.title()).setExact(true))));
    }

    public Locator titleLink(Locator card) {
        return card.getByRole(AriaRole.LINK)
                .filter(new Locator.FilterOptions().setHas(page.getByRole(AriaRole.HEADING)));
    }

    public Locator bookNowLink(Locator card) {
        return card.getByTestId("book-now-btn");
    }

    public Locator priceOn(Locator card, MockEvent event) {
        return card.getByText(event.formattedPrice(), new Locator.GetByTextOptions().setExact(true));
    }

    public Locator seatsOn(Locator card, MockEvent event) {
        return card.getByText(event.cardSeatsText(), new Locator.GetByTextOptions().setExact(true));
    }

    public Locator anywhereWithText(String text) {
        return page.getByText(text);
    }

    public EventsPage filterBy(String keyword, String category, String city) {
        page.waitForResponse(response -> response.url().contains("search="),
                () -> searchInput().fill(keyword));

        page.waitForResponse(response -> response.url().contains("category=" + category),
                () -> categorySelect().selectOption(category));

        page.waitForResponse(response -> response.url().contains("city=" + city),
                () -> citySelect().selectOption(city));

        return this;
    }

    public EventDetailPage openBookNow(Locator card) {
        bookNowLink(card).click();
        return new EventDetailPage(page);
    }
}
