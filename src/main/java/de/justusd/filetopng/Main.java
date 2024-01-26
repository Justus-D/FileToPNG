package de.justusd.filetopng;

import javax.swing.JOptionPane;

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

    public static void showFallbackSwingDialog() {
        // Fallback Swing dialog implementation
        JOptionPane.showMessageDialog(null, "JavaFX is not available.\nPlease download a JDK/JRE distribution which includes JavaFX.\nFor example the OpenJDK build from Azul \"Zulu\" (choose JDK FX).");
    }

}
