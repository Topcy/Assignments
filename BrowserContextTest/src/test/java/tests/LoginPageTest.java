package tests;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import config.PlaywrightConfig;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.LoginPage;
import retry.RetryAnalyzer;

import static org.testng.Assert.*;

public class LoginPageTest extends BaseTest{

    LoginPage loginPage;

    @DataProvider(name = "browserProjects")
    public Object[][] browserProjects(){
        return PlaywrightConfig.BROWSER_PROJECTS
                .stream()
                .map(browser -> new Object[]{browser})
                .toArray(Object[][]::new);

    }

    @Test(description = "verify that the login page loads correctly with the configured base URL",
            dataProvider = "browserProjects",
            retryAnalyzer = RetryAnalyzer.class
    )
    public void openLoginTest(String browserName){
        setUp(browserName);
        loginPage = new LoginPage(page);
        loginPage.openLoginPage();
        loginPage.waitForEventsToLoad();
        assertEquals(page.title(), "EventHub — Discover & Book Events");
        assertTrue(loginPage.isEmailFieldVisible());
        assertTrue(loginPage.isSignInButtonVisible());
    }

    @Test(description = "check that the email field contains the email value that was entered ",
            dataProvider = "browserProjects",
            retryAnalyzer = RetryAnalyzer.class
    )
    public void emailFieldValueTest(String browserName){
        setUp(browserName);
        loginPage = new LoginPage(page);
        loginPage.openLoginPage();
        String expectedEmail = "beginner@sample.com";
        loginPage.enterEmail(expectedEmail);
        String actualEmail = loginPage.getEmailValue();
        assertEquals(expectedEmail, actualEmail);
    }

    @Test(description = "verify a new browser context is created and running previous tests to ensure it is an isolated browser context ",
            dataProvider = "browserProjects",
            retryAnalyzer = RetryAnalyzer.class
    )
    public void isolatedBrowserContextTest(String browserName){
        setUp(browserName);
        BrowserContext newContext = browser.newContext();
        try {
            Page newPage = newContext.newPage();
            newPage.navigate(PlaywrightConfig.BASE_URL + "/login");
            LoginPage newLoginPage = new LoginPage(newPage);
            assertEquals(newPage.title(), "EventHub — Discover & Book Events");
            assertEquals("", newLoginPage.getEmailValue());
            assertTrue(newLoginPage.isEmailFieldVisible());
            assertTrue(newLoginPage.isSignInButtonVisible());
        } finally {
            //closing the new context
            if(newContext != null){
                newContext.close();
            }
        }


    }

}
