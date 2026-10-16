package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class EventsPage extends BasePage {

    private final Locator searchInput = page.getByPlaceholder("Search events");

    private final Locator categorySelect = page.getByRole(AriaRole.COMBOBOX)
            .filter(new Locator.FilterOptions().setHas(
                    page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("All Categories"))));

    private final Locator citySelect = page.getByRole(AriaRole.COMBOBOX)
            .filter(new Locator.FilterOptions().setHas(
                    page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("All Cities"))));

    private final Locator eventCards = page.getByTestId("event-card");

    public EventsPage(Page page) {
        super(page);
    }

    public EventsPage open() {
        goToEventsViaNav();
        assertThat(eventCards.first()).isVisible();
        return this;
    }

    public EventsPage filterBy(String category, String city, String text) {
        categorySelect.selectOption(category);
        citySelect.selectOption(city);
        searchInput.fill(text);
        return this;
    }

    public void openOnlyResult() {
        assertThat(eventCards).hasCount(1);
        eventCards.first().getByTestId("book-now-btn").click();
    }
}
