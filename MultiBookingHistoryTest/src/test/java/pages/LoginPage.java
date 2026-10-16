package pages;

import config.TestConfig;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginPage extends BasePage {

    private final Locator emailInput = page.getByLabel("Email");
    private final Locator passwordInput = page.getByLabel("Password");
    private final Locator signInButton =
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In"));
    private final Locator heading =
            page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Sign in to EventHub"));

    public LoginPage(Page page) {
        super(page);
    }

    public LoginPage open() {
        page.navigate(TestConfig.BASE_URL + "/login");
        assertThat(heading).isVisible();   // proves the page really rendered (it is an SPA)
        return this;
    }

    public void loginAs(String email, String password) {
        emailInput.fill(email);
        passwordInput.fill(password);
        signInButton.click();
        assertThat(loggedInUserEmail()).hasText(email);
    }
}
