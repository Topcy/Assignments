package config;

import java.util.Arrays;
import java.util.List;

public class PlaywrightConfig {
    public static final String BASE_URL ="https://eventhub.rahulshettyacademy.com";
    public static final String TEST_FOLDER = "src/test/java/tests";
    public static final int RETRIES = 2;
    public static final List<String> BROWSER_PROJECTS = Arrays.asList("firefox","chromium");

    private PlaywrightConfig(){

    }
}
