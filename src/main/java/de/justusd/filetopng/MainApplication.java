package de.justusd.filetopng;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        if (isJavaFXAvailable()) {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            stage.setTitle("FileToPNG");
            stage.setScene(scene);
            stage.show();
        } else {
            Main.showFallbackSwingDialog();
        }
    }

    private boolean isJavaFXAvailable() {
        try {
            new Stage();
            return true;
        } catch (Error e) {
            return false;
        }
    }

    public static void main(String[] args) {
        launch();
    }

}
