package com.chrisrobertsfl.coreengine.base;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Character.*;
import static java.util.stream.IntStream.range;

public class MoreStrings {

    private static final String URL_REGEX = "^(https?|ftp|sftp):\\/\\/([^:\\/\\s]+)(:([^\\/]*))?((\\/[^\\s/\\/]+)*)?\\/?([^#\\s\\?]*)(\\?([^#\\s]*))?(#(\\w*))?$";
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);
    private static final String PACKAGE_NAME_REGEX = "^[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$";
    private static final Pattern PACKAGE_NAME_PATTERN = Pattern.compile(PACKAGE_NAME_REGEX);

    public static Optional<String> toCamelCase(String input) {
        StringBuilder result = new StringBuilder();
        final boolean[] nextIsUpper = {false};

        range(0, input.length())
                .mapToObj(input::charAt)
                .map(c -> {
                    if (isLetterOrDigit(c)) {
                        if (nextIsUpper[0]) {
                            c = Character.toUpperCase(c);
                            nextIsUpper[0] = false;
                        }
                        return String.valueOf(c);
                    } else {
                        nextIsUpper[0] = true;
                        return "";
                    }
                })
                .forEach(result::append);

        String resultString = result.toString();
        if (resultString.length() > 0 && isUpperCase(resultString.charAt(0))) {
            resultString = toLowerCase(resultString.charAt(0)) + resultString.substring(1);
        }

        return Optional.of(resultString)
                .filter(s -> s.length() > 0)
                .filter(s -> !Character.isDigit(s.charAt(0)));
    }

    public static boolean isUrlLegal(String urlString) {
        return Optional.ofNullable(urlString)
                .map(URL_PATTERN::matcher)
                .map(Matcher::matches)
                .orElse(false);
    }

    public static boolean isCamelCaseLegal(String name) {
        return toCamelCase(name).isPresent();
    }

    public static Optional<String> toPackageName(String packageName) {
        return Optional.ofNullable(packageName)
                .map(PACKAGE_NAME_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(m -> packageName);
    }

    public static boolean isPackageNameLegal(String name) {
        return toPackageName(name).isPresent();
    }

    public static Supplier<String> supplyString(String string) {
        return () -> string;
    }

    public static String dotsToSlashes(final String string) {
        return StringUtils.replace(string, ".", "/");
    }
}
