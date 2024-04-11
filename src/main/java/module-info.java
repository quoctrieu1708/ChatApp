module org.example.chatapplication {
  requires javafx.controls;
  requires javafx.fxml;
  requires java.desktop;

  opens org.example.chatapplication to javafx.fxml;
  exports org.example.chatapplication;
}