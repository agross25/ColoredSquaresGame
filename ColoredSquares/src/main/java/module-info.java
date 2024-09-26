module gross.coloredsquares.coloredsquares {
    requires javafx.controls;
    requires javafx.fxml;


    opens gross.coloredsquares to javafx.fxml;
    exports gross.coloredsquares;
}