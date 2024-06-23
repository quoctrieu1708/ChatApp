package org.example.chatapplication;

import java.net.BindException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerController extends Application {

  @FXML
  private TextField txtServerPort;
  @FXML
  private TabPane tabPane;

  private ServerSocket serverSocket;
  private ExecutorService executorService;
  public static final String encryptionKey = "your_encryption_key"; // Replace with your encryption key

  public ServerController() {
    // Empty constructor required for Application class
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerChatter.fxml"));
    Parent root = loader.load();
    ServerController controller = loader.getController();

    Scene scene = new Scene(root, 600, 480);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Manager Chat");
    primaryStage.setOnCloseRequest(event -> {
      shutdownServer();
      Platform.exit();
    });
    primaryStage.show();

    controller.initialize();
  }

  public void initialize() {
    try {
      int port = Integer.parseInt(txtServerPort.getText().trim());
      serverSocket = new ServerSocket();
      serverSocket.setReuseAddress(true); // Set reuse address to true
      serverSocket.bind(new InetSocketAddress(port));
      executorService = Executors.newCachedThreadPool(); // Initialize executor service
      startServerListening();
    } catch (NumberFormatException e) {
      // Handle invalid port number
      showAlert(Alert.AlertType.ERROR, "Port Error", "Invalid port number.");
    } catch (IOException e) {
      if (e instanceof BindException) {
        // Inform user that the port is already in use
        showAlert(Alert.AlertType.ERROR, "Port Error", "Port " + txtServerPort.getText().trim() + " is already in use. Please choose a different port.");
      } else {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Server Error", "Could not start the server.");
      }
    }
  }

  private void startServerListening() {
    executorService.submit(() -> {
      while (true) {
        try {
          Socket clientSocket = serverSocket.accept();
          handleClientConnection(clientSocket);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  private void handleClientConnection(Socket clientSocket) {
    try (Socket socket = clientSocket;
        DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
        DataInputStream reader = new DataInputStream(socket.getInputStream())) {

      // Read login information from client
      String username = reader.readUTF();
      String encryptedPassword = reader.readUTF();
      String password = encryptedPassword; // Since we are using hash, we don't need to decrypt

      // Authenticate login information
      if (authenticateUser(username, password)) {
        writer.writeBoolean(true); // Send login success signal to client

        // Create a new tab for the logged-in user
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatView.fxml"));
        Parent root = loader.load();
        ChatViewController controller = loader.getController();
        controller.initializeSocket(socket, username);

        // Add tab to TabPane
        Platform.runLater(() -> {
          Tab tab = new Tab(username);
          tab.setContent(root);
          tabPane.getTabs().add(tab);
        });
      } else {
        writer.writeBoolean(false); // Send login failure signal to client
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private boolean authenticateUser(String username, String password) {
    try (Connection connection = DatabaseConfig.getConnection()) {
      String sql = "SELECT * FROM users WHERE username = ?";
      try (PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setString(1, username);
        try (ResultSet resultSet = statement.executeQuery()) {
          if (resultSet.next()) {
            String storedPassword = resultSet.getString("password");

            // Hash the password from the client and compare with the stored password
            String hashedPassword = hashPassword(password);
            return hashedPassword.equals(storedPassword);
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  private String hashPassword(String password) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.update(encryptionKey.getBytes());
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

  private void showAlert(Alert.AlertType alertType, String title, String message) {
    Alert alert = new Alert(alertType);
    alert.setTitle(title);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void shutdownServer() {
    if (serverSocket != null && !serverSocket.isClosed()) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (executorService != null && !executorService.isShutdown()) {
      executorService.shutdown();
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
