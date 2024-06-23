module org.example.chatapplication {
  requires javafx.controls;
  requires javafx.fxml;
  requires java.desktop;
  requires java.sql;

  opens org.example.chatapplication to javafx.fxml;
  exports org.example.chatapplication;
}