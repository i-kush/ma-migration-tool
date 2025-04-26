package com.kush.ma.migration.tool.service.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

@Service
public class ConsoleHandler {

    public String getString() {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            return bufferedReader.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException("Can't read value from console", e);
        }
    }
}
