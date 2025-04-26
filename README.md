# ma-migration-tool application

Is a plain java console spring boot application for migration of Microsoft Access files to the CSV files.
It helps to convert specific enterprise UA documents to the columns-based CSV files via filters.

1. `JRE` should be installed on PC before use: https://www.oracle.com/java/technologies/javase-jre8-downloads.html
2. Description:
    - `Java 8` application
    - prints info of work into console
    - handles directory with Microsoft Access files: apply search query to all files in directory and creates CSV-file
      in UTF-8 encoding
    - first column in CSV-file always will be a source file name
    - second column in CSV-file always will be a source table name
    - due to CSV-file is in `UTF-8` encoding it couldn't be open in Excel directly: it should be imported as `65001 UTF`
    - available modes names: `run-query`, `get-info`, `column-anal`
3. Files:
    - `ma-migration-tool-*.jar` is application
    - `application.properties` is main application properties
    - `query.sql` is a file with `SQL` query, which will be executed for MA tables
4. Start:
    - update `application.properties` with actual info
    - update `query.sql` with actual query
    - use command with actual path to `query.sql`: `java -jar ma-migration-tool-0.0.1-SNAPSHOT.jar MODE_NAME`
    - after configuration app could be started via `start.bat`
5. Result for mode `run-query`:
    - CSV-file will contain only columns from `query.sql` file with aggregated result according to query
6. Result for mode `get-info`:
    - CSV-file will contain all columns for each table in files
7. Result for mode `column-anal`:
    - CSV-file will contain all columns through all tables in all files as columns headers and cells will contain 'y'
      if column is present in table of file (compares ignoring case)