package config;

public final class TestConfig {

    public static final String BASE_URL =
            System.getProperty("baseUrl", "https://eventhub.rahulshettyacademy.com");

    public static final String USER_EMAIL =
            System.getProperty("userEmail", "test123@gmail.com");

    public static final String USER_PASSWORD =
            System.getProperty("userPassword", "Testing1#");

    public static final boolean HEADLESS =
            Boolean.parseBoolean(System.getProperty("headless", "true"));

}
