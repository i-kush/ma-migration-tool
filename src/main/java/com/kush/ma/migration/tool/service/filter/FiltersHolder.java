package com.kush.ma.migration.tool.service.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FiltersHolder {

    private static final Logger log = LoggerFactory.getLogger(FiltersHolder.class);

    @Autowired
    private List<Filter> allFilters;

    private List<Filter> activeFilters;

    @PostConstruct
    public void postConstruct() {
        activeFilters = allFilters
                .stream()
                .filter(Filter::isFilterCriteriaPresent)
                .collect(Collectors.toList());
        log.info("All filters: {}", allFilters);
        log.info("Active filters: {}", activeFilters);
    }

    public List<Filter> getAllFilters() {
        return allFilters;
    }

    public List<Filter> getActiveFilters() {
        return activeFilters;
    }
}
