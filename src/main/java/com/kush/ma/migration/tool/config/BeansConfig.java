package com.kush.ma.migration.tool.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

    @Bean
    public Map<String, Set<String>> columnsMapping(Config config) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(config.getColumnsMappingFile()), StandardCharsets.UTF_8);

        Map<String, Set<String>> columnsMapping = new HashMap<>();
        for (String line : lines) {
            processColumnMapping(config.getDelimiter(), line, columnsMapping);
        }

        return Collections.unmodifiableMap(columnsMapping);
    }

    private void processColumnMapping(String delimiter, String line, Map<String, Set<String>> columnsMapping) {
        int splitIndex = line.indexOf('=');
        String key = line.substring(0, splitIndex).trim();
        Set<String> value = Arrays
                .stream(line.substring(splitIndex + 1).split(Pattern.quote(delimiter)))
                .collect(Collectors.toSet());

        columnsMapping.put(key, Collections.unmodifiableSet(value));
    }

}
