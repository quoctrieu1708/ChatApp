package org.example.chatapplication;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ClientController extends Application {
  @FXML
  private AnchorPane loginPane;
  @FXML
  private TextField txtUsername;
  @FXML
  private PasswordField txtPassword;

  private Socket socket;
  private DataOutputStream writer;

  @Override
  public void start(Stage primaryStage) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("ClientChatter.fxml"));

    primaryStage.setTitle("Client Chat");
    primaryStage.setScene(new Scene(root, 600, 400));
    primaryStage.show();
  }

  @FXML
  private void connectToServer() {
    String serverIP = "localhost"; // Địa chỉ IP của server
    int serverPort = 4060; // Cổng của server

    String username = txtUsername.getText().trim();
    String password = txtPassword.getText().trim();

    if (!username.isEmpty() && !password.isEmpty()) {
      try {
        socket = new Socket(serverIP, serverPort);
        writer = new DataOutputStream(socket.getOutputStream());

        // Mã hóa mật khẩu trước khi gửi
        String encryptedPassword = hashPassword(password);
        writer.writeUTF(username);
        writer.writeUTF(encryptedPassword);
        writer.flush();

        DataInputStream reader = new DataInputStream(socket.getInputStream());
        boolean loginSuccess = reader.readBoolean();

        if (loginSuccess) {
          // Đăng nhập thành công, chuyển sang giao diện chat
          FXMLLoader chatLoader = new FXMLLoader(getClass().getResource("ChatView.fxml"));
          AnchorPane chatView = chatLoader.load();
          ChatViewController controller = chatLoader.getController();
          controller.initializeSocket(socket, username);
          loginPane.getChildren().add(chatView);
          loginPane.setVisible(false); // Ẩn giao diện đăng nhập
        } else {
          // Đăng nhập thất bại, hiển thị thông báo lỗi
          showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
        }

      } catch (IOException e) {
        System.err.println("Error connecting to the server: " + e.getMessage());
        showAlert(Alert.AlertType.ERROR, "Connection Error", "Could not connect to the server.");
      }
    } else {
      showAlert(Alert.AlertType.ERROR, "Login Failed", "Username and password are required.");
    }
  }

  @FXML
  public void handleLoginButton() {
    connectToServer();
  }

  private void showAlert(Alert.AlertType alertType, String title, String message) {
    Alert alert = new Alert(alertType);
    alert.setTitle(title);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private String hashPassword(String password) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.update(ServerController.encryptionKey.getBytes()); // Sử dụng khóa mã hóa của server
      byte[] hashedBytes = digest.digest(password.getBytes());
      StringBuilder stringBuilder = new StringBuilder();
      for (byte hashedByte : hashedBytes) {
        stringBuilder.append(Integer.toString((hashedByte & 0xff) + 0x100, 16).substring(1));
      }
      return stringBuilder.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
