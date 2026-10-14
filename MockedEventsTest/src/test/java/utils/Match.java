package utils;

import config.TestConfig;

import java.util.regex.Pattern;

public final class Match {

    private Match() {
    }

    public static Pattern urlOf(String path) {
       return Pattern.compile(TestConfig.url(path));
    }
}
