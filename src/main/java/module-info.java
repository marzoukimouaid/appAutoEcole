module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    opens org.example to javafx.fxml;
    exports org.example;
    exports controller;
    opens controller to javafx.fxml;

}
