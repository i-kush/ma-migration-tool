package com.kush.ma.migration.tool.service.handler;

import com.kush.ma.migration.tool.config.Config;
import com.kush.ma.migration.tool.service.common.Database;
import com.kush.ma.migration.tool.service.common.FilesInfoLoader;
import com.kush.ma.migration.tool.service.common.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class FilesHandler {

    private static final Logger log = LoggerFactory.getLogger(FilesHandler.class);

    @Autowired
    protected FilesInfoLoader filesInfoLoader;

    @Autowired
    protected Validator validator;

    @Autowired
    protected Config config;

    @Autowired
    protected Database database;

    protected List<String> fileNames;

    @PostConstruct
    public void postConstruct() throws Exception {
        validator.validateConfig(config);
        fileNames = filesInfoLoader.extractFileNames();
        customPostConstruct();
    }

    public abstract void handle() throws Exception;

    public void logBaseInfo() {
        log.info("Working with config {}", config);
        log.info("Working with '{}' files", fileNames.size());
    }

    protected void customPostConstruct() throws Exception {
    }

    protected List<String> getAvailableTableNames(Connection connection) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(null, null, "%", null)) {
            List<String> result = new ArrayList<>();

            while (resultSet.next()) {
                result.add(resultSet.getString(3));
            }

            return result;
        }
    }

    protected void prepareCommonPartOfHeader(Writer writer) throws IOException {
        writer.append("File");
        writer.append(config.getDelimiter());

        writer.append("Table");
        writer.append(config.getDelimiter());
    }

}
