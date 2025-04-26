package com.kush.ma.migration.tool.service.handler;

import com.kush.ma.migration.tool.dto.TableInfo;
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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service("get-info")
public class GetInfoHandler extends FilesHandler {

    private static final Logger log = LoggerFactory.getLogger(GetInfoHandler.class);

    @Override
    public void handle() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(config.getResultFile()), StandardCharsets.UTF_8))) {
            List<TableInfo> tableInfos = getTableInfos();
            int maxColumnsAmount = getMaxColumnsAmount(tableInfos);
            prepareHeader(writer, maxColumnsAmount);
            writeTableInfos(writer, tableInfos);
        }
    }

    public List<TableInfo> getTableInfos() {
        List<TableInfo> result = new ArrayList<>();

        for (String fileName : fileNames) {
            Connection connection = null;
            try {
                long before = System.currentTimeMillis();
                connection = database.getConnection(fileName);
                result.addAll(getTablesInfoFromFile(fileName, connection));
                log.info("File '{}' info getting took {} millis", fileName, System.currentTimeMillis() - before);
            } catch (SQLException e) {
                log.warn("Can't handle file '{}' in pre execution steps", fileName, e);
            } finally {
                Utils.silentlyClose(connection);
            }
        }

        return result;
    }

    private int getMaxColumnsAmount(List<TableInfo> tableInfos) {
        int result = -1;
        for (TableInfo tableInfo : tableInfos) {
            if (tableInfo.getColumns().size() > result) {
                result = tableInfo.getColumns().size();
            }
        }

        return result;
    }

    private void prepareHeader(Writer writer, int columnsAmount) throws IOException {
        prepareCommonPartOfHeader(writer);

        for (int i = 1; i <= columnsAmount; i++) {
            writer.append("ColumnName");
            writer.append(String.valueOf(i));
            writer.append(config.getDelimiter());
        }
        writer.append(System.lineSeparator());

        writer.flush();
    }

    private void writeTableInfos(BufferedWriter writer, List<TableInfo> tableInfos) throws IOException {
        for (TableInfo tableInfo : tableInfos) {
            writer.append(tableInfo.getFile());
            writer.append(config.getDelimiter());

            writer.append(tableInfo.getTable());
            writer.append(config.getDelimiter());

            for (String column : tableInfo.getColumns()) {
                writer.append(column);
                writer.append(config.getDelimiter());
            }

            writer.append(System.lineSeparator());
        }
    }

    public List<TableInfo> getTablesInfoFromFile(String file, Connection connection) throws SQLException {
        String tableFile = Paths.get(file).getFileName().toString();
        List<String> tables = getAvailableTableNames(connection);
        log.info("File '{}' has tables {}", file, tables);

        List<TableInfo> result = new ArrayList<>();
        for (String table : tables) {
            log.info("Getting info from table '{}' of file '{}'", table, file);
            result.add(getTableInfo(connection, table, tableFile));
        }

        return result;
    }

    private TableInfo getTableInfo(Connection connection, String table, String tableFile) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.prepareStatement(String.format("SELECT * FROM `%s` LIMIT 1", table));
            resultSet = statement.executeQuery();
            ResultSetMetaData resultMetaData = resultSet.getMetaData();

            int columnsCount = resultMetaData.getColumnCount();
            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= columnsCount; i++) {
                columns.add(resultMetaData.getColumnName(i));
            }
            columns.sort(String.CASE_INSENSITIVE_ORDER);

            return new TableInfo(tableFile, table, columns);
        } finally {
            Utils.silentlyClose(resultSet);
            Utils.silentlyClose(statement);
        }
    }

}
