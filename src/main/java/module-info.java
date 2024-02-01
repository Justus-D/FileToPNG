module de.justusd.filetopng {
    requires static annotations;
    requires java.desktop;
    requires java.logging;

    requires javafx.base;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.controls;

    opens de.justusd.filetopng to javafx.fxml;
    exports de.justusd.filetopng;
    exports de.justusd.filetopng.controllers;
    opens de.justusd.filetopng.controllers to javafx.fxml;
}
