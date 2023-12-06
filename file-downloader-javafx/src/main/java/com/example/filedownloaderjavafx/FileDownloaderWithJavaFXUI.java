package com.example.filedownloaderjavafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;

public class FileDownloaderWithJavaFXUI extends Application {

    private ProgressBar progressBar;
    private Label statusLabel;
    Button cancelButton;
    Button pauseButton;
    private TextField urlTextField;

    private boolean isDownloading = true;
    private boolean isPaused = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("File Downloader");

        progressBar = new ProgressBar(0);
        statusLabel = new Label("Downloading: ");
        cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> isDownloading = false);
        pauseButton = new Button("Pause");
        pauseButton.setOnAction(e -> {
            isPaused = !isPaused;
            if (isPaused) {
                pauseButton.setText("Resume");
            } else {
                pauseButton.setText("Pause");
            }
        });
        urlTextField = new TextField();
        urlTextField.setPromptText("Enter download URL");

        Button downloadButton = new Button("Download");
        downloadButton.setOnAction(e -> {
            String fileURL = urlTextField.getText();
            if (!fileURL.isEmpty()) {
                Path outputFile = Path.of("downloadedFile.zip");
                downloadFileWithJavaFXUI(fileURL, outputFile);
            }
        });
        downloadButton.setStyle("-fx-background-color: #457ecd; -fx-text-fill: #ffffff;");

        HBox urlBox = new HBox(urlTextField, downloadButton,progressBar,pauseButton);
        urlBox.setSpacing(20);
        urlBox.setPadding(new Insets(10,10,10,10));

        BorderPane layout = new BorderPane();
        layout.setTop(statusLabel);
        //layout.setRight(progressBar);
        layout.setBottom(cancelButton);
        //layout.setRight(pauseButton);
        layout.setLeft(urlBox);
        BorderPane.setMargin(statusLabel, new Insets(10, 10, 0, 10));
        BorderPane.setMargin(progressBar, new Insets(20, 10, 10, 10));
        BorderPane.setMargin(cancelButton, new Insets(10, 10, 10, 10));
        BorderPane.setMargin(pauseButton, new Insets(10, 10, 10, 10));
        BorderPane.setMargin(urlBox, new Insets(10, 10, 10, 10));

        primaryStage.setScene(new Scene(layout, 600, 200));
        primaryStage.show();
    }

    void downloadFileWithJavaFXUI(String fileURL, Path outputFile) {
        Task<Void> downloadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    URL url = new URL(fileURL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    long contentLength = connection.getContentLengthLong();
                    System.out.println("Get content length: " + contentLength);

                    try (ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
                         FileOutputStream fos = new FileOutputStream(outputFile.toFile())) {
                        ByteBuffer buffer = ByteBuffer.allocate(16384); // 16KB buffer
                        long totalBytesRead = 0;

                        while (rbc.read(buffer) != -1 && isDownloading) {
                            buffer.flip();
                            fos.getChannel().write(buffer);
                            buffer.clear();
                            totalBytesRead += buffer.limit();

                            // Update the progress bar on the JavaFX Application Thread
                            double progress = totalBytesRead / (double) contentLength;
                            Platform.runLater(() -> progressBar.setProgress(progress));

                            // Pause the downloading process if isPaused is true
                            while (isPaused) {
                                //isDownloading = false;
                                Thread.sleep(50);
                            }
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        new Thread(downloadTask).start();

        downloadTask.setOnSucceeded(e -> {
            statusLabel.setText("Download complete: " + outputFile.getFileName());
            cancelButton.setText("Close");
            cancelButton.setOnAction(actionEvent -> Platform.exit());
        });

        downloadTask.setOnFailed(e -> {
            statusLabel.setText("Download is failed or canceled. See console for details.");
            cancelButton.setText("Close");
            cancelButton.setOnAction(actionEvent -> Platform.exit());
        });


    }
}