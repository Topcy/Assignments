package pages;

import config.TestConfig;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class LoginPage {

    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    private Locator emailInput() {
        return page.getByLabel("Email");
    }

    private Locator passwordInput() {
        return page.getByLabel("Password");
    }

    private Locator signInButton() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In"));
    }

    public LoginPage open() {
        page.navigate(TestConfig.url("/login"));
        return this;
    }

    public HomePage loginAs(String email, String password) {
        emailInput().fill(email);
        passwordInput().fill(password);
        signInButton().click();
        return new HomePage(page);
    }
}
