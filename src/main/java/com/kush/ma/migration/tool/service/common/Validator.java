package com.kush.ma.migration.tool.service.common;

import com.kush.ma.migration.tool.config.Config;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

@Service
public class Validator {

    public void validateConfig(Config config) {
        if (isEmpty(config.getDir()) || !Files.isDirectory(Paths.get(config.getDir()))) {
            throw new IllegalArgumentException(String.format("'%s' is not a directory", config.getDir()));
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public void validateArgs(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Handler name should be specified");
        }
    }
}
