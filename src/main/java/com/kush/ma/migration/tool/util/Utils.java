package com.kush.ma.migration.tool.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    private Utils() {
        throw new UnsupportedOperationException();
    }

    public static void silentlyClose(AutoCloseable autoCloseable) {
        if (autoCloseable != null) {
            try {
                autoCloseable.close();
            } catch (Exception e) {
                log.warn("Can't close 'AutoCloseable' resource", e);
            }
        }
    }

}
