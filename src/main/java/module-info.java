module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;
    requires jbcrypt;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.json;
    requires java.desktop;
    requires jdk.jsobject;
    requires org.apache.pdfbox;

    opens org.example to javafx.fxml;
    exports org.example;
    opens entite to javafx.base;
    opens controller to javafx.fxml;
}
