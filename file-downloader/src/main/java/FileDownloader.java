import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.TimeUnit;

public class FileDownloader {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(FileDownloader.class);

    public static void main(String[] args) {
        if (args.length != 2) {
            logger.info("Usage: java FileDownloaderWithProgress <fileURL> <outputFile>");
            //example : java -jar .\file-downloader-1.0-SNAPSHOT-jar-with-dependencies.jar http://212.183.159.230/5MB.zip test.zip
            return;
        }

        String fileURL = args[0];
        String outputFile = args[1];

        try {
            downloadFileWithProgress(fileURL, outputFile, 3); // Retry 3 times on connection loss
            logger.info("File downloaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("File download failed.");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void downloadFileWithProgress(String fileURL, String outputFile, int maxRetries) throws IOException, InterruptedException {
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

        }
    }
}
