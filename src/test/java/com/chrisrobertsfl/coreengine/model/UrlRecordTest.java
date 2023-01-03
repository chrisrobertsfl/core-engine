package com.chrisrobertsfl.coreengine.model;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Disabled
public class UrlRecordTest {

    @Test
    void readFromInternet() throws MalformedURLException {
        URLRecord.fromURLs(List.of(new URL("https://api.publicapis.org/entries")))
                .stream()
                .map(URLRecord::stream)
                .map(InputStreamReader::new)
                .map(BufferedReader::new)
                .flatMap(BufferedReader::lines)
                .forEach(System.out::println);
    }

    @Test
    void readFromFile() throws MalformedURLException {
        URLRecord.fromURLs(List.of(new URL("file:///Users/TKMA5QX/.zshrc")))
                .stream()
                .map(URLRecord::stream)
                .map(InputStreamReader::new)
                .map(BufferedReader::new)
                .flatMap(BufferedReader::lines)
                .forEach(System.out::println);
    }
}

record URLRecord(String name, InputStream stream) {

    public static List<URLRecord> fromURLs(List<URL> urls) {
        return urls.stream()
                .map(URLRecord::fromURL)
                .collect(Collectors.toList());
    }

    static URLRecord fromURL(URL url) {
        try {
            return new URLRecord(url.getFile(), url.openStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}