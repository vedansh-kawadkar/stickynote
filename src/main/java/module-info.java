module com.stickynote.app {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.stickynote.app to javafx.fxml;
    exports com.stickynote.app;
}
