package config;

public final class TestConfig {

    private TestConfig() {

    }

    public static String baseUrl() {
        return System.getProperty("baseUrl", "https://eventhub.rahulshettyacademy.com");
    }

    public static String email() {
        return System.getProperty("email", "test123@gmail.com");
    }

    public static String password() {
        return System.getProperty("password", "Testing1#");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(System.getProperty("headless", "true"));
    }

    public static String url(String path) {
        return baseUrl() + path;
    }
}
