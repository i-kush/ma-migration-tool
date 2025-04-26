package com.kush.ma.migration.tool.service.filter;

public abstract class Filter {

    public abstract boolean apply(String value);

    public abstract boolean isFilterCriteriaPresent();

    public abstract String getFilterName();

    public abstract String getColumnName();

    @Override
    public String toString() {
        return String.format(
                "%s{isFilterCriteriaPresent=%b, filterName=%s, columnName=%s}",
                getClass().getSimpleName(),
                isFilterCriteriaPresent(),
                getFilterName(),
                getColumnName()
        );
    }

}
