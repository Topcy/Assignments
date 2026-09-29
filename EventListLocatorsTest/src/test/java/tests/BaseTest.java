package tests;

import com.microsoft.playwright.*;
import config.PlaywrightConfig;
import org.testng.annotations.AfterMethod;

public class BaseTest {
    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;


    public void setUp(String browserName) {

        System.out.println("Starting browser project: " + browserName);
        playwright = Playwright.create();
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(true);
        switch (browserName.toLowerCase()) {

            case "chromium":
                browser = playwright.chromium().launch(options);
                break;

            case "firefox":
                browser = playwright.firefox().launch(options);
                break;

            default:
                throw new IllegalArgumentException("Unsupported browser project: " + browserName);
        }
        context = browser.newContext(new Browser.NewContextOptions().setBaseURL(PlaywrightConfig.BASE_URL));
        page = context.newPage();

        System.out.println("Page created successfully");


    }

    @AfterMethod
    void tearDown() {

        if (context != null) {
            context.close();
        }

        if (browser != null) {
            browser.close();
        }

        if (playwright != null) {
            playwright.close();
        }
    }
}
