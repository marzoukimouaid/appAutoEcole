module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.json;
    requires java.desktop;

    // If your main application class is in org.example, open + export it:
    opens org.example to javafx.fxml;
    exports org.example;
    opens entite to javafx.base;
    // Open the controller package so FXML can reflectively access your controllers:
    opens controller to javafx.fxml;
    // If you need to call these controllers from outside the module, also do:
    // exports controller;

    // If you have other packages that contain FXML-based controllers,
    // or you want to reference them externally, also open/export them similarly:
    // opens dao to javafx.fxml;       // if needed
    // opens entite to javafx.fxml;    // if needed
    // opens service to javafx.fxml;   // if needed
    // opens utils to javafx.fxml;     // if needed
}
