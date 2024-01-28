/*
 * FileToPNG
 * Copyright (c) 2024  Justus Dietrich <git@justus-d.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
