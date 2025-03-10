module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.json;
    requires java.desktop;

    opens org.example to javafx.fxml;
    exports org.example;

    exports Secretaire.controller;
    opens Secretaire.controller to javafx.fxml;

    exports AutoEcole.controller;
    opens AutoEcole.controller to javafx.fxml;

    exports Authentication.controller;
    opens Authentication.controller to javafx.fxml;

    exports Moniteur.controller;
    opens Moniteur.controller to javafx.fxml;

    // Added export and opens for the entity package
    exports Candidat.entite;
    opens Candidat.entite to javafx.fxml, javafx.base;
}
