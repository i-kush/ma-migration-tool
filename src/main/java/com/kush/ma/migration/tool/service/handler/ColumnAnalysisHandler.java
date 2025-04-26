package com.kush.ma.migration.tool.service.handler;

import com.kush.ma.migration.tool.dto.TableInfo;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("column-anal")
public class ColumnAnalysisHandler extends FilesHandler {

    @Autowired
    private GetInfoHandler getInfoHandler;

    @Override
    public void handle() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(config.getResultFile()), StandardCharsets.UTF_8))) {
            List<TableInfo> tableInfos = getInfoHandler.getTableInfos();
            Set<String> allColumns = getAllColumns(tableInfos);
            prepareHeader(writer, allColumns);
            writeResult(writer, tableInfos, allColumns);
        }
    }

    private Set<String> getAllColumns(List<TableInfo> tableInfos) {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (TableInfo tableInfo : tableInfos) {
            result.addAll(tableInfo.getColumns());
        }

        return result;
    }

    private void prepareHeader(Writer writer, Set<String> columns) throws IOException {
        prepareCommonPartOfHeader(writer);

        for (String column : columns) {
            writer.append(column);
            writer.append(config.getDelimiter());
        }

        writer.append(System.lineSeparator());

        writer.flush();
    }

    private void writeResult(Writer writer, List<TableInfo> tableInfos, Set<String> allColumns) throws IOException {
        for (TableInfo tableInfo : tableInfos) {
            writer.append(tableInfo.getFile());
            writer.append(config.getDelimiter());

            writer.append(tableInfo.getTable());
            writer.append(config.getDelimiter());

            for (String column : allColumns) {
                if (tableInfo.getColumns().stream().anyMatch(column::equalsIgnoreCase)) {
                    writer.append("y");
                }
                writer.append(config.getDelimiter());
            }

            writer.append(System.lineSeparator());
        }
    }

}
