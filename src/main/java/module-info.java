module de.justusd.filetopng {
    requires javafx.controls;
    requires javafx.fxml;
    requires pngj;
    requires annotations;
    requires java.desktop;


    opens de.justusd.filetopng to javafx.fxml;
    exports de.justusd.filetopng;
    exports de.justusd.filetopng.controllers;
    opens de.justusd.filetopng.controllers to javafx.fxml;
}