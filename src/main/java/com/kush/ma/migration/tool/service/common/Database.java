package com.kush.ma.migration.tool.service.common;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.stereotype.Service;

@Service
public class Database {

    private static final String URL_TEMPLATE = "jdbc:ucanaccess://%s;memory=false;cache_file_scale=16";

    @PostConstruct
    public void postConstruct() throws ClassNotFoundException {
        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
    }

    public Connection getConnection(String url) throws SQLException {
        return DriverManager.getConnection(String.format(URL_TEMPLATE, url));
    }
}
