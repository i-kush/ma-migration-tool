package com.kush.ma.migration.tool.service.common;

import com.kush.ma.migration.tool.config.Config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FilesInfoLoader {

    @Autowired
    private Config config;

    public String getFileContent(String filePaht) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePaht)), StandardCharsets.UTF_8);
    }

    public List<String> extractFileNames() throws IOException {
        Path path = Paths.get(config.getDir());

        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException(String.format("File '%s' is not a directory", config.getDir()));
        }

        return Files.walk(path)
                    .filter(Files::isRegularFile)
                    .filter(p -> hasValidExtension(p, config.getValidExtensions()))
                    .map(Path::toString)
                    .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean hasValidExtension(Path path, List<String> validExtensions) {
        boolean result = false;

        String pathAsString = path.toString();
        for (String validExtension : validExtensions) {
            if (pathAsString.toLowerCase().endsWith("." + validExtension)) {
                result = true;
                break;
            }
        }

        return result;
    }
}
