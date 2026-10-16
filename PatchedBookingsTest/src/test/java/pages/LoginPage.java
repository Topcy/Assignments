package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class LoginPage {

    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    public Locator emailInput()    { return page.getByLabel("Email"); }
    public Locator passwordInput() { return page.getByLabel("Password"); }
    public Locator signInButton()  { return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In")); }

    public Locator signInHeading() {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Sign in to EventHub"));
    }

    public void open() {
        page.navigate("/login");
    }

    public void loginAs(String email, String password) {
        emailInput().fill(email);
        passwordInput().fill(password);
        signInButton().click();
    }
}
