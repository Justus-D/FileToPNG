package de.justusd.filetopng;

import javax.swing.*;

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

    private static void showFallbackSwingDialog() {
        // Fallback Swing dialog implementation
        JOptionPane.showMessageDialog(null, "JavaFX is not available.\nPlease download a JDK/JRE distribution which includes JavaFX. For example the OpenJDK build from Azul Zulu (with JavaFX).");
    }

}
