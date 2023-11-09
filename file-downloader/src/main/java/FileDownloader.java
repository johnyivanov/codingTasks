import ch.qos.logback.classic.Logger;
import org.apache.commons.cli.*;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.TimeUnit;

public class FileDownloader {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(FileDownloader.class);

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("h", "help", false, "Help message");
        options.addOption("o", "output", true, "Output file name");
        options.addOption("u", "url", true, "URL of the file to download");

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("help") || cmd.getOptions().length == 0) {
                printHelp(options);
            } else if (cmd.hasOption("url") && cmd.hasOption("output")) {
                String fileURL = cmd.getOptionValue("url");
                String outputFile = cmd.getOptionValue("output");
                int maxRetries = 3; // Maximum number of connection retries
                downloadFileWithRetries(fileURL, outputFile, maxRetries);
                logger.info("File downloaded successfully.");
            } else {
                logger.error("Missing required options.");
                printHelp(options);
            }
        } catch (ParseException e) {
            logger.error(e.getMessage());
            printHelp(options);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            logger.error("File download failed.");
        }
    }
    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("FileDownloaderWithReconnect", options);
    }

    public static void downloadFileWithRetries(String fileURL, String outputFile, int maxRetries) throws IOException, InterruptedException {
        int retries = 0;
        boolean downloadComplete = false;

        while (retries < maxRetries) {
            try {
                downloadFileWithProgressBar(fileURL, outputFile);
                downloadComplete = true;
                break; // Download successful, exit loop
            } catch (IOException e) {
                logger.info("Connection lost. Retrying...");
                TimeUnit.SECONDS.sleep(10);//waiting for reconnect (10 sec for retry) until maximum restries (3) will be reached
                retries++;
            }
        }

        if (!downloadComplete) {
            throw new IOException("Exceeded maximum number of retries");
        }
    }

    public static void downloadFileWithProgressBar(String fileURL, String outputFile) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        int contentLength = connection.getContentLength();

        try (ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            int totalBytesRead = 0;

            while ((bytesRead = rbc.read(ByteBuffer.wrap(buffer))) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                // Display a progress bar
                int progress = (int) (100.0 * totalBytesRead / contentLength);
                System.out.print("\r[" + "=".repeat(progress / 2) + " ".repeat(50 - progress / 2) + "] " + progress + "%");
            }   //used System.out.print for downloading progress bar
            System.out.println("\n");
        }
    }
}
