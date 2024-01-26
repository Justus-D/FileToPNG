package de.justusd.filetopng.controllers;

import de.justusd.filetopng.service.filetopng.FileToPNG;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

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
    public Tab tabFileToPNG;
    public Tab tabPNGToFile;
    public Button resetSaveButton;
    public Button saveDebug;
    public Button chooseInputDirectoryButton;
    public Button chooseOutputFileButton;
    public Label selectedInputDirectoryLabel;
    public Label selectedOutputFileLabel;
    public Button restoreButton;
    public Button resetRestoreButton;
    public Button restoreDebugButton;
    public Label restoreStatus;
    public ProgressBar restoreProgressBar;
    public Button detectFileNameButton;
    private File inputFile;
    private File outputDirectory;
    private File inputDirectory;
    private File outputFile;
    private FileToPNG fileToPNGSave;
    private FileToPNG fileToPNGRestore;
    private String suggestedOutputName;

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
        this.selectedFileLabel.setText(inputFile == null ? "No file selected" : inputFile.getName());
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
        this.selectedOutputDirectoryLabel.setText(outputDirectory == null ? "No output directory selected" : outputDirectory.getAbsolutePath());
    }

    public void setInputDirectory(File inputDirectory) {
        this.inputDirectory = inputDirectory;
        this.selectedInputDirectoryLabel.setText(inputDirectory == null ? "No directory selected" : inputDirectory.getAbsolutePath());
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
        this.selectedOutputFileLabel.setText(outputFile == null ? "No file selected" : outputFile.getName());
    }

    @FXML
    public void initialize() {
        HBox.setHgrow(tabPane, Priority.ALWAYS);
        if (System.getenv("DEBUG") != null) {
            this.saveDebug.setVisible(true);
            this.restoreDebugButton.setVisible(true);
        }
    }

    public void handleChooseInputFile(ActionEvent event) {
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Input file");
            Stage stage = new Stage();
            File inputFileUpdate = fileChooser.showOpenDialog(stage);
            if (inputFileUpdate != null) {
                this.setInputFile(inputFileUpdate);
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
                this.setOutputDirectory(outputDirectoryUpdate);
            }
            this.updateSaveButton();
        });
    }

    public void updateSaveButton() {
        this.saveButton.setDisable(this.inputFile == null || this.outputDirectory == null);
    }

    public void handleSave(ActionEvent actionEvent) {
        if (this.inputFile == null || this.outputDirectory == null || this.fileToPNGSave != null) return;
        this.chooseFileButton.setDisable(true);
        this.chooseOutputFolderButton.setDisable(true);
        this.saveButton.setDisable(true);
        this.resetSaveButton.setDisable(true);
        this.tabPNGToFile.setDisable(true);
        try {
            this.fileToPNGSave = new FileToPNG(inputFile, outputDirectory);
            FileToPNG f2p = this.fileToPNGSave;

            // Reflect status in GUI
            PropertyChangeListener changeListener = changeEvent -> Platform.runLater(() -> {
                String propertyName = changeEvent.getPropertyName();
                if (propertyName.equals("bytesProcessed")) {
                    float progress = f2p.progressPercentage();
                    MainController.this.saveProgressBar.setProgress(progress);
                    MainController.this.saveStatus.setText("Bytes processed: " + f2p.getBytesProcessed() + " / " + f2p.getBytesTotal() + " / " + ((int) (f2p.progressPercentage() * 100)) + " %");
                }
                else if (propertyName.equals("finished")) {
                    if (changeEvent.getNewValue().equals(true)) {
                        MainController.this.saveStatus.setText("Done.");
                        MainController.this.saveProgressBar.setVisible(false);
                        MainController.this.saveProgressBar.setProgress(0);
                        try {
                            f2p.joinThread();
                            MainController.this.chooseFileButton.setDisable(false);
                            MainController.this.chooseOutputFolderButton.setDisable(false);
                            MainController.this.resetSaveButton.setDisable(false);
                            MainController.this.tabPNGToFile.setDisable(false);
                            MainController.this.saveButton.setDisable(false);
                            MainController.this.fileToPNGSave = null;
                        } catch (InterruptedException ignore) {}
                    }
                }
            });
            f2p.addPropertyChangeListener(changeListener);
            this.saveProgressBar.setVisible(true);
            f2p.save();
        } catch (IOException e) {
            this.saveStatus.setText("An error occurred.");
            e.printStackTrace();
        }
    }

    public void handleSaveReset(ActionEvent actionEvent) {
        this.setInputFile(null);
        this.setOutputDirectory(null);
        this.fileToPNGSave = null;
        this.saveProgressBar.setVisible(false);
        this.saveProgressBar.setProgress(0);
        this.saveStatus.setText("");
        this.updateSaveButton();
    }

    public void handleUseDebugSave(ActionEvent actionEvent) {
        if (this.fileToPNGSave != null) return;

        File inputFile = new File(System.getenv("DEBUG_FILE"));
        File outputDirectory = new File(System.getenv("DEBUG_DIRECTORY"));
        this.setInputFile(inputFile);
        this.setOutputDirectory(outputDirectory);
        this.updateSaveButton();
    }

    public void updateRestoreButton() {
        this.restoreButton.setDisable(this.inputDirectory == null || this.outputFile == null);
    }

    public void handleChooseInputDirectory(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Directory containing the PNG(s)");
            Stage stage = new Stage();
            File inputDirectoryUpdate = directoryChooser.showDialog(stage);
            if (inputDirectoryUpdate != null) {
                this.setInputDirectory(inputDirectoryUpdate);
            }
            this.updateRestoreButton();
            this.detectFileName();
        });
    }

    public void handleChooseOutputFile(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Output file (also choose the extension");
            fileChooser.setInitialFileName(this.suggestedOutputName != null ? this.suggestedOutputName : "restoredFile");
            Stage stage = new Stage();
            File outputFileUpdate = fileChooser.showSaveDialog(stage);
            if (outputFileUpdate != null) {
                this.setOutputFile(outputFileUpdate);
            }
            this.updateRestoreButton();
        });
    }

    public void handleRestore(ActionEvent actionEvent) {
        if (this.inputDirectory == null || this.outputFile == null || this.fileToPNGRestore != null) return;
        this.chooseInputDirectoryButton.setDisable(true);
        this.chooseOutputFileButton.setDisable(true);
        this.restoreButton.setDisable(true);
        this.resetRestoreButton.setDisable(true);
        this.tabFileToPNG.setDisable(true);
        try {
            this.fileToPNGRestore = new FileToPNG(outputFile, inputDirectory);
            FileToPNG f2p = this.fileToPNGRestore;

            // Reflect status in GUI
            PropertyChangeListener changeListener = changeEvent -> Platform.runLater(() -> {
                if (changeEvent.getPropertyName().equals("bytesProcessed")) {
                    float progress = f2p.progressPercentage();
                    MainController.this.restoreProgressBar.setProgress(progress);
                    MainController.this.restoreStatus.setText("Bytes processed: " + f2p.getBytesProcessed() + " / " + f2p.getBytesTotal() + " / " + ((int) (f2p.progressPercentage() * 100)) + " %");
                }
                else if (changeEvent.getPropertyName().equals("gotError")) {
                    try {
                        f2p.throwException();
                    } catch (IOException e) {
                        e.printStackTrace();
                        this.resetRestore("Error: " + e.getMessage());
                    }
                }
                else if (changeEvent.getPropertyName().equals("finished")) {
                    this.resetRestore("Done.");
                }
            });
            f2p.addPropertyChangeListener(changeListener);
            this.restoreProgressBar.setVisible(true);
            f2p.restore();

        } catch (IOException e) {
            this.restoreStatus.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void resetRestore(String message) {
        Platform.runLater(() -> {
            MainController.this.restoreStatus.setText(message);
            MainController.this.restoreProgressBar.setVisible(false);
            MainController.this.restoreProgressBar.setProgress(0);
            try {
                if (this.fileToPNGRestore != null) this.fileToPNGRestore.joinThread();
                MainController.this.chooseInputDirectoryButton.setDisable(false);
                MainController.this.chooseOutputFileButton.setDisable(false);
                MainController.this.resetRestoreButton.setDisable(false);
                MainController.this.tabFileToPNG.setDisable(false);
                MainController.this.restoreButton.setDisable(false);
                MainController.this.fileToPNGRestore = null;
            } catch (InterruptedException ignore) {}
        });
    }

    public void handleRestoreReset(ActionEvent actionEvent) {
        this.setInputDirectory(null);
        this.setOutputFile(null);
        this.fileToPNGRestore = null;
        this.restoreProgressBar.setVisible(false);
        this.restoreProgressBar.setProgress(0);
        this.restoreStatus.setText("");
        this.updateRestoreButton();
    }

    public void handleUseDebugRestore(ActionEvent actionEvent) {
        if (this.fileToPNGRestore != null) return;

        File inputDirectory = new File(System.getenv("DEBUG_DIRECTORY"));
        File outputFile = new File(System.getenv("DEBUG_OUTPUT_FILE"));
        this.setInputDirectory(inputDirectory);
        this.setOutputFile(outputFile);
        this.updateRestoreButton();
    }

    public void detectFileName() {
        Platform.runLater(() -> {
            try {
                if (this.inputDirectory != null) {
                    this.suggestedOutputName = FileToPNG.detectFileName(this.inputDirectory);
                }
            } catch (IOException e) {
                this.suggestedOutputName = null;
            }
        });
    }

}
