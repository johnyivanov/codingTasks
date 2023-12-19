package com.example.filedownloaderjavafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;

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
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class FileDownloaderWithJavaFXUI extends Application {



    private final ObservableList<DownloadTask> urlData =
            FXCollections.observableArrayList();

    private ExecutorService service = Executors.newCachedThreadPool();

       public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start( Stage stage) {

        Scene scene = new Scene(new Group());
        stage.setTitle("Table View Sample");
        stage.setWidth(900);
        stage.setHeight(600);

        TableView<DownloadTask> table = new TableView<>();
        table.setEditable(true);

        table.getItems().add(new DownloadTask("http://212.183.159.230/100MB.zip"));

        Label label = new Label("File downloader");
        label.setFont(new Font("Arial", 20));

        TableColumn fileUrlColumn = new TableColumn("File URL");
        fileUrlColumn.setMinWidth(200);
        //fileUrlColumn.setCellValueFactory(new PropertyValueFactory<DownloadTask, String>(""));

        TableColumn fileSizeColumn = new TableColumn("Size");
        fileSizeColumn.setMinWidth(200);
        //fileSizeColumn.setCellValueFactory(new PropertyValueFactory<Collector, String>("size"));
        TableColumn progressBarColumn = new TableColumn("Downloading %");
        progressBarColumn.setMinWidth(200);
        progressBarColumn.setCellValueFactory(new PropertyValueFactory<DownloadTask, Double>("progress"));
        progressBarColumn.setCellFactory(ProgressBarTableCell.<ProgressBarTableCellTest.TestTask> forTableColumn());



       /* progressBarColumn = new TableColumn("Downloading %");
        progressBarColumn.setMinWidth(200);
        progressBarColumn.setCellFactory(ProgressBarTableCell.<Collector> forTableColumn());
        //progressBarColumn.setCellValueFactory(new PropertyValueFactory<Collector, Double>("progressBar"));
        //progressBarColumn.setCellFactory(ProgressBarTableCell.<Collector> forTableColumn());
*/
        TableColumn statusColumn = new TableColumn("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<DownloadTask, String>("message"));
        statusColumn.setPrefWidth(200);

        //table.setItems(urlData);
        table.getColumns().addAll(fileUrlColumn, fileSizeColumn, progressBarColumn,statusColumn);

        TextField urlTextField = new TextField();
        urlTextField.setMinWidth(150);
        urlTextField.setPromptText("File URL");
        urlTextField.setMaxWidth(fileUrlColumn.getPrefWidth());


        final Button addButton = new Button("Add");
        addButton.setOnAction(new EventHandler<ActionEvent>() {

            //Path outputFile = Path.of("downloadedFile.zip");
            @Override
            public void handle(ActionEvent e) {
                if (!urlTextField.getText().isEmpty()) {

                   // urlData.add(new DownloadTask(urlTextField.getText()));
                  //table.getItems().add(new DownloadTask(urlTextField.getText()));
                    //downloadFileWithJavaFXUI(urlTextField.getText(), outputFile);
                    urlTextField.clear();
                }
            }
        });
        Button cancelButton = new Button("Cancel");
        //cancelButton.setOnAction(e -> isDownloading = false);

       /* TableView.TableViewSelectionModel<TableData> selectionModel =
                table.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);*/

        Button downloadButton = new Button("Download");
       /* downloadButton.setOnAction(new EventHandler<ActionEvent>() {

            Path outputFile = Path.of("downloadedFile.zip");
            @Override
            public void handle(ActionEvent e) {
                if (!urlTextField.getText().isEmpty()) {

                    urlData.add(new TableData(urlTextField.getText()));

                    downloadFileWithJavaFXUI(urlTextField.getText(), outputFile);
                    urlTextField.clear();
                }
            }
        });*/
        Button  pauseButton = new Button("Pause");
       /* pauseButton.setOnAction(e -> {
            isPaused = !isPaused;
            if (isPaused) {
                pauseButton.setText("Resume");
            } else {
                pauseButton.setText("Pause");
            }
        });*/
        Button stopButton = new Button("Stop");
       // stopButton.setOnAction(e -> isDownloading = false);

        final HBox hb = new HBox();
        hb.getChildren().addAll(urlTextField,addButton,cancelButton);
        hb.setSpacing(5);

        final HBox hb1 = new HBox();
        hb1.getChildren().addAll(downloadButton,pauseButton,stopButton);
        hb1.setSpacing(5);

        final VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label,hb1,table, hb);

        ((Group) scene.getRoot()).getChildren().addAll(vbox);

        stage.setScene(scene);
        stage.show();
       /* ExecutorService executor = Executors.newFixedThreadPool(table.getItems().size(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        });
        for (DownloadTask task : table.getItems()) {
            executor.execute(task);
        }*/
       //DownloadTask downloadTask = new DownloadTask(urlTextField.getText());
      // executor.execute(downloadTask);*/
    }


   /* void downloadFileWithJavaFXUI(String fileURL, Path outputFile) {
        Task<Void> downloadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    URL url = new URL(fileURL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    long contentLength = connection.getContentLengthLong();
                    System.out.println("size =" + contentLength);
                    size = contentLength;
                    //fileSizeColumn.setCellValueFactory(new PropertyValueFactory<TableData, String>("size"));
                    fileSizeColumn.setCellValueFactory((Callback<TableColumn.CellDataFeatures, ObservableValue>)
                            new SimpleStringProperty(humanReadableByteCountSI(contentLength)));

                    try (ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
                         FileOutputStream fos = new FileOutputStream(outputFile.toFile())) {
                        ByteBuffer buffer = ByteBuffer.allocate(16384); // 16KB buffer
                        long totalBytesRead = 0;

                        while (rbc.read(buffer) != -1 && isDownloading) {
                            System.out.println("downloading");
                            buffer.flip();
                            fos.getChannel().write(buffer);
                            buffer.clear();
                            totalBytesRead += buffer.limit();
                            progressBar = new ProgressBar(0);

                            // Update the progress bar on the JavaFX Application Thread
                            double progress = totalBytesRead / (double) contentLength;
                            long finalTotalBytesRead = totalBytesRead;

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
            //label.setText("Download complete: " + outputFile.getFileName());
            cancelButton.setText("Exit");
            cancelButton.setCancelButton(isDownloading = false);
            cancelButton.setOnAction(actionEvent -> Platform.exit());
        });

        downloadTask.setOnFailed(e -> {
            label.setText("Download is failed or canceled. See console for details.");
            cancelButton.setText("Close");
            cancelButton.setOnAction(actionEvent -> Platform.exit());
        });


    }*/
    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }


    public static class DownloadTask extends Task<Void> {

        private String url;

        public DownloadTask(String url) {
            this.url = url;
        }

        @Override
        protected Void call() throws Exception {
            try {
               // this.updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS, 1);
                this.updateMessage("Waiting...");
                String fileURL = url;
                URL url = new URL(fileURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                long contentLength = connection.getContentLengthLong();
                System.out.println("size =" + contentLength);

                Path outputFile = Path.of("downloadedfile");
                try (ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
                     FileOutputStream fos = new FileOutputStream(outputFile.toFile())) {
                    ByteBuffer buffer = ByteBuffer.allocate(16384); // 16KB buffer
                    long totalBytesRead = 0;
                    this.updateMessage("Downloading...");
                    while (rbc.read(buffer) != -1 ) {
                        System.out.println("downloading");
                        buffer.flip();
                        fos.getChannel().write(buffer);
                        buffer.clear();
                        totalBytesRead += buffer.limit();


                        // Update the progress bar on the JavaFX Application Thread
                        double progress = totalBytesRead / (double) contentLength;
                        updateProgress(progress,100);

                        // Pause the downloading process if isPaused is true

                    }
                    this.updateMessage("Done");

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void failed() {
            System.out.println("Finished on thread: " + Thread.currentThread().getName());
            System.out.println("failed");
        }

        @Override
        protected void succeeded() {
            System.out.println("downloaded");
        }



    }

}















