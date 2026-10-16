package config;


public final class TestConfig {

    private TestConfig() { }

    public static String baseUrl()  { return get("baseUrl",  "https://eventhub.rahulshettyacademy.com"); }
    public static String email()    { return get("email",    "test123@gmail.com"); }
    public static String password() { return get("password", "Testing1#"); }

    public static String browser()  { return get("browser", "chromium"); }
    public static boolean headless(){ return Boolean.parseBoolean(get("headless", "true")); }

    private static String get(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }
}
