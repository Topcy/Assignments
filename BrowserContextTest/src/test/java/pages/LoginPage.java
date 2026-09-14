package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginPage {
    private final Page page;
    private final Locator emailField;
    //private final Locator passwordField;
    private final Locator signInButton;

    public LoginPage(Page page) {
        this.page = page;
        this.emailField = page.getByPlaceholder("you@email.com");
        //this.passwordField = page.getByLabel("Password");
        this.signInButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign In"));
    }

    public void openLoginPage(){
        page.navigate("/login");
    }

    public void waitForEventsToLoad(){
        assertThat(page).hasTitle("EventHub — Discover & Book Events");
        assertThat(emailField).isVisible();
        assertThat(signInButton).isVisible();
    }

    public boolean isEmailFieldVisible(){
        return emailField.isVisible();
    }

    public boolean isSignInButtonVisible(){
        return signInButton.isVisible();
    }

    public void enterEmail(String email){
        emailField.fill(email);
    }

    public String getEmailValue(){
        return emailField.inputValue();
    }
}
