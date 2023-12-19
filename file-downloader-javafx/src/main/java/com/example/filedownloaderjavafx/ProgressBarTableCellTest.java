package com.example.filedownloaderjavafx;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ProgressBarTableCellTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        TableView<TestTask> table = new TableView<TestTask>();

            table.getItems().add(new TestTask("http://212.183.159.230/100MB.zip"));
            table.getItems().add( new TestTask("http://212.183.159.230/50MB.zip"));

        TableColumn<TestTask, String> fileNameCol = new TableColumn("File name");
        fileNameCol.setCellValueFactory(new PropertyValueFactory<TestTask, String>("title"));
        fileNameCol.setPrefWidth(200);

        TableColumn<TestTask, Long> sizeCol = new TableColumn("Size");
        sizeCol.setCellValueFactory(new PropertyValueFactory<TestTask, Long>("value"));
        sizeCol.setPrefWidth(200);


        TableColumn<TestTask, Double> progressCol = new TableColumn("Progress");
        progressCol.setCellValueFactory(new PropertyValueFactory<TestTask, Double>("progress"));
        progressCol.setCellFactory(ProgressBarTableCell.<TestTask> forTableColumn());
        progressCol.setPrefWidth(200);

        TableColumn<TestTask, String> statusCol = new TableColumn("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<TestTask, String>("message"));
        statusCol.setPrefWidth(200);


        table.getColumns().addAll(fileNameCol,sizeCol,progressCol,statusCol);

        BorderPane root = new BorderPane();
        root.setCenter(table);
        primaryStage.setScene(new Scene(root));
        primaryStage.setWidth(1000);
        primaryStage.setHeight(600);
        primaryStage.show();

        ExecutorService executor = Executors.newFixedThreadPool(table.getItems().size(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        });


        for (TestTask task : table.getItems()) {
            executor.execute(task);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    static class TestTask extends Task<Void> {

        private String url;

        public TestTask(String url) {
            this.url = url;
        }

        @Override
        protected Void call() throws Exception {
            this.updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS, 1);
            this.updateMessage("Waiting...");
            this.updateTitle(url.toString());
            Thread.sleep(2000);

            String ext = url.substring(url.lastIndexOf("."), url.length());
            URLConnection connection = new URL(url).openConnection();
            long fileLength = connection.getContentLengthLong();

            System.out.println("size =" + fileLength);
            try (InputStream is = connection.getInputStream();
                 OutputStream os = Files.newOutputStream(Paths.get("downloads/downloadedfile" + ext))) {

                long nread = 0L;
                byte[] buf = new byte[8192];
                int n;
                this.updateMessage("Downloading...");
                while ((n = is.read(buf)) > 0) {
                    os.write(buf, 0, n);
                    nread += n;
                    updateProgress(nread, fileLength);
                }
                this.updateMessage("Done");
            }

            return null;
        }

       /* @Override
        protected Void call() throws Exception {
            try {
                this.updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS, 1);
                this.updateMessage("Waiting...");
                Thread.sleep(2000);
                String fileURL = url;
                URL url = new URL(fileURL);
                this.updateTitle(url.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                long contentLength = connection.getContentLengthLong();
                System.out.println("size =" + contentLength);

                Path outputFile = Path.of("downloadedfile");
                try (ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
                     FileOutputStream fos = new FileOutputStream(outputFile.toFile())) {
                    ByteBuffer buffer = ByteBuffer.allocate(16384); // 16KB buffer
                    long totalBytesRead = 0;
                    this.updateMessage("Downloading...");
                    while (rbc.read(buffer) != -1) {
                        System.out.println("downloading");
                        buffer.flip();
                        fos.getChannel().write(buffer);
                        buffer.clear();
                        totalBytesRead += buffer.limit();


                        // Update the progress bar on the JavaFX Application Thread
                        double progress = totalBytesRead / (double) contentLength;
                        updateProgress(progress, 1);
                        //Thread.sleep(50);

                        // Pause the downloading process if isPaused is true

                    }
                    this.updateMessage("Done");

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }*/

    }
}