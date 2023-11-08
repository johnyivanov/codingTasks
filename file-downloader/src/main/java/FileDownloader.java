import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


public class FileDownloader {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(FileDownloader.class);
    public static void main(String[] args) {
        if (args.length != 2) {
           logger.info("Usage: java FileDownloaderWithProgressBar <URL> <outputFile>");
            System.exit(1);
        }
        String fileURL = args[0];
        String outputFile = args[1];

        try {
            downloadFile(fileURL, outputFile);
            logger.info("\nFile downloaded successfully!");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public static void downloadFile(String fileURL, String outputFile) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            long contentLength = connection.getContentLengthLong();
            logger.info("File size: " + contentLength + " bytes");

            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(outputFile)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                int totalBytesRead = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    displayProgressBar(totalBytesRead,contentLength);
                }


            }
        } else {
            throw new IOException("HTTP Error: " + responseCode);
        }
    }

    public static void displayProgressBar(long current, long total) {
        int progress = (int) (current * 100 / total);
        System.out.println("\r[" + "=".repeat(progress) + " ".repeat(100 - progress) + "] " + progress + "%");
// used System.out.println for download progress bar, logger doesn't work correctly
    }

}