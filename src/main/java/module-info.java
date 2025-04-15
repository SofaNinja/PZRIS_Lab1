module org.example.pzris_lab1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;


    opens org.example.pzris_lab1 to javafx.fxml;
    exports org.example.pzris_lab1;
}