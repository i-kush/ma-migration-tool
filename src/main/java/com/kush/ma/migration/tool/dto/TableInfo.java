package com.kush.ma.migration.tool.dto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TableInfo {

    private final String file;
    private final String table;
    private final List<String> columns;

    public TableInfo(String file, String table, List<String> columns) {
        this.file = file;
        this.table = table;
        this.columns = columns;
    }

    public String getFile() {
        return file;
    }

    public String getTable() {
        return table;
    }

    public List<String> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableInfo tableInfo = (TableInfo) o;
        return Objects.equals(file, tableInfo.file) && Objects.equals(table, tableInfo.table) && Objects.equals(columns, tableInfo.columns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, table, columns);
    }
}
