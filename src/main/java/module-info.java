module de.justusd.filetopng {
    requires javafx.controls;
    requires javafx.fxml;
//    requires pngj;
    requires static annotations;
    requires java.desktop;
    requires java.logging;
    requires javafx.web;


    opens de.justusd.filetopng to javafx.fxml;
    exports de.justusd.filetopng;
    exports de.justusd.filetopng.controllers;
    opens de.justusd.filetopng.controllers to javafx.fxml;
}
