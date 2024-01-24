module de.justusd.filetopng {
    requires javafx.controls;
    requires javafx.fxml;
    requires static pngj;
    requires static annotations;
    requires java.desktop;
    requires java.logging;


    opens de.justusd.filetopng to javafx.fxml;
    exports de.justusd.filetopng;
    exports de.justusd.filetopng.controllers;
    opens de.justusd.filetopng.controllers to javafx.fxml;
}
