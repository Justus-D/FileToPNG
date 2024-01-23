package de.justusd.filetopng.controllers;

import de.justusd.filetopng.service.FileToPNG;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public class MainController {

    public TabPane tabPane;
    public HBox hBox;
    public Button chooseFileButton;
    public Button choosePNGButton;
    public Button chooseOutputFolderButton;
    public Label selectedFileLabel;
    public Label selectedOutputDirectoryLabel;
    public Button saveButton;
    public Label saveStatus;
    public ProgressBar saveProgressBar;
    public ProgressIndicator saveProgressIndicator;
    private File inputFile;
    private File outputDirectory;
    private FileToPNG fileToPNG;

    @FXML
    public void initialize() {
        HBox.setHgrow(tabPane, Priority.ALWAYS);
//        chooseFileButton.setOnMouseClicked();
    }

    public void handleChooseInputFile(ActionEvent event) {
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Input file");
            Stage stage = new Stage();
            File inputFileUpdate = fileChooser.showOpenDialog(stage);
            if (inputFileUpdate != null) {
                this.inputFile = inputFileUpdate;
                this.selectedFileLabel.setText(this.inputFile.getName());
            }
            this.updateSaveButton();
        });
    }

    public void handleChooseOutputFolder(ActionEvent event) {
        Platform.runLater(() -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Output directory for generated PNGs");
            Stage stage = new Stage();
            File outputDirectoryUpdate = directoryChooser.showDialog(stage);
            if (outputDirectoryUpdate != null) {
                this.outputDirectory = outputDirectoryUpdate;
                this.selectedOutputDirectoryLabel.setText(this.outputDirectory.getAbsolutePath());
            }
            this.updateSaveButton();
        });
    }
    public void updateSaveButton() {
        this.saveButton.setDisable(this.inputFile == null || this.outputDirectory == null);
    }

//    public void handleSaveOld(ActionEvent event) {
//        Task<Void> task = new Task<>() {
//            @Override
//            protected Void call() throws Exception {
//                long bytesTotal = MainController.this.inputFile.length();
//                AtomicLong bytesProcessed = FileToPNG.saveAsyncWithProgress(MainController.this.inputFile, MainController.this.outputDirectory);
//                while (bytesProcessed.get() < bytesTotal) {
//                    updateProgress(bytesProcessed.get(), bytesTotal);
//                    // MainController.this.saveStatus.setText("" + bytesProcessed + " bytes / " + bytesTotal + " bytes processed");
//                    Thread.sleep(50);
//                }
//                return null;
//            }
//        };
//        this.saveProgressBar.setVisible(true);
//        this.saveProgressBar.progressProperty().bind(task.progressProperty());
//        task.setOnSucceeded(event1 -> {
//            this.saveStatus.setText("Done.");
//            this.saveProgressBar.setVisible(false);
//        });
//        Thread thread = new Thread(task);
//        thread.setDaemon(true);
//        thread.start();
//    }

    public void handleSave(ActionEvent actionEvent) {
        if (this.inputFile == null || this.outputDirectory == null || this.fileToPNG != null) return;
        this.chooseFileButton.setDisable(true);
        this.chooseOutputFolderButton.setDisable(true);
        try {
            this.fileToPNG = new FileToPNG(inputFile, outputDirectory);
            FileToPNG f2p = this.fileToPNG;
            PropertyChangeListener changeListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent changeEvent) {
                    Platform.runLater(() -> {
                        String propertyName = changeEvent.getPropertyName();
                        if (propertyName.equals("bytesProcessed")) {
                            changeEvent.getNewValue();
                            float progress = f2p.progressPercentage();
                            MainController.this.saveProgressBar.setProgress(progress);
                            MainController.this.saveStatus.setText("Bytes processed: " + f2p.getBytesProcessed() + " / " + f2p.getBytesTotal() + " / " + ((int) (f2p.progressPercentage() * 100)) + " %");
                        }
                        else if (propertyName.equals("finished")) {
                            if (changeEvent.getNewValue().equals(true)) {
                                MainController.this.saveStatus.setText("Done.");
                                try {
                                    f2p.joinThread();
                                    MainController.this.chooseFileButton.setDisable(false);
                                    MainController.this.chooseOutputFolderButton.setDisable(false);
                                    MainController.this.fileToPNG = null;
                                } catch (InterruptedException ignore) {}
                            }
                        }
                    });
                }
            };
            this.fileToPNG.addPropertyChangeListener(changeListener);
            this.saveProgressBar.setVisible(true);
            this.fileToPNG.save();
        } catch (IOException e) {
            this.saveStatus.setText("An error occurred.");
            e.printStackTrace();
        }
    }

}
