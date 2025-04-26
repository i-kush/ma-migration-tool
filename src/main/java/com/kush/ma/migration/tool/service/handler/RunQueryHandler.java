package com.kush.ma.migration.tool.service.handler;

import com.kush.ma.migration.tool.dto.TableInfo;
import com.kush.ma.migration.tool.service.common.ConsoleHandler;
import com.kush.ma.migration.tool.service.filter.FiltersHolder;
import com.kush.ma.migration.tool.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("run-query")
public class RunQueryHandler extends FilesHandler {

    private static final Logger log = LoggerFactory.getLogger(RunQueryHandler.class);
    private static final String FIRST_PART = ":::";
    private static final String SECOND_PART = ":::";

    @Autowired
    private ConsoleHandler consoleHandler;

    @Autowired
    private Map<String, Set<String>> columnsMapping;

    @Autowired
    private GetInfoHandler getInfoHandler;

    @Autowired
    private FiltersHolder filtersHolder;

    private boolean isSmartQuery;

    public void customPostConstruct() throws IOException {
        String sql = filesInfoLoader.getFileContent(config.getQueryFile());
        isSmartQuery = sql.contains(FIRST_PART) || sql.contains(SECOND_PART);

        log.info("Gonna work with {} filters: {}", filtersHolder.getActiveFilters().size(), filtersHolder.getActiveFilters());
    }

    @Override
    public void handle() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(config.getResultFile()), StandardCharsets.UTF_8))) {
            String sql = filesInfoLoader.getFileContent(config.getQueryFile());
            log.info("Working with query {}", sql);

            List<String> resultColumns = getResultColumns(sql);
            prepareHeader(writer, resultColumns);
            for (String fileName : fileNames) {
                long before = System.currentTimeMillis();
                processTable(sql, writer, fileName, resultColumns);
                log.info("File '{}' processing took {} millis", fileName, System.currentTimeMillis() - before);
            }
        }
    }

    private List<String> getResultColumns(String sql) {
        String rawColumns = sql.substring("select ".length(), sql.toLowerCase().indexOf(" from ")).trim();
        String[] columns = rawColumns.split("`");

        List<String> result = new ArrayList<>();
        for (String rawColumn : columns) {
            String column = rawColumn.trim();
            if (!column.isEmpty() && !",".equals(column)) {
                result.add(column);
            }
        }

        if (isSmartQuery) {
            result = result
                    .stream()
                    .map(this::handleSmartColumn)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        log.info("Working with columns {}. Smart={}", result, isSmartQuery);

        return result;
    }

    private String handleSmartColumn(String column) {
        return column.substring(FIRST_PART.length(), column.length() - SECOND_PART.length());
    }

    private void prepareHeader(Writer writer, List<String> columns) throws IOException {
        prepareCommonPartOfHeader(writer);

        for (String column : columns) {
            writer.append(column);
            writer.append(config.getDelimiter());
        }
        writer.append(System.lineSeparator());

        writer.flush();
    }

    private void processTable(String query, Writer writer, String file, List<String> columns) throws IOException {
        String tableFile = Paths.get(file).getFileName().toString();
        Connection connection = null;

        try {
            connection = database.getConnection(file);
            List<String> availableTables = getAvailableTableNames(connection);
            log.info("File '{}' has tables {}", file, availableTables);

            List<String> tables = config.isProcessAllTables() || 1 == availableTables.size() ? availableTables : getUserInputTables(availableTables);
            List<TableInfo> tableInfos = isSmartQuery ? getInfoHandler.getTablesInfoFromFile(file, connection) : Collections.emptyList();
            log.info("About to process tables: {}", tables);

            for (String table : tables) {
                log.info("Processing table '{}' of file '{}'", table, file);
                executeStatement(connection, writer, query, table, tableFile, columns, tableInfos);
            }
        } catch (SQLException | RuntimeException e) {
            log.warn("Can't handle file '{}' in pre execution steps", file, e);

            writeStub(writer, tableFile, "UNKNOWN", "ERROR", columns);

            writer.append(e.getMessage());
            writer.append(System.lineSeparator());
        } finally {
            writer.flush();

            Utils.silentlyClose(connection);
        }
    }

    private List<String> prepareSmartColumns(List<String> columns, TableInfo tableInfo) {
        List<String> result = new ArrayList<>();

        for (String column : columns) {
            Set<String> mappedColumns = columnsMapping.get(column);
            String mappedColumn = tableInfo.getColumns()
                                           .stream()
                                           .filter(mappedColumns::contains)
                                           .findFirst()
                                           .orElseThrow(() -> getExceptionForWrongColumns(column, tableInfo.getTable(), tableInfo.getFile()));
            result.add(mappedColumn);
        }

        return result;
    }

    private String prepareSmartQuery(String query, List<String> columns, TableInfo tableInfo) {
        String result = query;

        for (String column : columns) {
            Set<String> mappedColumns = columnsMapping.get(column);
            String mappedColumn = tableInfo.getColumns()
                                           .stream()
                                           .filter(mappedColumns::contains)
                                           .findFirst()
                                           .orElseThrow(() -> getExceptionForWrongColumns(column, tableInfo.getTable(), tableInfo.getFile()));
            result = result.replaceAll(
                    String.format("%s%s%s", FIRST_PART, column, SECOND_PART),
                    mappedColumn.replace("$", "\\$")
            );
        }

        return result;
    }

    private RuntimeException getExceptionForWrongColumns(String column, String table, String file) {
        return new RuntimeException(
                String.format(
                        "Column group '%s' values are absent across actual columns for table '%s' of file '%s'",
                        column,
                        table,
                        file
                )
        );
    }

    private void executeStatement(Connection connection,
                                  Writer writer,
                                  String query,
                                  String table,
                                  String tableFile,
                                  List<String> columns,
                                  List<TableInfo> tableInfos) throws IOException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            if (isSmartQuery) {
                TableInfo tableInfo = tableInfos
                        .stream()
                        .filter(e -> table.equalsIgnoreCase(e.getTable()))
                        .findFirst()
                        .orElseThrow(RuntimeException::new);
                List<String> columnsTemplate = columns;
                columns = prepareSmartColumns(columnsTemplate, tableInfo);
                log.info("Smart columns for table '{}': {}", table, columns);

                query = prepareSmartQuery(query, columnsTemplate, tableInfo);
                log.info("Smart query for table '{}': {}", table, query);
            }

            statement = connection.prepareStatement(MessageFormat.format(query, table));
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                writer.append(tableFile);
                writer.append(config.getDelimiter());

                writer.append(table);
                writer.append(config.getDelimiter());

                for (String column : columns) {
                    writer.append(resultSet.getString(column));
                    writer.append(config.getDelimiter());
                }

                writer.append(System.lineSeparator());
            }
        } catch (SQLException | RuntimeException e) {
            log.warn("Can't handle table '{}' in file '{}'", table, tableFile, e);

            writeStub(writer, tableFile, table, "ERROR", columns);

            writer.append(e.getMessage());
            writer.append(System.lineSeparator());
        } finally {
            Utils.silentlyClose(resultSet);
            Utils.silentlyClose(statement);
        }
    }

    private void writeStub(Writer writer, String tableFile, String table, String columnValue, List<String> columns) throws IOException {
        writer.append(tableFile);
        writer.append(config.getDelimiter());

        writer.append(table);
        writer.append(config.getDelimiter());

        for (String column : columns) {
            writer.append(columnValue);
            writer.append(config.getDelimiter());
        }
    }

    private List<String> getUserInputTables(List<String> availableTables) {
        log.info("Please select tables for processing with ',' separation (e.g. TABLE1, TABLE2): {}", availableTables);
        String rawTablesAsString = consoleHandler.getString();
        log.info("Received raw string from user's input '{}'", rawTablesAsString);

        return Arrays.stream(rawTablesAsString.split(","))
                     .map(String::trim)
                     .filter(availableTables::contains)
                     .collect(Collectors.toList());
    }
}
