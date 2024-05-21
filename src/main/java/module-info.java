module uk.ac.aber.dcs.cs39440.tjamflowlines {
    requires javafx.controls;
    requires javafx.fxml;
    requires junit;


    opens uk.ac.aber.dcs.cs39440.tjamflowlines to javafx.fxml;
    exports uk.ac.aber.dcs.cs39440.tjamflowlines;
    exports uk.ac.aber.dcs.cs39440.tjamflowlines.tests;
}