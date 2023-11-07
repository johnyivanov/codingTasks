import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileDownloader {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java FileDownloaderWithProgressBar <URL> <outputFile>");
            System.exit(1);
        }

        String fileURL = args[0];
        String outputFile = args[1];

        try {
            downloadFile(fileURL, outputFile);
            System.out.println("\nFile downloaded successfully!");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void downloadFile(String fileURL, String outputFile) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            long contentLength = connection.getContentLengthLong();
            System.out.println("File size: " + contentLength + " bytes");

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

    public static void displayProgressBar(int bytesRead, long contentLength) {
        int progress = (int) (bytesRead * 1.0 / contentLength * 100);
        System.out.print("\r[");

        for (int i = 0; i < 50; i++) {
            if (i < progress / 2) {
                System.out.print("=");
            } else {
                System.out.print(" ");
            }
        }
        System.out.print("] " + progress + "%");

    }

}