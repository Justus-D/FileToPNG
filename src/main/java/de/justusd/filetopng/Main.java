package de.justusd.filetopng;

import javax.swing.*;
import javafx.application.*;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    public static void main(String[] args) {
        if (isJavaFXAvailable()) {
            MainApplication.launch(MainApplication.class);
        } else {
            showFallbackSwingDialog();
        }
    }

    private static boolean isJavaFXAvailable() {
        try {
            Class.forName("javafx.application.Application");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean isJavaFXStageAvailable() {
        try {
            new Stage();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isJavaFXRuntimeAvailable() {
        AtomicBoolean result = new AtomicBoolean(false);
        Platform.startup(() -> {
            System.out.println("fxThread");
            try {
                System.out.println("startup");
                Stage stage = new Stage();
                new Scene(new StackPane(), 1, 1);
                synchronized (result) {
                    result.set(true);
                    result.notify();
                }
                Platform.exit();
                System.out.println("after exit");
            } catch (Exception e) {
                synchronized (result) {
                    result.set(false);
                    result.notify();
                }
            }
        });

        synchronized (result) {
            try {
                result.wait();
                return result.get();
            } catch (InterruptedException ignored) {}
        }

        return result.get();
    }

    private static Thread getJavaFXApplicationThread() {
        final Thread[] javafxThread = new Thread[1];

        Platform.runLater(() -> {
            javafxThread[0] = Thread.currentThread();
            synchronized (javafxThread) {
                javafxThread.notify();
            }
        });

        synchronized (javafxThread) {
            try {
                javafxThread.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return javafxThread[0];
    }

    public static void showFallbackSwingDialog() {
        // Fallback Swing dialog implementation
        JOptionPane.showMessageDialog(null, "JavaFX is not available.\nPlease download a JDK/JRE distribution which includes JavaFX.\nFor example the OpenJDK build from Azul \"Zulu\" (choose JDK FX).");
    }

}
