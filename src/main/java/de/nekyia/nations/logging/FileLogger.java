package de.nekyia.nations.logging;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

public class FileLogger {
    private Logger logger;
    private FileHandler fileHandler;
    private LocalDate currentDate;
    private int fileId;
    private String path; // Path to the log directory
    private String className; // For the initial header line
    private String filename;

    public FileLogger(String path, String filename) {
        // Initialize the logger with a unique name
        this.logger = Logger.getLogger(FileLogger.class.getName() + "_" + filename);
        this.logger.setUseParentHandlers(false); // Disable console logging

        // Rest of the constructor remains the same
        this.path = path;
        this.className = FileLogger.class.getName();
        this.filename = filename;

        this.currentDate = LocalDate.now();
        this.fileId = getNextFileId(currentDate);
        setupFileHandler();
    }

    private void setupFileHandler() {
        try {
            // Generate the file name based on the current date and ID
            String fileName = getFileNameForDate(currentDate, fileId);

            // Close and remove the old handler if it exists
            if (fileHandler != null) {
                logger.removeHandler(fileHandler);
                fileHandler.close();
            }

            // Create a new FileHandler with the new file name
            fileHandler = new FileHandler(fileName, true); // 'true' to append to existing file
            fileHandler.setFormatter(new OneLineFormatter());

            // Add the handler to the logger
            logger.addHandler(fileHandler);

            // Write the initial header line if the file is newly created
            File logFile = new File(fileName);
            if (logFile.length() == 0) {
                writeInitialHeader(logFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileNameForDate(LocalDate date, int id) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd_MMMM_yyyy");
        String dateString = date.format(formatter);
        String fileName = String.format(filename + "_%s_%d.log", dateString, id);

        // Combine the path and file name
        if (path != null && !path.isEmpty()) {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs(); // Create the directory if it doesn't exist
            }
            return new File(dir, fileName).getAbsolutePath();
        } else {
            return fileName;
        }
    }

    private int getNextFileId(LocalDate date) {
        int nextId = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd_MMMM_yyyy");
        String dateString = date.format(formatter);

        // Build the file pattern to match existing log files
        String datePattern = dateString.replace("_", "_"); // Escape underscores
        String filePattern = String.format("Nations_%s_\\d+\\.log", datePattern);

        // Determine the directory to search for existing log files
        File dir = (path != null && !path.isEmpty()) ? new File(path) : new File(".");

        // Ensure the directory exists
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // List files matching the pattern
        File[] matchingFiles = dir.listFiles((d, name) -> name.matches(filePattern));

        if (matchingFiles != null && matchingFiles.length > 0) {
            // Extract IDs and find the maximum
            for (File file : matchingFiles) {
                String fileName = file.getName();
                String idPart = fileName.substring(fileName.lastIndexOf('_') + 1, fileName.lastIndexOf('.'));
                try {
                    int existingId = Integer.parseInt(idPart);
                    if (existingId >= nextId) {
                        nextId = existingId + 1;
                    }
                } catch (NumberFormatException e) {
                    // Ignore files with invalid ID formats
                }
            }
        }

        return nextId;
    }

    private void checkDateAndUpdateHandler() {
        LocalDate now = LocalDate.now();
        if (!now.equals(currentDate)) {
            currentDate = now;
            fileId = getNextFileId(currentDate);
            setupFileHandler();
        }
    }

    private void writeInitialHeader(File logFile) {
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(logFile.toPath()), "UTF-8")) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM. dd, yyyy hh:mm:ss a");
            String dateTimeString = LocalDateTime.now().format(dateTimeFormatter);
            String header = String.format("%s %s logInfo%n", dateTimeString, className);
            writer.write(header);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to log INFO level messages
    public void logInfo(String message) {
        checkDateAndUpdateHandler();
        logger.info(message);
    }

    // Method to log WARNING level messages
    public void logWarning(String message) {
        checkDateAndUpdateHandler();
        logger.warning(message);
    }

    // Method to log SEVERE level messages
    public void logSevere(String message) {
        checkDateAndUpdateHandler();
        logger.severe(message);
    }

    // Close the FileHandler when done
    public void close() {
        if (fileHandler != null) {
            fileHandler.close();
        }
    }

    // Custom Formatter to format log messages in one line
    private static class OneLineFormatter extends Formatter {
        private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        @Override
        public String format(LogRecord record) {
            String time = LocalDateTime.now().format(timeFormatter);
            String level = record.getLevel().getName();
            String message = formatMessage(record).replaceAll("\\r?\\n", " "); // Remove newlines
            return String.format("[%s] [%s] %s%n", time, level, message);
        }
    }
}
