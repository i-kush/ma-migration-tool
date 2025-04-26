package com.kush.ma.migration.tool.config;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Value("${path.to.directory.with.ma.files}")
    private String dir;

    @Value("${path.to.query}")
    private String queryFile;

    @Value("${result.file}")
    private String resultFile;

    @Value("${process.all.tables}")
    private boolean processAllTables;

    @Value("${result.delimeter}")
    private String delimiter;

    @Value("${valid.extensions}")
    private List<String> validExtensions;

    @Value("${columns.mapping}")
    private String columnsMappingFile;

    public String getDir() {
        return dir;
    }

    public String getQueryFile() {
        return queryFile;
    }

    public String getResultFile() {
        return resultFile;
    }

    public boolean isProcessAllTables() {
        return processAllTables;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public List<String> getValidExtensions() {
        return Collections.unmodifiableList(validExtensions);
    }

    public String getColumnsMappingFile() {
        return columnsMappingFile;
    }

    @Override
    public String toString() {
        return "Config{" +
                "dir='" + dir + '\'' +
                ", queryFile='" + queryFile + '\'' +
                ", resultFile='" + resultFile + '\'' +
                ", processAllTables=" + processAllTables +
                ", delimeter='" + delimiter + '\'' +
                ", validExtensions=" + validExtensions +
                ", columnsMappingFile='" + columnsMappingFile + '\'' +
                '}';
    }
}
