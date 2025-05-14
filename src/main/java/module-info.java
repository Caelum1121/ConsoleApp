module org.example.asm2_clone {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.desktop;

    opens asm2_clone to javafx.fxml;
    opens asm2_clone.model to org.hibernate.orm.core;
    opens asm2_clone.controller to javafx.fxml;
    
    exports asm2_clone;
    exports asm2_clone.model;
    exports asm2_clone.controller;
    exports asm2_clone.db;
}