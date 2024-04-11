package org.example.chatapplication;

import javafx.scene.control.TextField;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ClientController extends Application {
  private AnchorPane chatAnchorPane;
  @FXML
  private TextField txtStaff;
  @FXML
  private TextField txtServerIP;
  @FXML
  private TextField txtServerPort;

  private Socket socket;
  private DataOutputStream writer;

  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientChatter.fxml"));
    loader.setController(this);
    Parent root = loader.load();
    chatAnchorPane = (AnchorPane) loader.getNamespace().get("chatAnchorPane");

    primaryStage.setTitle("Client Chat");
    primaryStage.setScene(new Scene(root, 600, 400));
    primaryStage.show();
  }

  @FXML
  private void connectToServer() {
    String serverIP = txtServerIP.getText().trim();
    String serverPortString = txtServerPort.getText().trim();
    String staffName = txtStaff.getText().trim();

    if (!serverIP.isEmpty() && !serverPortString.isEmpty()) {
      try {
        int serverPort = Integer.parseInt(serverPortString);
        socket = new Socket(serverIP, serverPort);
        writer = new DataOutputStream(socket.getOutputStream());

        writer.writeUTF(staffName);
        writer.flush();

        FXMLLoader chatLoader = new FXMLLoader(getClass().getResource("ChatView.fxml"));
        Parent chatRoot = chatLoader.load();
        chatAnchorPane.getChildren().add(chatRoot);

      } catch (NumberFormatException e) {
        System.err.println("Port must be a valid number.");
      } catch (IOException e) {
        System.err.println("Error connecting to the server: " + e.getMessage());
      }
    } else {
      System.err.println("Please enter the server IP and port.");
    }
  }

  public static void main(String[] args) {
    launch(args);
  }

  public void handleConnectButton(ActionEvent actionEvent) {
    connectToServer();
  }
}
