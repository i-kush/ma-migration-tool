package com.kush.ma.migration.tool;

import com.kush.ma.migration.tool.service.common.Validator;
import com.kush.ma.migration.tool.service.handler.FilesHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MAMigrationApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MAMigrationApplication.class);

    @Autowired
    private Map<String, FilesHandler> filesHandlers;

    @Autowired
    private Validator validator;

    public static void main(String[] args) {
        SpringApplication.run(MAMigrationApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        validator.validateArgs(args);

        String rawFilesHandlerName = args[0];
        FilesHandler filesHandler = filesHandlers.get(rawFilesHandlerName);
        if (filesHandler == null) {
            log.error("No handlers with name '{}'. Available handlers: {}", rawFilesHandlerName, filesHandlers.keySet());
            return;
        }

        filesHandler.logBaseInfo();

        long before = System.currentTimeMillis();
        filesHandler.handle();
        log.info("Handling took {} millis", System.currentTimeMillis() - before);
    }


}
