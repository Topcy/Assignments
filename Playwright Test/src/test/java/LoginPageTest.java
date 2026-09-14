import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import org.testng.annotations.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginPageTest {
    /* The core playwright library only provides browser automation APIs while @playwright/test runner is a comprehensive end-to-end testing framework built on top of the core library.
    However, Java doesn't use separate test runner packages. TestNG is the test runner.
     */

    Playwright playwright = Playwright.create();
    Browser browser = playwright.firefox().launch(new BrowserType.LaunchOptions().setHeadless(true));
    Page page = browser.newPage();

    private static final String baseURL = "https://eventhub.rahulshettyacademy.com";

    @Test
    public void  loginTest(){
        page.navigate(baseURL + "/login");
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Sign in to EventHub"))).isVisible();
        assertThat(page.getByPlaceholder("you@email.com")).isVisible();
        assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign in"))).isVisible();

    }
}
