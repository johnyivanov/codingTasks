package com.example.filedownloaderjavafx;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class FileDownloaderJavaFX extends Application {
static boolean isDownloading = false;
static boolean isPaused = false;

    @Override
    public void start(Stage primaryStage) {

        Scene scene = new Scene(new Group());
        primaryStage.setTitle("Table View Sample");
        primaryStage.setWidth(900);
        primaryStage.setHeight(600);


        TableView<TestTask> table = new TableView<TestTask>();

        Label label = new Label("File downloader");
        label.setFont(new Font("Arial", 20));

        TableColumn<TestTask, String> fileNameCol = new TableColumn("File name");
        fileNameCol.setCellValueFactory(new PropertyValueFactory<TestTask, String>("title"));
        fileNameCol.setPrefWidth(200);

        TableColumn<TestTask, String> sizeCol = new TableColumn("Size");
        sizeCol.setCellValueFactory(cellData -> cellData.getValue().sizePropertyProperty());
        sizeCol.setPrefWidth(200);


        TableColumn<TestTask, Double> progressCol = new TableColumn("Progress");
        progressCol.setCellValueFactory(new PropertyValueFactory<TestTask, Double>("progress"));
        progressCol.setCellFactory(ProgressBarTableCell.<TestTask> forTableColumn());
        progressCol.setPrefWidth(200);

        TableColumn<TestTask, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<TestTask, String>("message"));
        statusCol.setPrefWidth(200);


        table.getColumns().addAll(fileNameCol,sizeCol,progressCol,statusCol);

        TextField urlTextField = new TextField();
        urlTextField.setMinWidth(150);
        urlTextField.setPromptText("File URL");
        urlTextField.setMaxWidth(fileNameCol.getPrefWidth());

        final Button addButton = new Button("Add");
        addButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                if (!urlTextField.getText().isEmpty()) {
                    TestTask testTask = new TestTask(urlTextField.getText());
                    table.getItems().add(testTask);
                    ExecutorService executor = Executors.newFixedThreadPool(table.getItems().size(), new ThreadFactory() {
                        @Override
                        public Thread newThread(Runnable r) {
                            Thread t = new Thread(r);
                            t.setDaemon(true);
                            return t;
                        }
                    });
                      executor.execute(testTask);

                      urlTextField.clear();
                }
            }
        });

        final HBox hb = new HBox();
        hb.getChildren().addAll(urlTextField,addButton);
        hb.setSpacing(5);

        Button downloadButton = new Button("Download");
        Button  pauseButton = new Button("Pause");
        // Listen for a mouse click and access the selectedItem property
        table.setOnMouseClicked(event -> {
            // Make sure the user clicked on a populated item
            if (table.getSelectionModel().getSelectedItem() != null) {
                System.out.println("You clicked on " + table.getSelectionModel().getSelectedItem().getUrl());

                pauseButton.setOnAction(e -> {
                    isPaused = !isPaused;
                    if (isPaused) {
                        pauseButton.setText("Resume");
                    } else {
                        pauseButton.setText("Pause");
                    }

                });
            }
        });


        Button stopButton = new Button("Stop");

        final HBox hb1 = new HBox();
        hb1.getChildren().addAll(downloadButton,pauseButton,stopButton);
        hb1.setSpacing(5);

        final VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label,hb1,table,hb);

        ((Group) scene.getRoot()).getChildren().addAll(vbox);

        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class TestTask extends Task<Void> {

        private String url;
        private SimpleStringProperty sizeProperty;

        public TestTask(String url) {
            this.url = url;
            this.sizeProperty = new SimpleStringProperty();
        }
        public String getSize() {
            return sizeProperty.get();
        }

        public String getSizeProperty() {
            return sizeProperty.get();
        }

        public void setSizeProperty(String  sizeProperty) {
            this.sizeProperty.set(sizeProperty);
        }

        public final SimpleStringProperty sizePropertyProperty() {
            return sizeProperty;
        }
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        protected Void call() throws Exception {
            isDownloading = true;
            this.updateProgress(ProgressIndicator.INDETERMINATE_PROGRESS, 1);
            this.updateMessage("Waiting...");
            this.updateTitle(url.toString());
            Thread.sleep(2000);

            String ext = url.substring(url.lastIndexOf("."), url.length());
            URLConnection connection = new URL(url).openConnection();
            long fileLength = connection.getContentLengthLong();
            System.out.println("size =" + humanReadableByteCountSI(fileLength));
             setSizeProperty(humanReadableByteCountSI(fileLength));
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

                    while (isPaused) {
                        isDownloading = false;
                        Thread.sleep(50);
                    }
                }
                this.updateMessage("Done");
            }

            return null;
        }
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


    }
}